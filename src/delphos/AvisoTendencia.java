package delphos;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import delphos.iu.Delphos;

@Entity
public class AvisoTendencia {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	private Date fechaRegistro;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn(name = "idTendencia", referencedColumnName = "id")
	private Tendencia tendencia;
	//private Double variacion;
	private Double pcPeriodoAnterior;
	private Double pcPeriodoActual;
	private String tipo;
	private String periodo;
	private boolean revisado;
	
	public static final String LICITACIONES = "Licitaciones";
	public static final String PATENTES = "Patentes";
	
	public static final String ULTIMO_ANIO = "Último Año";
	public static final String ULTIMO_MES = "Último Mes";
	public static final String ULTIMOS_3MESES = "Último Trimestre";
	public static final String ULTIMOS_6MESES = "Último Semestre";
	
	public AvisoTendencia(){
	}
	
	public AvisoTendencia(Tendencia tendencia, Double pcPeriodoAnterior, Double pcPeriodoActual, String tipo, String periodo) {
		this.tendencia = tendencia;
		this.pcPeriodoAnterior = pcPeriodoAnterior;
		this.pcPeriodoActual = pcPeriodoActual;
		this.tipo = tipo;
		this.periodo = periodo;
		this.fechaRegistro = new Date();
		
		//La guardamos en la base de datos
		Delphos.getSession().beginTransaction();
		Delphos.getSession().save(this);
		Delphos.getSession().getTransaction().commit();
		
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Tendencia getTendencia() {
		return tendencia;
	}
	public void setTendencia(Tendencia tendencia) {
		this.tendencia = tendencia;
	}
	
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getPeriodo() {
		return periodo;
	}
	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}
	public Date getFechaRegistro() {
		return fechaRegistro;
	}
	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}
	
	public Double getPcPeriodoAnterior() {
		return pcPeriodoAnterior;
	}

	public void setPcPeriodoAnterior(Double pcPeriodoAnterior) {
		this.pcPeriodoAnterior = pcPeriodoAnterior;
	}

	public Double getPcPeriodoActual() {
		return pcPeriodoActual;
	}

	public void setPcPeriodoActual(Double pcPeriodoActual) {
		this.pcPeriodoActual = pcPeriodoActual;
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YYYY");
		Double variacion = pcPeriodoActual - pcPeriodoAnterior;
		NumberFormat formatter = new DecimalFormat("#0.0000");
	    
		return sdf.format(this.fechaRegistro) + ": El término \"" + this.tendencia.getTerminoPrincipal() + "\" ha tenido una variación "
				+ " en " + this.tipo + " de "  
				+ formatter.format(variacion) + "% (de " + formatter.format(this.pcPeriodoAnterior) + "% a " + formatter.format(this.pcPeriodoActual)  + "%) " 
				+ " durante el " + this.periodo + " que es superior al umbral establecido."; 				
	}

	public boolean isRevisado() {
		return revisado;
	}
}
