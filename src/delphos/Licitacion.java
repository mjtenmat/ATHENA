package delphos;

import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

//@Entity
public class Licitacion {
	//@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	private String titulo;
	private String entidadEmisora;
	//@ManyToOne (fetch = FetchType.LAZY)
	//@JoinColumn(name="idLocalizacion", referencedColumnName="id")
	//private Licitacion_Localizacion localizacion;
	private String localizacion;
	private URL url;
	private String fechaPublicacion;
	private String tipoDocumento;
	private String resumen;
	//@ManyToOne (fetch = FetchType.LAZY)
	//@JoinColumn(name="idTipoLicitacion", referencedColumnName="id")
	private TipoLicitacion tipoLicitacion;
	//@ManyToMany(fetch = FetchType.LAZY)
	//@JoinTable(name = "Licitacion_CPV", joinColumns = { 
//			@JoinColumn(name = "idLicitacion", nullable = false, updatable = false) }, 
//			inverseJoinColumns = { @JoinColumn(name = "idCPV", 
//					nullable = false, updatable = false) })
	String listaCPV;
	//private Set<CPV> cpv = new HashSet<CPV>();
//	@ManyToMany(fetch = FetchType.LAZY)
//	@JoinTable(name = "Licitacion_Sector", joinColumns = { 
//			@JoinColumn(name = "idLicitacion", nullable = false, updatable = false) }, 
//			inverseJoinColumns = { @JoinColumn(name = "idSector", 
//					nullable = false, updatable = false) })
	private Set<Sector> sectores = new HashSet<Sector>();

	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getEntidadEmisora() {
		return entidadEmisora;
	}
	public void setEntidadEmisora(String entidadEmisora) {
		this.entidadEmisora = entidadEmisora;
	}
	public String getLocalizacion() {
		return localizacion;
	}
	public void setLocalizacion(String localizacion) {
		this.localizacion = localizacion;
	}
	public URL getUrl() {
		return url;
	}
	public void setUrl(URL url) {
		this.url = url;
	}
	public String getFechaPublicacion() {
		return fechaPublicacion;
	}
	public void setFechaPublicacion(String fechaPublicacion) {
		this.fechaPublicacion = fechaPublicacion;
	}
	public String getResumen() {
		return resumen;
	}
	public void setResumen(String resumen) {
		this.resumen = resumen;
	}
	public TipoLicitacion getTipoLicitacion() {
		return tipoLicitacion;
	}
	public void setTipoLicitacion(TipoLicitacion tipoLicitacion) {
		this.tipoLicitacion = tipoLicitacion;
	}
	public void setListaCPV(String lista){
		this.listaCPV = lista;
	}
	public String getListaCPV(){
		return this.listaCPV;
	}
	public String getTipoDocumento(){
		return tipoDocumento;
	}
	public void setTipoDocumento(String tipoDocumento){
		this.tipoDocumento = tipoDocumento;
	}

}
