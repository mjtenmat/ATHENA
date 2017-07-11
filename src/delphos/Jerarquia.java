package delphos;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import delphos.iu.Delphos;

@MappedSuperclass
public abstract class Jerarquia<T> {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)  //No tiene autoincrement en la BD.
	protected Integer id;
	@ManyToOne
	@JoinColumn(name="idPadre", referencedColumnName="id")
	protected T padre;
	protected String nombre;
	protected String descripcion;
	protected String idJerar;

	@Transient
	private Class<T> clase;
	
	public Jerarquia(Class<T> clase){
		this.clase = clase;
	}
	
	public T cargar(int id){
		Session session = Delphos.getSession();
		session.beginTransaction();
		T obj = (T)session.get(this.clase, id);
		session.getTransaction().commit();
		return obj;
	}
	
	public T buscar(String nombre){
		Criteria crit = Delphos.getSession().createCriteria(this.clase);
		crit.add(Restrictions.eq("nombre", nombre));
		return (T)crit.uniqueResult();
	}
	
	public String toString(){
		String textoAMostrar = this.nombre;
		if (this.descripcion != null)
			textoAMostrar += " - " + this.descripcion;
		return textoAMostrar;
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public T getPadre() {
		return padre;
	}
	public void setpadre(T padre) {
		this.padre = padre;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getIdJerar() {
		return idJerar;
	}
	public void setIdJerar(String idJerar) {
		this.idJerar = idJerar;
	}
}
