package delphos;

import java.util.ArrayList;
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
import javax.swing.JFrame;
import javax.swing.JOptionPane;

@Entity
public class Tendencia implements Cloneable {
	public static final String SIN_FILTROS = "Quitar Filtros";
	public static final String TEXTO_LIBRE = "Texto Libre";
	public static final String PAIS = "País";
	public static final String SECTOR = "Sector";
	public static final String TIPO = "Tipo";
	public static final String SOLICITANTE = "Solicitante";
	public static final String INVENTOR = "Inventor";
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String terminoPrincipal;
	private String listaTerminosRelacionados;
	private Boolean indicadorLicitaciones;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "Tendencia_Licitacion_Sector", joinColumns = { @JoinColumn(name = "idTendencia", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "idLicitacion_Sector", nullable = false, updatable = false) })
	private Set<Licitacion_Sector> listaLicitacionSector = new HashSet<Licitacion_Sector>();
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "Tendencia_Licitacion_Localizacion", joinColumns = { @JoinColumn(name = "idTendencia", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "idLicitacion_Localizacion", nullable = false, updatable = false) })
	private Set<Licitacion_Localizacion> listaLicitacionLocalizacion = new HashSet<Licitacion_Localizacion>();
	private String licitacionTipo;
	private String licitacionEntidadSolicitante;
	private Boolean indicadorPatentes;
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "Tendencia_Patente_Sector", joinColumns = { @JoinColumn(name = "idTendencia", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "idPatente_Sector", nullable = false, updatable = false) })
	private Set<Patente_Sector> listaPatenteSector = new HashSet<Patente_Sector>();
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "Tendencia_Patente_Localizacion", joinColumns = { @JoinColumn(name = "idTendencia", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "idPatente_Localizacion", nullable = false, updatable = false) })
	private Set<Patente_Localizacion> listaPatenteLocalizacion = new HashSet<Patente_Localizacion>();
	private String patenteInventor;
	private String patenteSolicitante;
	private Boolean indicadorDocs;
	private Double incUltimoMes;
	private Double incUltimos3Meses;
	private Double incUltimos6Meses;
	private Double incUltimoAnio;
	private String filtroPrincipalLicitaciones;
	private String filtroPrincipalPatentes;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTerminoPrincipal() {
		return terminoPrincipal;
	}

	public void setTerminoPrincipal(String terminoPrincipal) {
		this.terminoPrincipal = terminoPrincipal;
	}

	public String getListaTerminosRelacionados() {
		return listaTerminosRelacionados;
	}

	public void setListaTerminosRelacionados(String listaTerminosRelacionados) {
		this.listaTerminosRelacionados = listaTerminosRelacionados;
	}

	public Boolean getIndicadorLicitaciones() {
		return indicadorLicitaciones;
	}

	public void setIndicadorLicitaciones(Boolean indicadorLicitaciones) {
		this.indicadorLicitaciones = indicadorLicitaciones;
	}

	public Set<Licitacion_Sector> getListaLicitacionSector() {
		return listaLicitacionSector;
	}

	public void setListaLicitacionSector(Set<Licitacion_Sector> listaLicitacionSector) {
		this.listaLicitacionSector = listaLicitacionSector;
	}

	public Set<Licitacion_Localizacion> getListaLicitacionLocalizacion() {
		return listaLicitacionLocalizacion;
	}

	public void setListaLicitacionLocalizacion(Set<Licitacion_Localizacion> listaLicitacionLocalizacion) {
		this.listaLicitacionLocalizacion = listaLicitacionLocalizacion;
	}

	public String getLicitacionEntidadSolicitante() {
		return licitacionEntidadSolicitante;
	}

	public void setLicitacionEntidadSolicitante(String licitacionEntidadSolicitante) {
		this.licitacionEntidadSolicitante = licitacionEntidadSolicitante;
	}

	public Boolean getIndicadorPatentes() {
		return indicadorPatentes;
	}

	public void setIndicadorPatentes(Boolean indicadorPatentes) {
		this.indicadorPatentes = indicadorPatentes;
	}

	public Set<Patente_Sector> getListaPatenteSector() {
		return listaPatenteSector;
	}

	public void setListaPatenteSector(Set<Patente_Sector> listaPatenteSector) {
		this.listaPatenteSector = listaPatenteSector;
	}

	public Set<Patente_Localizacion> getListaPatenteLocalizacion() {
		return listaPatenteLocalizacion;
	}

	public void setListaPatenteLocalizacion(Set<Patente_Localizacion> listaPatenteLocalizacion) {
		this.listaPatenteLocalizacion = listaPatenteLocalizacion;
	}

	public String getPatenteInventor() {
		return patenteInventor;
	}

	public void setPatenteInventor(String patenteInventor) {
		this.patenteInventor = patenteInventor;
	}

	public String getPatenteSolicitante() {
		return patenteSolicitante;
	}

	public void setPatenteSolicitante(String patenteSolicitante) {
		this.patenteSolicitante = patenteSolicitante;
	}

	public Boolean getIndicadorDocs() {
		return indicadorDocs;
	}

	public void setIndicadorDocs(Boolean indicadorDocs) {
		this.indicadorDocs = indicadorDocs;
	}

	public Double getIncUltimoMes() {
		return incUltimoMes;
	}

	public void setIncUltimoMes(Double incUltimoMes) {
		this.incUltimoMes = incUltimoMes;
	}

	public Double getIncUltimos3Meses() {
		return incUltimos3Meses;
	}

	public void setIncUltimos3Meses(Double incUltimos3Meses) {
		this.incUltimos3Meses = incUltimos3Meses;
	}

	public Double getIncUltimos6Meses() {
		return incUltimos6Meses;
	}

	public void setIncUltimos6Meses(Double incUltimos6Meses) {
		this.incUltimos6Meses = incUltimos6Meses;
	}

	public Double getIncUltimoAnio() {
		return incUltimoAnio;
	}

	public void setIncUltimoAnio(Double incUltimoAnio) {
		this.incUltimoAnio = incUltimoAnio;
	}

	public String getLicitacionTipo() {
		return licitacionTipo;
	}

	public void setLicitacionTipo(String licitacionTipo) {
		this.licitacionTipo = licitacionTipo;
	}

	public String getFiltroPrincipalLicitaciones() {
		return filtroPrincipalLicitaciones;
	}

	public void setFiltroPrincipalLicitaciones(String filtroPrincipalLicitaciones) {
		this.filtroPrincipalLicitaciones = filtroPrincipalLicitaciones;
	}

	public String getFiltroPrincipalPatentes() {
		return filtroPrincipalPatentes;
	}

	public void setFiltroPrincipalPatentes(String filtroPrincipalPatentes) {
		this.filtroPrincipalPatentes = filtroPrincipalPatentes;
	}

	public Object clone() {
		Tendencia obj = null;
		try {
			obj = (Tendencia) super.clone();
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
		}

		// obj.listaLicitacionLocalizacion = (HashSet<Licitacion_Localizacion>)
		// ((HashSet) obj.listaLicitacionLocalizacion)
		// .clone();
		// obj.listaLicitacionSector = (HashSet<Licitacion_Sector>) ((HashSet)
		// obj.listaLicitacionSector)
		// .clone();
		// obj.listaPatenteLocalizacion = (HashSet<Patente_Localizacion>)
		// ((HashSet) obj.listaPatenteLocalizacion)
		// .clone();
		// obj.listaPatenteSector = (HashSet<Patente_Sector>) ((HashSet)
		// obj.listaPatenteSector)
		// .clone();

		return obj;
	}

	public Tendencia crearClonParaTotales() throws Exception {
		// Calculamos la tendenciaClon para los totales
		Tendencia tendenciaClon = (Tendencia) this.clone();

		if (this.getFiltroPrincipalLicitaciones() != null)
			switch (this.getFiltroPrincipalLicitaciones()) {
			case Tendencia.TEXTO_LIBRE:
				tendenciaClon.setTerminoPrincipal("");
				break;
			case Tendencia.SIN_FILTROS:
				tendenciaClon.setListaLicitacionLocalizacion(null);
				tendenciaClon.setListaLicitacionSector(null);
				tendenciaClon.setLicitacionEntidadSolicitante(null);
				break;
			case Tendencia.PAIS:
				tendenciaClon.setListaLicitacionLocalizacion(new HashSet<Licitacion_Localizacion>());
				break;
			case Tendencia.SECTOR:
				tendenciaClon.setListaLicitacionSector(new HashSet<Licitacion_Sector>());
				break;
			case Tendencia.TIPO:
				tendenciaClon.setLicitacionTipo(null);
				break;
			case Tendencia.SOLICITANTE:
				tendenciaClon.setLicitacionEntidadSolicitante("");
				break;
			default:
				throw new Exception("Tendencia sin Filtro Principal de Licitaciones.");
			}

		if (this.getFiltroPrincipalPatentes() != null)
			switch (this.getFiltroPrincipalPatentes()) {
			case Tendencia.TEXTO_LIBRE:
				tendenciaClon.setTerminoPrincipal("");
				break;
			case Tendencia.SIN_FILTROS:
				tendenciaClon.setListaPatenteLocalizacion(null);
				tendenciaClon.setListaPatenteSector(null);
				tendenciaClon.setPatenteInventor(null);
				tendenciaClon.setPatenteSolicitante(null);
				break;
			case Tendencia.PAIS:
				tendenciaClon.setListaPatenteLocalizacion(new HashSet<Patente_Localizacion>());
				break;
			case Tendencia.SECTOR:
				tendenciaClon.setListaPatenteSector(new HashSet<Patente_Sector>());
				break;
			case Tendencia.INVENTOR:
				tendenciaClon.setPatenteInventor("");
				break;
			case "Solicitante":
				tendenciaClon.setPatenteSolicitante("");
				break;
			default:
				throw new Exception("Tendencia sin Filtro Principal en Patentes.");
			}

		return tendenciaClon;
	}

	@Override
	public String toString() {
		String sTendencia = "";
		if (!terminoPrincipal.isEmpty())
			sTendencia += terminoPrincipal.substring(0, 1).toUpperCase() + terminoPrincipal.substring(1);
		if (indicadorLicitaciones) {
			if (sTendencia.length() > 0)
				sTendencia += "\n";
			if (listaLicitacionSector != null) {
				sTendencia += "Licitacion: [";
				if (!listaLicitacionSector.isEmpty()) {
					sTendencia += "Sectores: ";
					for (Licitacion_Sector ls : listaLicitacionSector)
						sTendencia += ls.getNombre() + ", ";
					if (listaLicitacionSector.size() > 0)
						sTendencia = sTendencia.substring(0, sTendencia.length() - 2);
					sTendencia += ";";
				}
				if (!listaLicitacionLocalizacion.isEmpty()) {
					sTendencia += " Países: ";
					for (Licitacion_Localizacion ll : listaLicitacionLocalizacion)
						sTendencia += ll.getNombre() + ", ";
					if (listaLicitacionLocalizacion.size() > 0)
						sTendencia = sTendencia.substring(0, sTendencia.length() - 2);
					sTendencia += ";";
				}
				if (licitacionTipo != null)
					sTendencia += " Tipo: " + licitacionTipo;
				if (!licitacionEntidadSolicitante.isEmpty())
					sTendencia += " E.Emisora: " + licitacionEntidadSolicitante;
				sTendencia += "]";
			}
		}
		if (indicadorPatentes) {
			if (sTendencia.length() > 0)
				sTendencia += "\n";
			if (listaPatenteSector != null) {
				sTendencia += "Patente: [";
				if (!listaPatenteSector.isEmpty()) {
					sTendencia += "Sectores: ";
					for (Patente_Sector ls : listaPatenteSector)
						sTendencia += ls.getNombre() + ", ";
					if (listaPatenteSector.size() > 0)
						sTendencia = sTendencia.substring(0, sTendencia.length() - 2);
					sTendencia += ";";
				}
				if (!listaPatenteLocalizacion.isEmpty()) {
					sTendencia += " Países: ";
					for (Patente_Localizacion ll : listaPatenteLocalizacion)
						sTendencia += ll.getNombre() + ", ";
					if (listaPatenteLocalizacion.size() > 0)
						sTendencia = sTendencia.substring(0, sTendencia.length() - 2);
					sTendencia += ";";
				}
				if (!patenteInventor.isEmpty())
					sTendencia += " Inventor: " + patenteInventor;
				if (!patenteSolicitante.isEmpty())
					sTendencia += " Solicitante: " + patenteSolicitante;
				sTendencia += "]";
			}
		}

		return sTendencia;
	}

	public void establecerFiltrosPrincipales() {
		int numFiltros;

		if (this.getIndicadorLicitaciones()) {
			numFiltros = 0;
			if (!this.getListaLicitacionLocalizacion().isEmpty())
				numFiltros++;
			if (!this.getListaLicitacionSector().isEmpty())
				numFiltros++;
			if (this.getLicitacionTipo() != null)
				numFiltros++;
			if (!this.getLicitacionEntidadSolicitante().isEmpty())
				numFiltros++;

			if (!this.getTerminoPrincipal().isEmpty() && numFiltros == 0)
				this.setFiltroPrincipalLicitaciones(Tendencia.TEXTO_LIBRE);
			else if (this.getTerminoPrincipal().isEmpty() && numFiltros == 1)
				this.setFiltroPrincipalLicitaciones(Tendencia.SIN_FILTROS);
		}
		if (this.getIndicadorPatentes()) {
			numFiltros = 0;
			if (!this.getListaPatenteLocalizacion().isEmpty())
				numFiltros++;
			if (!this.getListaPatenteSector().isEmpty())
				numFiltros++;
			if (!this.getPatenteInventor().isEmpty())
				numFiltros++;
			if (!this.getPatenteSolicitante().isEmpty())
				numFiltros++;

			if (!this.getTerminoPrincipal().isEmpty() && numFiltros == 0)
				this.setFiltroPrincipalPatentes(Tendencia.TEXTO_LIBRE);
			else if (this.getTerminoPrincipal().isEmpty() && numFiltros == 1)
				this.setFiltroPrincipalPatentes(Tendencia.SIN_FILTROS);
		}
	}

	public double getIncremento(String nombrePeriodo) {
		double incremento = 0;
		switch (nombrePeriodo) {
		case AvisoTendencia.ULTIMO_ANIO:
			incremento = this.getIncUltimoAnio();
			break;
		case AvisoTendencia.ULTIMO_MES:
			incremento = this.getIncUltimoMes();
			break;
		case AvisoTendencia.ULTIMOS_3MESES:
			incremento = this.getIncUltimos3Meses();
			break;
		case AvisoTendencia.ULTIMOS_6MESES:
			incremento = this.getIncUltimos6Meses();
			break;
		}

		return incremento;
	}

	public String getTextoLibre() {
		String textoLibre = this.terminoPrincipal;
		if (this.listaTerminosRelacionados != null) 
			if (!this.listaTerminosRelacionados.isEmpty()){
				String[] listaTR = this.listaTerminosRelacionados.split(",");
				for (String palabra : listaTR)
					textoLibre += " OR \"" + palabra + "\"";
			}
		return textoLibre;
	}

}
