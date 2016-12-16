TRUNCATE TABLE Fuente;
TRUNCATE TABLE Host;
TRUNCATE TABLE Descriptor;
ALTER TABLE Fuente AUTO_INCREMENT=1;
ALTER TABLE Host AUTO_INCREMENT=1;
ALTER TABLE Descriptor AUTO_INCREMENT=1;

#Migramos los sectores
DROP TABLE IF EXISTS Sector_dup;
CREATE TABLE IF NOT EXISTS Sector_dup LIKE Sector;
ALTER TABLE Sector_dup DROP PRIMARY KEY;
INSERT INTO Sector_dup (id, nombre) 
	SELECT taxonomy_term_data.tid,taxonomy_term_data.name 
	FROM taxonomy_term_data
        LEFT JOIN field_revision_field_sector
	ON taxonomy_term_data.tid = field_revision_field_sector.field_sector_tid
	WHERE taxonomy_term_data.vid = 4;

TRUNCATE TABLE Sector;
INSERT INTO Sector (id,nombre) SELECT id,nombre FROM Sector_dup GROUP BY id;
UPDATE Sector,taxonomy_term_hierarchy 
	SET idPadre=taxonomy_term_hierarchy.parent 
	WHERE taxonomy_term_hierarchy.tid = Sector.id;
DROP TABLE Sector_dup;	

#Quitamos los idPadre = 0
UPDATE Sector SET idPadre = NULL WHERE idPadre = 0;

#Consulta para revisar sectores
#SELECT t1.nombre AS lev1, t2.nombre as lev2, t3.nombre as lev3, t4.nombre as lev4
#	FROM Sector AS t1
#	LEFT JOIN Sector AS t2 ON t2.idPadre = t1.id
#	LEFT JOIN Sector AS t3 ON t3.idPadre = t2.id
#	LEFT JOIN Sector AS t4 ON t4.idPadre = t3.id;

#Migramos los tipos de organización
DROP TABLE IF EXISTS TipoOrganizacion_dup;
CREATE TABLE IF NOT EXISTS TipoOrganizacion_dup LIKE Sector;
ALTER TABLE TipoOrganizacion_dup DROP PRIMARY KEY;
INSERT INTO TipoOrganizacion_dup (id, nombre) 
	SELECT taxonomy_term_data.tid,taxonomy_term_data.name 
	FROM taxonomy_term_data
       	LEFT JOIN field_revision_field_tipo_de_organizaci_n
	ON taxonomy_term_data.tid = field_revision_field_tipo_de_organizaci_n.field_tipo_de_organizaci_n_tid
	WHERE taxonomy_term_data.vid = 5;

#Aquí habrá que poner un LEFT JOIN con taxonomy_term_data, para asegurarnos que vienen todos.

TRUNCATE TABLE TipoOrganizacion;
INSERT INTO TipoOrganizacion (id,nombre) SELECT id,nombre FROM TipoOrganizacion_dup GROUP BY id;
UPDATE TipoOrganizacion,taxonomy_term_hierarchy 
	SET idPadre=taxonomy_term_hierarchy.parent 
	WHERE taxonomy_term_hierarchy.tid = TipoOrganizacion.id;
DROP TABLE TipoOrganizacion_dup;	
UPDATE TipoOrganizacion SET idPadre = NULL WHERE idPadre = 0;

#Consulta para revisar Tipos de Organización
#SELECT t1.nombre AS lev1, t2.nombre as lev2, t3.nombre as lev3, t4.nombre as lev4
#	FROM TipoOrganizacion AS t1
#	LEFT JOIN TipoOrganizacion AS t2 ON t2.idPadre = t1.id
#	LEFT JOIN TipoOrganizacion AS t3 ON t3.idPadre = t2.id
#	LEFT JOIN TipoOrganizacion AS t4 ON t4.idPadre = t3.id;


#Migramos las localizaciones
DROP TABLE IF EXISTS Localizacion_dup;
CREATE TABLE IF NOT EXISTS Localizacion_dup LIKE Localizacion;
ALTER TABLE Localizacion_dup DROP PRIMARY KEY;
INSERT INTO Localizacion_dup (id, nombre) 
	SELECT taxonomy_term_data.tid,taxonomy_term_data.name 
	FROM taxonomy_term_data
        LEFT JOIN field_revision_field_tags
	ON taxonomy_term_data.tid = field_revision_field_tags.field_tags_tid
	WHERE taxonomy_term_data.vid = 2;

TRUNCATE TABLE Localizacion;
INSERT INTO Localizacion (id,nombre) SELECT id,nombre FROM Localizacion_dup GROUP BY id;
UPDATE Localizacion,taxonomy_term_hierarchy 
	SET idPadre=taxonomy_term_hierarchy.parent 
	WHERE taxonomy_term_hierarchy.tid = Localizacion.id;
DROP TABLE Localizacion_dup;	

