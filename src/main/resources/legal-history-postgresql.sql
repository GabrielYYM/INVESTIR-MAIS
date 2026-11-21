CREATE OR REPLACE FUNCTION protect_legal_history() RETURNS trigger LANGUAGE plpgsql AS
'BEGIN RAISE EXCEPTION ''Published legal documents and acceptance records are immutable''; END;';

-- Ordinary changes remain forbidden, including empty DELETE statements and TRUNCATE.
-- Final erasure permits only the specified subject, after its account has been removed
-- in the same transaction. The row trigger prevents widening the DELETE predicate.
CREATE OR REPLACE FUNCTION protect_legal_acceptance() RETURNS trigger LANGUAGE plpgsql AS '
DECLARE subject uuid := NULLIF(current_setting(''app.erasure_user_id'', true), '''')::uuid;
BEGIN
    IF TG_OP = ''DELETE'' AND subject IS NOT NULL AND NOT EXISTS (SELECT 1 FROM tb_user WHERE id = subject) THEN
        IF TG_LEVEL = ''STATEMENT'' THEN RETURN NULL; END IF;
        IF OLD.user_id = subject THEN RETURN OLD; END IF;
    END IF;
    RAISE EXCEPTION ''Published legal documents and acceptance records are immutable'';
END;
';

CREATE OR REPLACE TRIGGER legal_release_immutable
    BEFORE UPDATE OR DELETE OR TRUNCATE ON legal_document_release
    FOR EACH STATEMENT EXECUTE FUNCTION protect_legal_history();
CREATE OR REPLACE TRIGGER legal_acceptance_immutable
    BEFORE UPDATE OR DELETE OR TRUNCATE ON legal_document_acceptance
    FOR EACH STATEMENT EXECUTE FUNCTION protect_legal_acceptance();
CREATE OR REPLACE TRIGGER legal_acceptance_erasure_scope
    BEFORE DELETE ON legal_document_acceptance
    FOR EACH ROW EXECUTE FUNCTION protect_legal_acceptance();
