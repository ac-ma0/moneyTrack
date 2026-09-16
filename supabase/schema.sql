create extension if not exists "pgcrypto";

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text not null default '',
  role text not null default 'user' check (role in ('admin','user')),
  currency text not null default 'PHP',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create table if not exists public.user_permissions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  permission text not null,
  granted_by uuid references auth.users(id) on delete set null,
  created_at timestamptz not null default now(),
  unique(user_id, permission)
);
create table if not exists public.categories (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id) on delete cascade,
  name text not null,
  kind text not null check (kind in ('income','expense')),
  created_at timestamptz not null default now(),
  unique(user_id, name, kind)
);
create table if not exists public.income (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  amount numeric(14,2) not null check (amount > 0),
  category text not null default '',
  occurred_at timestamptz not null default now(),
  notes text not null default '',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create table if not exists public.expenses (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  amount numeric(14,2) not null check (amount > 0),
  category text not null default '',
  occurred_at timestamptz not null default now(),
  notes text not null default '',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create table if not exists public.debts (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  borrower text not null,
  principal numeric(14,2) not null check (principal > 0),
  interest_rate numeric(8,4) not null default 0 check (interest_rate >= 0),
  interest_type text not null default 'none' check (interest_type in ('none','simple','monthly_compound')),
  months integer not null check (months > 0),
  start_date date not null default current_date,
  due_date date not null,
  monthly_expected numeric(14,2) not null check (monthly_expected >= 0),
  total_payable numeric(14,2) not null check (total_payable >= 0),
  notes text not null default '',
  status text not null default 'active' check (status in ('active','completed','cancelled')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create table if not exists public.debt_monthly_payments (
  id uuid primary key default gen_random_uuid(),
  debt_id uuid not null references public.debts(id) on delete cascade,
  user_id uuid not null references auth.users(id) on delete cascade,
  payment_month date not null,
  expected_amount numeric(14,2) not null check (expected_amount >= 0),
  paid_amount numeric(14,2) not null default 0 check (paid_amount >= 0),
  status text not null default 'unpaid' check (status in ('paid','unpaid','partially_paid')),
  paid_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(debt_id, payment_month)
);
create table if not exists public.audit_logs (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id) on delete set null,
  action text not null,
  record_type text not null,
  record_id uuid,
  old_value jsonb,
  new_value jsonb,
  created_at timestamptz not null default now()
);
create table if not exists public.app_settings (
  user_id uuid primary key references auth.users(id) on delete cascade,
  background_color text not null default '',
  text_color text not null default '',
  accent_color text not null default '',
  dark_mode boolean not null default false,
  updated_at timestamptz not null default now()
);

create index if not exists income_user_date on public.income(user_id, occurred_at desc);
create index if not exists expenses_user_date on public.expenses(user_id, occurred_at desc);
create index if not exists debts_user_status on public.debts(user_id, status);
create index if not exists debt_payments_user_month on public.debt_monthly_payments(user_id, payment_month);
create index if not exists audit_user_time on public.audit_logs(user_id, created_at desc);

create or replace function public.is_admin() returns boolean language sql stable security definer set search_path = public
as $$ select exists(select 1 from public.profiles where id = auth.uid() and role = 'admin') $$;
create or replace function public.has_permission(p text) returns boolean language sql stable security definer set search_path = public
as $$ select public.is_admin() or exists(select 1 from public.user_permissions where user_id = auth.uid() and permission = p) $$;
create or replace function public.set_updated_at() returns trigger language plpgsql as $$
begin new.updated_at = now(); return new; end $$;

do $$ declare t text; begin
  foreach t in array array['profiles','user_permissions','categories','income','expenses','debts','debt_monthly_payments','audit_logs','app_settings'] loop
    execute format('alter table public.%I enable row level security', t);
  end loop;
end $$;

create policy profiles_read on public.profiles for select using (id = auth.uid() or public.is_admin());
create policy profiles_admin_write on public.profiles for all using (public.is_admin()) with check (public.is_admin());
create policy profiles_self_update on public.profiles for update using (id = auth.uid()) with check (id = auth.uid());
create policy permissions_read on public.user_permissions for select using (user_id = auth.uid() or public.is_admin());
create policy permissions_admin_write on public.user_permissions for all using (public.is_admin()) with check (public.is_admin());
create policy categories_access on public.categories for all using (user_id = auth.uid() or (user_id is null and public.is_admin())) with check (user_id = auth.uid() or public.is_admin());
create policy income_access on public.income for all using (user_id = auth.uid() or public.has_permission('view_all_records')) with check (user_id = auth.uid() or public.has_permission('view_all_records'));
create policy expenses_access on public.expenses for all using (user_id = auth.uid() or public.has_permission('view_all_records')) with check (user_id = auth.uid() or public.has_permission('view_all_records'));
create policy debts_access on public.debts for all using (user_id = auth.uid() or public.has_permission('view_all_records')) with check (user_id = auth.uid() or public.has_permission('view_all_records'));
create policy payments_access on public.debt_monthly_payments for all using (user_id = auth.uid() or public.has_permission('view_all_records')) with check (user_id = auth.uid() or public.has_permission('view_all_records'));
create policy audit_read on public.audit_logs for select using (user_id = auth.uid() or public.is_admin() or public.has_permission('view_history'));
create policy audit_insert on public.audit_logs for insert with check (user_id = auth.uid());
create policy settings_access on public.app_settings for all using (user_id = auth.uid()) with check (user_id = auth.uid());

create or replace function public.handle_new_user() returns trigger language plpgsql security definer set search_path = public
as $$ begin insert into public.profiles(id) values(new.id) on conflict do nothing; return new; end $$;
drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created after insert on auth.users for each row execute function public.handle_new_user();
