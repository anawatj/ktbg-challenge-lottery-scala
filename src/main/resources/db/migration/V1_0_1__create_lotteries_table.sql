CREATE TABLE IF NOT EXISTS lotteries (

    ticket VARCHAR(6)  PRIMARY KEY,
    amount INT NOT NULL,
    price  double precision NOT NULL

);