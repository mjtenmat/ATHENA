package delphos;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.DoubleType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;

import delphos.iu.Delphos;

public class Resultado {
	private Fuente fuente;
	private Double similitud;
	private Double cobertura;
	private boolean relevante;

	public Resultado(Fuente fuente){
		this.fuente = fuente;
		this.similitud = 0.0;
		this.relevante = false;
	}
	
	public Resultado(int idFuente, double cobertura, double similitud){
		this.fuente = (Fuente)Delphos.getSession().get(Fuente.class, idFuente);
		this.cobertura = cobertura;
		this.similitud = similitud;
	}
	
	public String verSimilitud(){
		int numDecimales = 3;
		String similitud = this.similitud.toString();
		if (similitud.length() > numDecimales + 2)
			similitud = similitud.substring(0, similitud.indexOf('.') + numDecimales + 1);
		return similitud;
	}
	public double verCobertura(){
		return cobertura;
	}
	public Fuente getFuente() {
		return fuente;
	}
	public void setFuente(Fuente fuente) {
		this.fuente = fuente;
	}
	public Double getSimilitud() {
		return similitud;
	}
	public void setSimilitud(Double similitud) {
		this.similitud = similitud;
	}
	public boolean isRelevante() {
		return relevante;
	}
	public void setRelevante(boolean relevante) {
		this.relevante = relevante;
	}
}
