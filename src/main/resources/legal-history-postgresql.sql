CREATE OR REPLACE FUNCTION protect_legal_history() RETURNS trigger LANGUAGE plpgsql AS
'BEGIN RAISE EXCEPTION ''Published legal documents and acceptance records are immutable''; END;';

-- Account deletion is logical. Acceptance evidence remains immutable, including after deletion.
CREATE OR REPLACE FUNCTION protect_legal_acceptance() RETURNS trigger LANGUAGE plpgsql AS
'BEGIN RAISE EXCEPTION ''Published legal documents and acceptance records are immutable''; END;';

CREATE OR REPLACE TRIGGER legal_release_immutable
    BEFORE UPDATE OR DELETE OR TRUNCATE ON legal_document_release
    FOR EACH STATEMENT EXECUTE FUNCTION protect_legal_history();
CREATE OR REPLACE TRIGGER legal_acceptance_immutable
    BEFORE UPDATE OR DELETE OR TRUNCATE ON legal_document_acceptance
    FOR EACH STATEMENT EXECUTE FUNCTION protect_legal_acceptance();
CREATE OR REPLACE TRIGGER legal_acceptance_erasure_scope
    BEFORE DELETE ON legal_document_acceptance
    FOR EACH ROW EXECUTE FUNCTION protect_legal_acceptance();


CREATE OR REPLACE FUNCTION protect_audit_access_log() RETURNS trigger LANGUAGE plpgsql AS
'BEGIN RAISE EXCEPTION ''Audit access logs are append-only''; END;';
CREATE OR REPLACE TRIGGER audit_access_log_immutable BEFORE UPDATE OR DELETE OR TRUNCATE ON audit_access_log
    FOR EACH STATEMENT EXECUTE FUNCTION protect_audit_access_log();
