-- MoneyTrack PH Supabase schema. Run in Supabase SQL editor.
create extension if not exists "uuid-ossp";
create table if not exists public.profiles (id uuid primary key references auth.users(id) on delete cascade, full_name text, role text not null default 'user' check (role in ('admin','user')), created_at timestamptz default now());
alter table public.profiles add column if not exists role text not null default 'user';
create table if not exists public.user_permissions (user_id uuid references auth.users(id) on delete cascade, permission text not null, granted_by uuid references auth.users(id), created_at timestamptz default now(), primary key (user_id, permission));
create table if not exists public.categories (id uuid primary key default uuid_generate_v4(), user_id uuid references auth.users(id) on delete cascade, name text not null, kind text not null check (kind in ('income','expense')), created_at timestamptz default now());
create table if not exists public.income (id uuid primary key default uuid_generate_v4(), user_id uuid not null references auth.users(id) on delete cascade, category_id uuid references public.categories(id), title text not null, amount numeric(14,2) not null check (amount >= 0), occurred_on date default current_date, notes text);
create table if not exists public.expenses (id uuid primary key default uuid_generate_v4(), user_id uuid not null references auth.users(id) on delete cascade, category_id uuid references public.categories(id), title text not null, amount numeric(14,2) not null check (amount >= 0), occurred_on date default current_date, notes text);
alter table public.income add column if not exists category text;
alter table public.income add column if not exists user_id uuid references auth.users(id) on delete cascade;
alter table public.income add column if not exists title text not null default '';
alter table public.income add column if not exists amount numeric(14,2) not null default 0;
alter table public.income add column if not exists occurred_on date default current_date;
alter table public.income add column if not exists notes text;
alter table public.income add column if not exists category_id uuid references public.categories(id);
alter table public.income add column if not exists updated_at timestamptz default now();
alter table public.income add column if not exists deleted boolean not null default false;
alter table public.expenses add column if not exists category text;
alter table public.expenses add column if not exists user_id uuid references auth.users(id) on delete cascade;
alter table public.expenses add column if not exists title text not null default '';
alter table public.expenses add column if not exists amount numeric(14,2) not null default 0;
alter table public.expenses add column if not exists occurred_on date default current_date;
alter table public.expenses add column if not exists notes text;
alter table public.expenses add column if not exists category_id uuid references public.categories(id);
alter table public.expenses add column if not exists updated_at timestamptz default now();
alter table public.expenses add column if not exists deleted boolean not null default false;
create table if not exists public.debts (id uuid primary key default uuid_generate_v4(), user_id uuid not null references auth.users(id) on delete cascade, person text not null, principal_amount numeric(14,2) not null, interest_rate numeric(8,4) not null default 0, interest_type text not null default 'flat' check (interest_type in ('flat','simple','reducing')), number_of_months integer not null default 1 check (number_of_months > 0), start_date date default current_date, due_date date, monthly_expected_payment numeric(14,2), total_payable numeric(14,2), notes text, status text not null default 'active' check (status in ('active','paid','cancelled')));
create table if not exists public.debt_monthly_payments (id uuid primary key default uuid_generate_v4(), debt_id uuid not null references public.debts(id) on delete cascade, user_id uuid not null references auth.users(id) on delete cascade, payment_month date not null, amount numeric(14,2) not null check (amount >= 0), status text not null default 'unpaid' check (status in ('paid','unpaid','partially_paid')), notes text, created_at timestamptz default now());
create table if not exists public.audit_logs (id uuid primary key default uuid_generate_v4(), user_id uuid references auth.users(id) on delete set null, action text not null, record_type text, record_id uuid, old_value jsonb, new_value jsonb, timestamp timestamptz default now(), created_at timestamptz default now());
create table if not exists public.app_settings (user_id uuid primary key references auth.users(id) on delete cascade, dark_theme boolean default false, currency text default 'PHP', updated_at timestamptz default now());
create or replace function public.is_admin() returns boolean language sql stable security definer set search_path = public as $$ select exists (select 1 from public.profiles where id = auth.uid() and role = 'admin') $$;
-- Older installations may have a smaller debts table. Keep this migration additive
-- so existing data survives while the current client can use the full model.
alter table public.debts add column if not exists user_id uuid references auth.users(id) on delete cascade;
alter table public.debts add column if not exists person text not null default 'Unknown';
alter table public.debts add column if not exists principal_amount numeric(14,2) not null default 0;
alter table public.debts add column if not exists interest_rate numeric(8,4) not null default 0;
alter table public.debts add column if not exists interest_type text not null default 'flat';
alter table public.debts add column if not exists number_of_months integer not null default 1;
alter table public.debts add column if not exists start_date date default current_date;
alter table public.debts add column if not exists due_date date;
alter table public.debts add column if not exists monthly_expected_payment numeric(14,2);
alter table public.debts add column if not exists total_payable numeric(14,2);
alter table public.debts add column if not exists notes text;
alter table public.debts add column if not exists status text not null default 'active';
alter table public.debts add column if not exists updated_at timestamptz default now();
alter table public.debts add column if not exists deleted boolean not null default false;

