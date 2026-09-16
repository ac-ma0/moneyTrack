# MoneyTrack PH

The supplied Supabase URL and publishable key are configured as safe client-side defaults. You can override them with `SUPABASE_URL` and `SUPABASE_ANON_KEY` Gradle properties; never use a service_role key. Run `supabase/schema.sql` in the Supabase SQL editor. The app starts with Supabase email/password authentication and offers a clearly labeled offline mode.

Open the project in AndroidIDE and run the `app` configuration. It uses Android Gradle Plugin 4.2.2, Gradle 6-compatible conventions, Kotlin 1.4.32, and conservative Compose 1.0.5 dependencies.

The local data layer is Room-first and stores UUID-keyed income, expense, debt, monthly payment, audit, and sync-queue records. Deletes are soft deletes, writes create local audit entries, and queued operations remain pending until an authenticated uploader is supplied. The app includes basic add/delete/status flows and an offline continuation option.

Supabase Auth is wired through a REST compatibility client, while the AndroidIDE-compatible `gotrue-kt` and `postgrest-kt` dependencies remain available for incremental API migration. Set `SUPABASE_URL` and `SUPABASE_ANON_KEY` in `gradle.properties` only when overriding the safe defaults.

The current project is an MVP foundation: production sync upload mapping, full edit/search/filter/chart screens, and complete admin user-management UI still require additional iterations.
