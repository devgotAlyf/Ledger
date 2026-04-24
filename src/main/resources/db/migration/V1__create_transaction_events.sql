create table if not exists transaction_events (
    id bigserial primary key,
    transaction_id uuid not null,
    event_type varchar(32) not null,
    payload jsonb not null,
    ai_decision text,
    created_at timestamptz not null default now()
);

create index if not exists idx_transaction_events_transaction_id
    on transaction_events (transaction_id);

create index if not exists idx_transaction_events_user_id_created_at
    on transaction_events ((payload ->> 'userId'), created_at desc);
