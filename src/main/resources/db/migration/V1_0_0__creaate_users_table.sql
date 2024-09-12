CREATE TABLE IF NOT EXISTS users (

    id  serial  PRIMARY KEY,
    username varchar(100),
    password varchar(8000),
    first_name varchar(100),
    last_name varchar(100),
    role varchar(100)

);