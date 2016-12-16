-- Dumping data for table `Fuente`

INSERT INTO `Fuente` (`id`, `url`, `fechaActualizacion`, `idPadre`, `documento`, `titulo`, `descripcion`, `idioma`, `error`, `parseada`, `idHost`) VALUES
(1, 'http://www.fomento.gob.es', NULL, NULL, 'doc', 'Título 1', NULL, NULL, NULL, 1, 1),
(2, 'http://www.minetur.gob.es', NULL, NULL, 'doc', 'Título 2', NULL, NULL, NULL, 1, 2),
(3, 'http://www.19e37.com', NULL, NULL, 'doc', 'Título 3', NULL, NULL, NULL, 0, 3),
(4, 'http://es.wikipedia.org/wiki/Energía', NULL, NULL, 'doc', 'Título 4', NULL, NULL, NULL, 0, 4),
(5, 'http://www.agenciaandaluzadelaenergia.es/‎', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 5);

-- --------------------------------------------------------

-- Dumping data for table `Host`

INSERT INTO `Host` (`id`, `idTipoOrganizacion`, `idSector`, `idLocalizacion`, `url`) VALUES
(1, 1, 1 , 1, 'Host 1'),
(2, 3, 1 , 4, 'Host 2'),
(3, 4, 2 , 6, 'Host 3'),
(4, NULL, 3 , NULL, 'Host 4'),
(5, NULL, 5 , 2, 'Host 5'),
(6, NULL, 4 , NULL, 'Host 6'),
(7, NULL, NULL , NULL, 'Host 7');

-- --------------------------------------------------------

-- Dumping data for table `Peso`

INSERT INTO `Descriptor` (`id`, `descriptor`, `frecuencia`, `idFuente`, `multiplicador`, `peso`) VALUES
(1, 'desc', 3, 1, NULL, 1),
(2, 'energ', 5, 1, NULL, 2),
(3, 'energ', 7, 2, NULL, 5),
(4, 'construccion', 3, 2, NULL, 4),
(5, 'descB', 7, 1, NULL, 5),
(6, 'descB', 7, 2, NULL, 5),
(7, 'descC', 7, 3, NULL, 5),
(8, 'descC', 7, 5, NULL, 5),
(9, 'descD', 7, 2, NULL, 5);

-- Dumping data for table `Sector`

INSERT INTO `Sector` (`id`, `idPadre`, `nombre`, `descripcion`) VALUES
(1, NULL, 'Sector 1' , NULL),
(2, NULL, 'Sector 2' , NULL),
(3, 1, 'Sector 1.1' , NULL),
(4, 1, 'Sector 1.2' , NULL),
(5, 3, 'Sector 1.1.1' , NULL),
(6, NULL, 'Sector 3' , NULL);

-- --------------------------------------------------------

-- Dumping data for table `TipoOrganizacion`

INSERT INTO `TipoOrganizacion` (`id`, `idPadre`, `nombre`, `descripcion`) VALUES
(1, NULL, 'TipoOrg 1' , NULL),
(2, NULL, 'TipoOrg 2' , NULL),
(3, 1, 'TipoOrg 1.1' , NULL),
(4, 1, 'TipoOrg 1.2' , NULL),
(5, 3, 'TipoOrg 1.1.1' , NULL),
(6, NULL, 'TipoOrg 3' , NULL);

-- --------------------------------------------------------

-- Dumping data for table `Localizacion`

INSERT INTO `Localizacion` (`id`, `idPadre`, `nombre`, `descripcion`) VALUES
(1, NULL, 'Localización 1' , NULL),
(2, NULL, 'Localización 2' , NULL),
(3, 1, 'Localización 1.1' , NULL),
(4, 1, 'Localización 1.2' , NULL),
(5, 3, 'Localización 1.1.1' , NULL),
(6, NULL, 'Localización 3' , NULL);

-- --------------------------------------------------------
