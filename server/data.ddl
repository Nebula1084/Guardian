CREATE SEQUENCE calls_id_seq;

CREATE TABLE calls
(
  id     SERIAL NOT NULL
    CONSTRAINT calls_pkey
    PRIMARY KEY,
  number TEXT   NOT NULL,
  type   TEXT   NOT NULL,
  time   INTEGER
);

CREATE FUNCTION tag_call(n TEXT, t TEXT)
  RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
  ok BOOLEAN;

BEGIN

  ok := NOT exists(
      SELECT number
      FROM guardian.calls
      WHERE calls.number = n AND calls.type = t
  );

  IF ok
  THEN
    PERFORM pg_advisory_xact_lock(1);
    INSERT INTO guardian.calls (number, type, time)
      SELECT
        n,
        t,
        0
      WHERE NOT exists(
          SELECT number
          FROM guardian.calls
          WHERE calls.number = n AND calls.type = t
      );

  END IF;


  UPDATE guardian.calls
  SET time = time + 1
  WHERE calls.number = n
        AND
        calls.type = t;

END;
$$;

