package delphos;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import delphos.iu.Delphos;

@Entity
public class Host {
	//
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	private String url;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn(name="idTipoOrganizacion", referencedColumnName="id")
	private TipoOrganizacion tipoOrganizacion;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn(name="idLocalizacion", referencedColumnName="id")
	private Localizacion localizacion;	
	//@ManyToOne (fetch = FetchType.LAZY)
	//@JoinColumn(name="idSector", referencedColumnName="id")
	//private Sector sector;
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "Host_Sector", joinColumns = { 
			@JoinColumn(name = "idHost", nullable = false, updatable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "idSector", 
					nullable = false, updatable = false) })
	private Set<Sector> sectores = new HashSet<Sector>();

	@Transient
	private final static Logger log = Logger.getLogger(Host.class);

	public Host(){
		//El constructor siguiente necesita el constructor por defecto.
	}
	public Host(String url){
		this.url = url;
		Session session = Delphos.getSession();
		
		Criteria crit = session.createCriteria(Host.class);
		crit.add(Restrictions.eq("url", url));
		List<Host> resultado = crit.list();
		
		if (!resultado.isEmpty())
			clone((Host)resultado.get(0));
	}
	public void clone(Host h){
		this.id = h.getId();
		this.url = h.getUrl();
		this.tipoOrganizacion = h.getTipoOrganizacion();
		this.localizacion = h.getLocalizacion();
		this.sectores = h.getSectores();
	}
	
	//Getters y Setters
	public Integer getId() {
		return id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public TipoOrganizacion getTipoOrganizacion() {
		return tipoOrganizacion;
	}
	public void setTipoOrganizacion(TipoOrganizacion tipoOrganizacion) {
		this.tipoOrganizacion = tipoOrganizacion;
	}
	public Localizacion getLocalizacion() {
		return localizacion;
	}
	public void setLocalizacion(Localizacion localizacion) {
		this.localizacion = localizacion;
	}
	public Set<Sector> getSectores() {
		return sectores;
	}
	public void setSectores(Set<Sector> sectores) {
		this.sectores = sectores;
	}
	public String verSectores() {
		String resultado = null;
		Iterator<Sector> it = this.sectores.iterator();
		if (it.hasNext())
			resultado = it.next().toString();
		while(it.hasNext()){
			resultado += ", " + it.next().toString();
		}
		return resultado;
	}
	
}
