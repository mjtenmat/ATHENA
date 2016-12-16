package delphos;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.criterion.Restrictions;

import delphos.iu.Delphos;

@Entity
public class Peso implements Cloneable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn(name = "idFuente", referencedColumnName = "id")
	private Fuente fuente;
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	@OneToMany (orphanRemoval=true, fetch = FetchType.LAZY)
	//(cascade = CascadeType.ALL) - Eliminado para que Parser no borre descriptores de otras fuentes
	@JoinColumn(name = "idDescriptor", referencedColumnName = "id")
	private Descriptor descriptor;
	private Integer frecuencia;
	public Double multiplicador;
	public Double peso;
	private boolean enBody = false;

	public Object clone() {
		Object obj = null;
		try {
			obj = super.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("Descritor: CloneNotSupportedException.");
			System.exit(-1);
		}
		return obj;
	}
	
	public String toString(){
		String texto = "";
		
		texto += id + "\t";
		texto += frecuencia + "\t";
		texto += fuente.getId() + "\t";
		texto += multiplicador + "\t";
		texto += peso + "\t";
		texto += descriptor.getId() + "\t";
		
		return texto;
	
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Fuente getFuente() {
		return fuente;
	}

	public void setFuente(Fuente fuente) {
		this.fuente = fuente;
	}

	public String getTextoDescriptor() {
		return descriptor.getTexto();
	}
	
	public Descriptor getDescriptor(){
		return descriptor;
	}

	public void setDescriptor(String textoDescriptor) {
		if (this.descriptor == null) {
			// Primero buscamos si existe
			Session session = Delphos.getSession();
			Criteria crit = session.createCriteria(Descriptor.class);
			crit.add(Restrictions.eq("texto", textoDescriptor));
			this.descriptor = (Descriptor) crit.uniqueResult();
			if (this.descriptor == null){	//Si no existe, lo creamos
				this.descriptor = new Descriptor();
			}
		}
		this.descriptor.setTexto(textoDescriptor);
	}

	public int getFrecuencia() {
		return frecuencia;
	}

	public void setFrecuencia(int frecuencia) {
		this.frecuencia = frecuencia;
	}

	public double getPeso() {
		if (peso == null) // Evita excepción de puntero nulo si no ha pasado el Weigher
			peso = 0.0;
		return peso;
	}

	public void setPeso(double peso) {
		this.peso = peso;
	}

	public double getMultiplicador() {
		return multiplicador;
	}

	public void setMultiplicador(double multiplicador) {
		this.multiplicador = multiplicador;
	}
	
	public boolean isEnBody() {
		return enBody;
	}
	public void setEnBody(boolean enBody) {
		this.enBody = enBody;
	}

}
