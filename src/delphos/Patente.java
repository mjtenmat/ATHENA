package delphos;

import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;


//@Entity
public class Patente implements Comparable{
	//@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	private String titulo;
	private String inventor;
	private String solicitante;
	private String localizacion;
	private URL url;
	private Date fechaPublicacion;
	private String resumen;
	private String docId;
	//@ManyToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
	//@JoinTable(name = "Patente_CPI", joinColumns = { 
	//		@JoinColumn(name = "idPatente", nullable = false, updatable = false) }, 
	//		inverseJoinColumns = { @JoinColumn(name = "idCPI", 
	//				nullable = false, updatable = false) })
	private Set<CPI> cpi = new HashSet<CPI>();
	private String listaCPI;
	
	//Atributos utilizados por OPSCrawler
	//@Transient
	String documentIdType, docNumber, kind;
	
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getInventor() {
		return inventor;
	}
	public void setInventor(String inventor) {
		this.inventor = inventor;
	}
	public String getSolicitante() {
		return solicitante;
	}
	public void setSolicitante(String solicitante) {
		this.solicitante = solicitante;
	}
	public URL getUrl() {
		return url;
	}
	public void setUrl(URL url) {
		this.url = url;
	}
	public Date getFechaPublicacion() {
		return fechaPublicacion;
	}
	public void setFechaPublicacion(Date fechaPublicacion) {
		this.fechaPublicacion = fechaPublicacion;
	}
	public Set<CPI> getCpi() {
		return cpi;
	}
	public void setCpi(Set<CPI> cpi) {
		this.cpi = cpi;
	}
	public Integer getId() {
		return id;
	}
	public String getLocalizacion() {
		return localizacion;
	}
	public void setLocalizacion(String localizacion) {
		this.localizacion = localizacion;
	}
	
	public String getResumen() {
		return resumen;
	}
	public void setResumen(String resumen) {
		this.resumen = resumen;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	
	public void setListaCPI(String lista){
		this.listaCPI = lista;
	}
	public String getListaCPI(){
		return this.listaCPI;
	}
	
	@Override
	public String toString() {
		return "Patente [id=" + id + ", titulo=" + titulo + ", inventor=" + inventor + ", solicitante=" + solicitante + ", localizacion=" + localizacion + ", url=" + url + ", fechaPublicacion=" + fechaPublicacion + ", resumen=" + resumen + ", cpi=" + cpi + ", documentIdType=" + documentIdType + ", docNumber=" + docNumber + ", kind=" + kind + "]";
	}
	@Override
	public int compareTo(Object o) {
		//Por fecha descendente
		return ((Patente)o).fechaPublicacion.compareTo(this.fechaPublicacion);
	}
	
}
