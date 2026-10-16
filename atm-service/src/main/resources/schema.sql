CREATE TABLE tarjeta (
    numero VARCHAR(19) NOT NULL PRIMARY KEY,
    activa CHAR(1)     NOT NULL,
    CONSTRAINT ck_tarjeta_activa CHECK (activa IN ('S', 'N'))
);

CREATE TABLE cuenta (
    numero VARCHAR(20)    NOT NULL PRIMARY KEY,
    cbu    VARCHAR(22)    NOT NULL,
    saldo  DECIMAL(15, 2) NOT NULL,
    CONSTRAINT uk_cuenta_cbu UNIQUE (cbu),
    CONSTRAINT ck_cuenta_saldo_no_negativo CHECK (saldo >= 0)
);

CREATE TABLE tarjeta_cuenta (
    tarjeta VARCHAR(19) NOT NULL,
    cuenta  VARCHAR(20) NOT NULL,
    CONSTRAINT pk_tarjeta_cuenta PRIMARY KEY (tarjeta, cuenta),
    CONSTRAINT fk_tc_tarjeta FOREIGN KEY (tarjeta) REFERENCES tarjeta (numero),
    CONSTRAINT fk_tc_cuenta FOREIGN KEY (cuenta) REFERENCES cuenta (numero)
);
