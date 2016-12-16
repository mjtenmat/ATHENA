package delphos;

import java.net.URL;

public class DocumentoWeb {
	private String titulo;
	private URL url;
	private String extracto;
	private int idFuente;
	private String localizacion;
	private String sector;
	private String tipoOrgnizacion;

	
	public DocumentoWeb(String titulo, URL url, String extracto, int idFuente, String localizacion, String sector, String tipoOrgnizacion) {
		this.titulo = titulo;
		this.url = url;
		this.extracto = extracto;
		this.idFuente = idFuente;
		this.localizacion = localizacion;
		this.sector = sector;
		this.tipoOrgnizacion = tipoOrgnizacion;
	}
	public String getLocalizacion() {
		return localizacion;
	}
	public void setLocalizacion(String localizacion) {
		this.localizacion = localizacion;
	}
	public String getSector() {
		return sector;
	}
	public void setSector(String sector) {
		this.sector = sector;
	}
	public String getTipoOrgnizacion() {
		return tipoOrgnizacion;
	}
	public void setTipoOrgnizacion(String tipoOrgnizacion) {
		this.tipoOrgnizacion = tipoOrgnizacion;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public URL getUrl() {
		return url;
	}
	public void setUrl(URL url) {
		this.url = url;
	}
	public String getExtracto() {
		return extracto;
	}
	public void setExtracto(String extracto) {
		this.extracto = extracto;
	}
	public int getIdFuente() {
		return idFuente;
	}
	public void setIdFuente(int idFuente) {
		this.idFuente = idFuente;
	}
}
