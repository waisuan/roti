ALTER TABLE maintenance
DROP CONSTRAINT maintenance_workordernumber_unique;

ALTER TABLE maintenance
ADD CONSTRAINT maintenance_workordernumber_unique UNIQUE ("serialNumber", "workOrderNumber");