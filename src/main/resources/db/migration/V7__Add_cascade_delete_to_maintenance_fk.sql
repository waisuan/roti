ALTER TABLE maintenance
DROP CONSTRAINT fk_maintenance_serialnumber_serialnumber;

ALTER TABLE maintenance
ADD CONSTRAINT fk_maintenance_serialnumber_serialnumber FOREIGN KEY ("serialNumber") REFERENCES machines("serialNumber") ON DELETE CASCADE;