-- Final compatibility pass for databases created from an older schema.
-- Run these checks before any view, policy, index, or trigger references.
do $$
begin
  if to_regclass('public.income') is not null then
    execute 'alter table public.income add column if not exists user_id uuid';
    execute 'alter table public.income add column if not exists title text not null default ''''';
    execute 'alter table public.income add column if not exists amount numeric(14,2) not null default 0';
    execute 'alter table public.income add column if not exists occurred_on date default current_date';
    execute 'alter table public.income add column if not exists notes text';
  end if;
  if to_regclass('public.expenses') is not null then
    execute 'alter table public.expenses add column if not exists user_id uuid';
    execute 'alter table public.expenses add column if not exists title text not null default ''''';
    execute 'alter table public.expenses add column if not exists amount numeric(14,2) not null default 0';
    execute 'alter table public.expenses add column if not exists occurred_on date default current_date';
    execute 'alter table public.expenses add column if not exists notes text';
  end if;
end $$;

-- Legacy-compatible view for older clients.
create or replace view public.transactions as select id, user_id, 'income'::text as kind, title, amount, occurred_on, notes from public.income union all select id, user_id, 'expense'::text, title, amount, occurred_on, notes from public.expenses;

alter table public.profiles enable row level security;
-- All application tables are private to the authenticated owner.
do $$ declare t text; begin foreach t in array array['user_permissions','categories','income','expenses','debts','debt_monthly_payments','audit_logs','app_settings'] loop execute format('alter table public.%I enable row level security', t); end loop; end $$;
-- Policies are recreated so this file is safe to apply repeatedly during development.
do $$ declare p record; begin
  for p in select policyname, tablename from pg_policies where schemaname='public'
    and tablename in ('profiles','user_permissions','categories','income','expenses','debts','debt_monthly_payments','audit_logs','app_settings')
  loop execute format('drop policy if exists %I on public.%I', p.policyname, p.tablename); end loop;
end $$;
create policy "profiles own rows" on public.profiles for all using (auth.uid() = id or public.is_admin()) with check (auth.uid() = id or public.is_admin());
create policy "permissions own rows" on public.user_permissions for all using (auth.uid() = user_id or public.is_admin()) with check (public.is_admin());
create policy "categories own rows" on public.categories for all using (auth.uid() = user_id or public.is_admin()) with check (auth.uid() = user_id or public.is_admin());
create policy "income own rows" on public.income for all using (auth.uid() = user_id or public.is_admin()) with check (auth.uid() = user_id or public.is_admin());
create policy "expenses own rows" on public.expenses for all using (auth.uid() = user_id or public.is_admin()) with check (auth.uid() = user_id or public.is_admin());
create policy "debts own rows" on public.debts for all using (auth.uid() = user_id or public.is_admin()) with check (auth.uid() = user_id or public.is_admin());
create policy "payments own rows" on public.debt_monthly_payments for all using (auth.uid() = user_id or public.is_admin()) with check (auth.uid() = user_id or public.is_admin());
create policy "audit own rows" on public.audit_logs for select using (auth.uid() = user_id or public.is_admin());
create policy "audit insert own rows" on public.audit_logs for insert with check (auth.uid() = user_id);
create policy "settings own rows" on public.app_settings for all using (auth.uid() = user_id or public.is_admin()) with check (auth.uid() = user_id or public.is_admin());
create or replace function public.handle_new_user() returns trigger language plpgsql security definer set search_path = public as $$ begin insert into public.profiles(id) values (new.id); insert into public.app_settings(user_id) values (new.id); return new; end; $$;
drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created after insert on auth.users for each row execute procedure public.handle_new_user();

create index if not exists income_user_updated_idx on public.income(user_id, updated_at);
create index if not exists expenses_user_updated_idx on public.expenses(user_id, updated_at);
create index if not exists debts_user_updated_idx on public.debts(user_id, updated_at);
create index if not exists payments_user_updated_idx on public.debt_monthly_payments(user_id, payment_month);
create index if not exists audit_user_timestamp_idx on public.audit_logs(user_id, timestamp desc);
create or replace function public.audit_finance_change() returns trigger language plpgsql security definer set search_path=public as $$
begin
  insert into public.audit_logs(user_id, action, record_type, record_id, old_value, new_value)
  values (coalesce(new.user_id, old.user_id), lower(TG_OP), TG_TABLE_NAME, coalesce(new.id, old.id),
    case when TG_OP='INSERT' then null else to_jsonb(old) end,
    case when TG_OP='DELETE' then null else to_jsonb(new) end);
  return coalesce(new, old);
end; $$;
drop trigger if exists income_audit on public.income;
create trigger income_audit after insert or update or delete on public.income for each row execute procedure public.audit_finance_change();
drop trigger if exists expenses_audit on public.expenses;
create trigger expenses_audit after insert or update or delete on public.expenses for each row execute procedure public.audit_finance_change();
drop trigger if exists debts_audit on public.debts;
create trigger debts_audit after insert or update or delete on public.debts for each row execute procedure public.audit_finance_change();
drop trigger if exists payments_audit on public.debt_monthly_payments;
create trigger payments_audit after insert or update or delete on public.debt_monthly_payments for each row execute procedure public.audit_finance_change();
