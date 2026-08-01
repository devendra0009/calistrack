-- Add date of birth; age remains for legacy rows and is synced when DOB is set.
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS date_of_birth DATE;
