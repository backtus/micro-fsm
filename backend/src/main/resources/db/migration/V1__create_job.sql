CREATE TABLE job (
    id uuid PRIMARY KEY,
    tenant_id uuid NOT NULL,
    customer_name varchar(200) NOT NULL,
    address varchar(300) NOT NULL,
    scheduled_date date NOT NULL,
    status varchar(20) NOT NULL,
    actual_minutes integer,
    version bigint NOT NULL,
    created_at timestamptz NOT NULL
);
