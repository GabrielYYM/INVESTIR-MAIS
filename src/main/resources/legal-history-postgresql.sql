CREATE OR REPLACE FUNCTION protect_legal_history() RETURNS trigger LANGUAGE plpgsql AS
'BEGIN RAISE EXCEPTION ''Published legal documents and acceptance records are immutable''; END;';

CREATE OR REPLACE TRIGGER legal_release_immutable
    BEFORE UPDATE OR DELETE OR TRUNCATE ON legal_document_release
    FOR EACH STATEMENT EXECUTE FUNCTION protect_legal_history();
CREATE OR REPLACE TRIGGER legal_acceptance_immutable
    BEFORE UPDATE OR DELETE OR TRUNCATE ON legal_document_acceptance
    FOR EACH STATEMENT EXECUTE FUNCTION protect_legal_history();
