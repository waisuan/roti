CREATE OR REPLACE FUNCTION refresh_machine_document()
  RETURNS TRIGGER AS
$func$
BEGIN
   NEW."document" := coalesce(NEW."serialNumber", '') || ' ' || coalesce(NEW.customer, '') || ' ' || coalesce(NEW."state", '') || ' ' || coalesce(NEW."accountType", '') || ' ' || coalesce(NEW."model", '') || ' ' || coalesce(NEW.status, '') || ' ' || coalesce(NEW."brand", '') || ' ' || coalesce(NEW.district, '') || ' ' || coalesce(NEW."personInCharge", '') || ' ' || coalesce(NEW."reportedBy" , '') || ' ' || coalesce(NEW."tncDate"::text, '') || ' ' || coalesce(NEW."ppmDate"::text, '') || ' ' || coalesce(NEW."createdAt"::text, '') || ' ' || coalesce(NEW."updatedAt"::text, '');
   RETURN NEW;
END
$func$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS refresh_machine_document ON machines;

CREATE TRIGGER refresh_machine_document
BEFORE INSERT OR UPDATE ON machines
FOR EACH ROW EXECUTE PROCEDURE refresh_machine_document();