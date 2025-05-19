-- 1. Roles
CREATE TABLE roles
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 2. Permissions
CREATE TABLE permissions
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 3. Users
CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- 4. Owners (natural & legal persons)
CREATE TABLE owners
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    owner_type VARCHAR(10) CHECK (owner_type IN ('NATURAL', 'LEGAL')),
    wallet_id  UUID UNIQUE  NOT NULL REFERENCES wallets (id) ON DELETE CASCADE,
);

-- 5. Wallets
CREATE TABLE wallets
(
    id             UUID PRIMARY KEY,
    fiat_balance   NUMERIC(18, 2) NOT NULL DEFAULT 0,
    crypto_balance NUMERIC(18, 2) NOT NULL DEFAULT 0,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);


-- 6. Transactions
CREATE TABLE transactions
(
    id                   UUID PRIMARY KEY,
    origin_owner_id      UUID           NOT NULL REFERENCES owners (id) ON DELETE CASCADE,
    destination_owner_id UUID           NOT NULL REFERENCES owners (id) ON DELETE CASCADE,
    amount               NUMERIC(18, 8) NOT NULL,
    currency             VARCHAR(10)    NOT NULL,
    conversion_rate      NUMERIC(18, 2) NOT NULL,
    concept              VARCHAR(255),
    blockchain_tx_hash   VARCHAR(255),
    transaction_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    status               VARCHAR(10) CHECK (status IN ('PENDING', 'SUCCESS', 'FAILURE'))
);

-- 7. User–Role bridge
CREATE TABLE user_roles
(
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 8. Role–Permission bridge
CREATE TABLE role_permissions
(
    role_id       UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);
