DO $$
DECLARE
    target_table text;
    target_column text;
BEGIN
    FOREACH target_table IN ARRAY ARRAY['tb_user', 'tb_user_audit'] LOOP
        FOREACH target_column IN ARRAY ARRAY['name', 'age', 'birth_date', 'password'] LOOP
            IF EXISTS (
                SELECT 1 FROM pg_attribute
                WHERE attrelid = to_regclass(target_table)
                  AND attname = target_column AND attnotnull AND NOT attisdropped
            ) THEN
                EXECUTE format('ALTER TABLE %I ALTER COLUMN %I DROP NOT NULL', target_table, target_column);
            END IF;
        END LOOP;
    END LOOP;
END;
$$;
