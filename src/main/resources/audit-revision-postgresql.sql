DO $$
DECLARE
    highest_revision bigint;
    sequence_value bigint;
    sequence_called boolean;
    allocation_size bigint;
    next_upper_bound bigint;
BEGIN
    IF to_regclass('revinfo') IS NULL OR to_regclass('revinfo_seq') IS NULL THEN
        RETURN;
    END IF;

    PERFORM pg_advisory_xact_lock(hashtext('investir-mais:audit-revision-initialization'));
    LOCK TABLE revinfo IN SHARE ROW EXCLUSIVE MODE;
    SELECT COALESCE(MAX(rev), 0) INTO highest_revision FROM revinfo;
    SELECT last_value, is_called INTO sequence_value, sequence_called FROM revinfo_seq;
    SELECT seqincrement INTO allocation_size FROM pg_sequence WHERE seqrelid = 'revinfo_seq'::regclass;
    next_upper_bound := sequence_value + CASE WHEN sequence_called THEN allocation_size ELSE 0 END;

    IF highest_revision > 0 AND next_upper_bound - allocation_size + 1 <= highest_revision THEN
        EXECUTE format('ALTER SEQUENCE revinfo_seq RESTART WITH %s', highest_revision + allocation_size);
    END IF;
END;
$$;
