-- @tag:auth @tag:account-linking
-- Account & identity data model (ADR 0002). App accounts are separate from learner
-- profiles (users): one app_account owns one or more learner profiles, and several
-- external OAuth identities can resolve to the same app_account. Forward-only (V3):
-- V1/V2 stay untouched; only additive changes here.

-- Application account: the identity a token's `sub` maps to (owner/child, etc.).
CREATE TABLE app_account (
    id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name text        NOT NULL,
    role         text        NOT NULL CHECK (role IN ('owner', 'child')),
    created_at   timestamptz NOT NULL DEFAULT now()
);

-- External OAuth identity (IdP `sub`) linked to an app_account. Several identities
-- may map to one account (child on multiple devices). Idempotent per (provider, subject).
CREATE TABLE external_identity (
    id         uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id uuid        NOT NULL REFERENCES app_account (id) ON DELETE CASCADE,
    provider   text        NOT NULL,
    subject    text        NOT NULL,
    email      text        NULL,
    linked_at  timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_external_identity_provider_subject UNIQUE (provider, subject)
);

CREATE INDEX idx_external_identity_account ON external_identity (account_id);

-- A learner profile is owned by an app_account. Nullable for backward compat with
-- existing rows; detaching an account leaves orphaned (unowned) profiles rather than
-- deleting learning data.
ALTER TABLE users
    ADD COLUMN owner_account_id uuid NULL REFERENCES app_account (id) ON DELETE SET NULL;

CREATE INDEX idx_users_owner_account ON users (owner_account_id);
