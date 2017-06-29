package delphos;

import java.util.ArrayList;
import java.util.Iterator;

import javax.persistence.Entity;

import org.hibernate.Query;

import delphos.iu.Delphos;

@Entity
public class Documento_Clasificacion extends Jerarquia<Documento_Clasificacion> {

	public Documento_Clasificacion() {
		super(Documento_Clasificacion.class);
	}

	public String getListaDocumentoClasificacion() {
		
		StringBuilder resultado = new StringBuilder();
		resultado.append(this.getNombre() + ",");
		
		ArrayList<Documento_Clasificacion> listaSubDoc = this.getSubDoc();
		for (Documento_Clasificacion subLoc : listaSubDoc)
			resultado.append(subLoc.getNombre() + ",");
				
		String sResultado = resultado.toString();
		if (sResultado.length() > 0)
			sResultado = sResultado.substring(0, sResultado.length()-1);	//Quitamos la última coma
		
		return sResultado;
	}

	private ArrayList<Documento_Clasificacion> getSubDoc() {
		ArrayList<Documento_Clasificacion> listaSubDoc = new ArrayList<Documento_Clasificacion>();
		
		Query query = Delphos.getSession().createQuery("FROM Documento_Clasificacion WHERE idPadre = " + this.getId());
		listaSubDoc = (ArrayList<Documento_Clasificacion>) query.list();
		
		//Añadimos subLocalizaciones
		ArrayList<Documento_Clasificacion> listaAuxiliar = new ArrayList<Documento_Clasificacion>();
		Iterator it = listaSubDoc.iterator();
		while(it.hasNext())
			listaAuxiliar.addAll(((Documento_Clasificacion)it.next()).getSubDoc());
		
		listaSubDoc.addAll(listaAuxiliar);
		
		return listaSubDoc;
	}
}
