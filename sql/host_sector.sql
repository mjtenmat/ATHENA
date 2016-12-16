/*
SELECT Fuente.id, Host.id, Fuente.url, Host.url, field_revision_field_web.field_web_url as url2, field_revision_field_sector.field_sector_tid as idSector
FROM Fuente, Host, field_revision_field_web, field_revision_field_sector 
WHERE Fuente.idHost = Host.id AND Fuente.profundidad = 0 AND Fuente.url = field_revision_field_web.field_web_url AND field_revision_field_sector.entity_id = field_revision_field_web.entity_id 
ORDER BY url2;
*/
TRUNCATE TABLE Host_Sector;

INSERT INTO Host_Sector (idHost, idSector) 
	SELECT Host.id, field_revision_field_sector.field_sector_tid
	FROM Fuente, Host, field_revision_field_web, field_revision_field_sector 
	WHERE Fuente.idHost = Host.id AND Fuente.profundidad = 0 AND Fuente.url = field_revision_field_web.field_web_url AND field_revision_field_sector.entity_id = field_revision_field_web.entity_id;


