ALTER TABLE machines ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 1;

ALTER TABLE maintenance ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 1;

CREATE OR REPLACE FUNCTION version_checker()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.version != (OLD.version + 1) THEN
        RAISE EXCEPTION 'Version (%) is not in sequence. Updated record looks outdated.', NEW.version;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS verify_version ON machines;
CREATE TRIGGER verify_version
BEFORE UPDATE ON machines
FOR EACH ROW EXECUTE PROCEDURE version_checker();

DROP TRIGGER IF EXISTS verify_version ON maintenance;
CREATE TRIGGER verify_version
BEFORE UPDATE ON maintenance
FOR EACH ROW EXECUTE PROCEDURE version_checker();