#Quitamos los idPadre = 0
UPDATE Localizacion SET idPadre = NULL WHERE idPadre = 0;

#Consulta para revisar localizaciones
#SELECT t1.nombre AS lev1, t2.nombre as lev2, t3.nombre as lev3, t4.nombre as lev4
#	FROM Localizacion AS t1
#	LEFT JOIN Localizacion AS t2 ON t2.idPadre = t1.id
#	LEFT JOIN Localizacion AS t3 ON t3.idPadre = t2.id
#	LEFT JOIN Localizacion AS t4 ON t4.idPadre = t3.id;


#Migramos las Fuentes
ALTER TABLE Fuente ADD idSector INT NULL;
ALTER TABLE Fuente ADD idTipoOrganizacion INT NULL;
ALTER TABLE Fuente ADD idLocalizacion INT NULL;
DROP TABLE IF EXISTS fuente_dup;
CREATE TABLE IF NOT EXISTS fuente_dup LIKE Fuente;
#ALTER TABLE fuente_dup DROP INDEX URL_UNICA;
INSERT INTO fuente_dup (url,idSector,idTipoOrganizacion, idLocalizacion)
	SELECT field_revision_field_web.field_web_url as url, 
		field_revision_field_sector.field_sector_tid as idSector,
		field_revision_field_tipo_de_organizaci_n.field_tipo_de_organizaci_n_tid as idTipoOrganizacion,
		field_revision_field_tags.field_tags_tid as idLocalizacion
	FROM field_revision_field_web, field_revision_field_sector, field_revision_field_tipo_de_organizaci_n, field_revision_field_tags 
	WHERE 
	field_revision_field_sector.entity_id = field_revision_field_web.entity_id AND
	field_revision_field_tipo_de_organizaci_n.entity_id = field_revision_field_web.entity_id AND
	field_revision_field_tags.entity_id = field_revision_field_web.entity_id ;

TRUNCATE TABLE Fuente;
INSERT INTO Fuente (url, idSector, idTipoOrganizacion, idLocalizacion) SELECT url, idSector, idTipoOrganizacion, idLocalizacion FROM fuente_dup GROUP BY url;
DROP TABLE fuente_dup;

#Encontramos los Hosts
DROP TABLE IF EXISTS host_dup;
CREATE TABLE IF NOT EXISTS host_dup LIKE Host;
ALTER TABLE host_dup DROP INDEX URL_UNICA;
INSERT INTO host_dup (url,idSector,idTipoOrganizacion,idLocalizacion) SELECT 
       LEFT(SUBSTRING(url, 
       (CASE WHEN LOCATE('//',url)=0 
            THEN 5 
            ELSE  LOCATE('//',url) + 2
            END), 35),
       (CASE 
       WHEN LOCATE('/', SUBSTRING(url, LOCATE('//', url) + 2, 35))=0 
       THEN LENGTH(url) 
       else LOCATE('/', SUBSTRING(url, LOCATE('//', url) + 2, 35))- 1
       END)),
       idSector, idTipoOrganizacion, idLocalizacion
       FROM Fuente;
ALTER TABLE Fuente DROP idSector;
ALTER TABLE Fuente DROP idTipoOrganizacion;
ALTER TABLE Fuente DROP idLocalizacion;
TRUNCATE TABLE Host;
INSERT INTO Host (url, idSector,idTipoOrganizacion, idLocalizacion) SELECT url, idSector, idTipoOrganizacion, idLocalizacion FROM host_dup GROUP BY url;
DROP TABLE host_dup;

#Incluimos el idHost en la tabla Fuente
UPDATE Fuente 
LEFT JOIN Host ON 
       LEFT(SUBSTRING(Fuente.url, 
       (CASE WHEN LOCATE('//',Fuente.url)=0 
            THEN 5 
            ELSE  LOCATE('//',Fuente.url) + 2
            END), 35),
       (CASE 
       WHEN LOCATE('/', SUBSTRING(Fuente.url, LOCATE('//', Fuente.url) + 2, 35))=0 
       THEN LENGTH(Fuente.url) 
       else LOCATE('/', SUBSTRING(Fuente.url, LOCATE('//', Fuente.url) + 2, 35))- 1
       END)) = Host.url 
SET Fuente.idHost = Host.id;

#SELECT field_revision_field_web.field_web_url, taxonomy_term_data.name
#FROM field_revision_field_web, taxonomy_term_data, field_revision_field_sector
#WHERE field_revision_field_web.entity_id = field_revision_field_sector.entity_id
#AND taxonomy_term_data.tid = field_revision_field_sector.field_sector_tid;

## Corrección de URLs sin protocolo
UPDATE Fuente SET url = CONCAT('http://',url)
WHERE url NOT LIKE 'http%';

## Corrección de Localización inexistente (Sudáfrica) (http://www.nwu.ac.za)
UPDATE Host SET idLocalizacion = 180 WHERE idLocalizacion = 197;

