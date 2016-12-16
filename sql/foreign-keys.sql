-- ALTER TABLE Fuente ADD FOREIGN KEY (idPadre) REFERENCES Fuente(id);
-- ALTER TABLE Fuente ADD FOREIGN KEY (idHost) REFERENCES Host(id);
-- ALTER TABLE Fuente_Descriptor_Frecuencia ADD FOREIGN KEY (idFuente) REFERENCES Fuente(id);
-- ALTER TABLE Fuente_Descriptor_Frecuencia ADD FOREIGN KEY (idDescriptor) REFERENCES Descriptor(id);
-- ALTER TABLE Host ADD FOREIGN KEY (idSector) REFERENCES Sector(id);
-- ALTER TABLE Host ADD FOREIGN KEY (idTipoOrganizacion) REFERENCES TipoOrganizacion(id);
-- ALTER TABLE Host ADD FOREIGN KEY (idLocalizacion) REFERENCES Localizacion(id);
-- ALTER TABLE Host_Sector ADD FOREIGN KEY (idHost) REFERENCES Host(id);
-- ALTER TABLE Host_Sector ADD FOREIGN KEY (idSector) REFERENCES Sector(id);
-- ALTER TABLE Licitacion ADD FOREIGN KEY (idLocalizacion) REFERENCES Licitacion_Localizacion(id);
-- ALTER TABLE Licitacion ADD FOREIGN KEY (idTipoLicitacion) REFERENCES TipoLicitacion(id);
-- ALTER TABLE Licitacion_CPV ADD FOREIGN KEY (idLicitacion) REFERENCES Licitacion(id);
-- ALTER TABLE Licitacion_CPV ADD FOREIGN KEY (idCPV) REFERENCES CPV(id);
-- ALTER TABLE Licitacion_Sector ADD FOREIGN KEY (idLicitacion) REFERENCES Licitacion(id);
-- ALTER TABLE Licitacion_Sector ADD FOREIGN KEY (idSector) REFERENCES Sector(id);
-- ALTER TABLE Localizacion ADD FOREIGN KEY (idPadre) REFERENCES Localizacion(id);
-- ALTER TABLE Peso ADD FOREIGN KEY (idDescriptor) REFERENCES Descriptor(id);
-- ALTER TABLE Sector ADD FOREIGN KEY (idPadre) REFERENCES Sector(id);
-- ALTER TABLE TipoOrganizacion ADD FOREIGN KEY (idPadre) REFERENCES TipoOrganizacion(id);
-- ALTER TABLE CPI  ADD FOREIGN KEY (idPatente_Sector) REFERENCES Patente_Sector(id);
-- ALTER TABLE Patente_CPI  ADD FOREIGN KEY (idPatente) REFERENCES Patente(id);
-- ALTER TABLE Patente_Sector  ADD FOREIGN KEY (idPadre) REFERENCES Patente_Sector(id);
ALTER TABLE Patente_Sector_CPI  ADD FOREIGN KEY (idPatente_Sector) REFERENCES Patente_Sector(id);
-- ALTER TABLE Patente_Sector_CPI  ADD FOREIGN KEY (idCPI) REFERENCES CPI(id);



