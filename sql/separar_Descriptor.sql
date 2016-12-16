RENAME TABLE Descriptor TO Peso;

CREATE TABLE IF NOT EXISTS `Descriptor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `texto` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `texto_unico` ( `texto` )
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

INSERT INTO Descriptor (texto) SELECT DISTINCT descriptor FROM Peso;

ALTER TABLE Peso ADD idDescriptor int(11) NOT NULL;

UPDATE Peso SET idDescriptor = (SELECT id FROM Descriptor WHERE texto = descriptor);

ALTER TABLE Peso DROP INDEX Descriptor_Fuente_Unica;

ALTER TABLE Peso DROP COLUMN descriptor;

