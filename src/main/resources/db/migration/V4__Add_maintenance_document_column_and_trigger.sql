ALTER TABLE maintenance ADD COLUMN IF NOT EXISTS document TEXT;

CREATE OR REPLACE FUNCTION refresh_maintenance_document()
  RETURNS TRIGGER AS
$func$
BEGIN
   NEW."document" := coalesce(NEW."workOrderNumber", '') || ' ' || coalesce(NEW."workOrderDate"::text, '') || ' ' || coalesce(NEW."reportedBy" , '') || ' ' || coalesce(NEW."workOrderType", '') || ' ' || coalesce(NEW."createdAt"::text, '') || ' ' || coalesce(NEW."updatedAt"::text, '');
   RETURN NEW;
END
$func$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS refresh_maintenance_document ON maintenance;

CREATE TRIGGER refresh_maintenance_document
BEFORE INSERT OR UPDATE ON maintenance
FOR EACH ROW EXECUTE PROCEDURE refresh_maintenance_document();