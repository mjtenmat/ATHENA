CREATE TABLE IF NOT EXISTS `Documento` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `texto` longtext NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

INSERT INTO Documento (id, texto) SELECT id, documento FROM Fuente WHERE documento is not NULL;
ALTER TABLE Fuente ADD COLUMN idDocumento INT NULL;
UPDATE Fuente SET idDocumento = id WHERE documento IS NOT NULL;
ALTER TABLE Fuente DROP COLUMN documento;
