CREATE TABLE IF NOT EXISTS user_tickets (

    id  serial  PRIMARY KEY,
    user_id INTEGER NOT NULL,
    ticket  VARCHAR(6) NOT NULL
);