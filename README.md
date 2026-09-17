# MoneyTrack PH

The supplied Supabase URL and publishable key are configured as safe client-side defaults. You can override them with `SUPABASE_URL` and `SUPABASE_ANON_KEY` Gradle properties; never use a service_role key. Run `supabase/schema.sql` in the Supabase SQL editor. The app starts with Supabase email/password authentication and offers a clearly labeled offline mode.

Open the project in AndroidIDE and run the `app` configuration. It uses Android Gradle Plugin 4.0.2, compatible with AndroidIDE's Gradle 6.1.1 runtime, Kotlin 1.4.32, and a simple Android Views UI. Compose was intentionally removed because AGP 4.0.2's Compose compiler lookup is incompatible with the available Compose 1.0.5 artifact on AndroidIDE.

The included desktop wrapper uses Gradle 6.7.1, but AndroidIDE may use its installed Gradle 6.1.1. AGP 4.0.2 supports that AndroidIDE runtime, so the same project can be evaluated in both environments.

The local data layer is Room-first and stores UUID-keyed income, expense, debt, monthly payment, audit, and sync-queue records. Deletes are soft deletes, writes create local audit entries, and queued operations remain pending until an authenticated uploader is supplied. The app includes basic add/delete/status flows and an offline continuation option.

Supabase Auth is wired through the lightweight REST compatibility client in `SupabaseAuth.kt`. The supabase-kt artifacts are intentionally not included because their current transitive Kotlin/Ktor versions require Kotlin 1.8+, which conflicts with the AndroidIDE Gradle 6.1.1/Kotlin 1.4.32 toolchain. Set `SUPABASE_URL` and `SUPABASE_ANON_KEY` in `gradle.properties` only when overriding the safe defaults.

The Android Views MVP now includes a styled dashboard with summary cards and quick actions, a working navigation drawer, Income and Expenses screens with search and soft-delete actions, Debt Tracker with status and monthly payment status choices, History, Reports, Settings with dark-theme persistence/reset, My Profile, Users, Login/Signup, and offline continuation.

The REST sync layer uploads queued rows with authenticated bearer tokens, retries transient failures, and can be triggered by Android connectivity callbacks. Transaction/debt edit forms include dates, categories, interest schedules, and notes. Reports include a lightweight custom View chart; roles and permissions are enforced locally and mirrored by Supabase RLS.

The activity uses an AppCompat theme, and the Room database is versioned with a destructive migration fallback for this MVP. This prevents startup crashes after upgrading from an earlier database schema; local data may be reset once after an app update.
