DROP TABLE IF EXISTS Licitacion_Localizacion;

CREATE TABLE IF NOT EXISTS `Licitacion_Localizacion` (
`id` int(11) NOT NULL,
  `codPais` varchar(5) DEFAULT NULL,
  `pais` varchar(255) NOT NULL,
  `ciudad` varchar(255) NOT NULL
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

ALTER TABLE Licitacion_Localizacion ADD idPais INT NOT NULL ;

INSERT INTO Licitacion_Localizacion (ciudad, idPais) 
	SELECT nombre, idPadre 
	FROM Localizacion
	WHERE id > 1213;
	
UPDATE Licitacion_Localizacion
	SET pais = (SELECT nombre FROM Localizacion
				WHERE id = Licitacion_Localizacion.idPais);
				
ALTER TABLE Licitacion_Localizacion DROP idPais;