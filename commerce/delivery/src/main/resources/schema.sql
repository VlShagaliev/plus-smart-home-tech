CREATE TABLE IF NOT EXISTS address (
    address_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country VARCHAR,
    city VARCHAR,
    street VARCHAR,
    house VARCHAR,
    flat VARCHAR
);

CREATE TABLE IF NOT EXISTS delivery (
    delivery_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_address_id UUID NOT NULL REFERENCES address(address_id),
    to_address_id UUID NOT NULL REFERENCES address(address_id),
    order_id UUID NOT NULL,
    delivery_state VARCHAR NOT NULL
);