package delphos;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import delphos.iu.Delphos;

@Entity
public class CPI{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String nombre;
	private String descripcion;
	
	public CPI(){
		
	}
	
	public CPI(String nombre){
		this.nombre = nombre;
	}
	
	private void clone(CPI cpi) {
		this.nombre = cpi.nombre;
		this.descripcion = cpi.descripcion;
		this.id = cpi.id;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public int getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@Override
	public String toString() {
		//return "CPI [id=" + id + ", nombre=" + nombre + ", descripcion=" + descripcion + "]";
		return this.nombre + " - " + this.descripcion;
	}

	public static List<CPI> getCPIs(String nombre) {
		Criteria crit = Delphos.getSession().createCriteria(CPI.class);
		crit.add(Restrictions.eq("nombre", nombre));
		return crit.list();
	}

}
