package delphos;

import java.util.ArrayList;

import delphos.AnalisisTendencia.CategoriasLicitaciones;
import delphos.AnalisisTendencia.Tipo;

public class AnalisisTendencia {
	public static enum Tipo {PATENTES, LICITACIONES};
	public static enum CategoriasPatentes {SECTOR, PAIS, INVENTOR, SOLICITANTE, CONTENIDO};
	public static enum CategoriasLicitaciones {SECTOR, PAIS, TIPO, SOLICITANTE, CONTENIDO};
	public static enum TipoGeneral {LICITACION_SECTOR, LICITACION_PAIS, LICITACION_TIPO, LICITACION_SOLICITANTE, LICITACION_CONTENIDO, PATENTE_SECTOR, PATENTE_PAIS, PATENTE_INVENTOR, PATENTE_SOLICITANTE, PATENTE_CONTENIDO};
	
	private Tendencia tendencia;
	private ArrayList<EntradaAnalisisTendencia> listaPatenteSector = new ArrayList<EntradaAnalisisTendencia>();
	private ArrayList<EntradaAnalisisTendencia> listaPatentePais = new ArrayList<EntradaAnalisisTendencia>();
	private ArrayList<EntradaAnalisisTendencia> listaPatenteInventor = new ArrayList<EntradaAnalisisTendencia>();
	private ArrayList<EntradaAnalisisTendencia> listaPatenteSolicitante = new ArrayList<EntradaAnalisisTendencia>();
	private ArrayList<EntradaAnalisisTendencia> listaPatenteContenido = new ArrayList<EntradaAnalisisTendencia>();
	private ArrayList<EntradaAnalisisTendencia> listaLicitacionSector = new ArrayList<EntradaAnalisisTendencia>();
	private ArrayList<EntradaAnalisisTendencia> listaLicitacionPais = new ArrayList<EntradaAnalisisTendencia>();
	private ArrayList<EntradaAnalisisTendencia> listaLicitacionTipo = new ArrayList<EntradaAnalisisTendencia>();
	private ArrayList<EntradaAnalisisTendencia> listaLicitacionSolicitante = new ArrayList<EntradaAnalisisTendencia>();
	private ArrayList<EntradaAnalisisTendencia> listaLicitacionContenido = new ArrayList<EntradaAnalisisTendencia>();
	public Tendencia getTendencia() {
		return tendencia;
	}
	public ArrayList<EntradaAnalisisTendencia> getListaPatenteSector() {
		return listaPatenteSector;
	}
	public ArrayList<EntradaAnalisisTendencia> getListaPatentePais() {
		return listaPatentePais;
	}
	public ArrayList<EntradaAnalisisTendencia> getListaPatenteInventor() {
		return listaPatenteInventor;
	}
	public ArrayList<EntradaAnalisisTendencia> getListaPatenteSolicitante() {
		return listaPatenteSolicitante;
	}
	public ArrayList<EntradaAnalisisTendencia> getListaPatenteContenido() {
		return listaPatenteContenido;
	}
	public ArrayList<EntradaAnalisisTendencia> getListaLicitacionSector() {
		return listaLicitacionSector;
	}
	public ArrayList<EntradaAnalisisTendencia> getListaLicitacionPais() {
		return listaLicitacionPais;
	}
	public ArrayList<EntradaAnalisisTendencia> getListaLicitacionTipo() {
		return listaLicitacionTipo;
	}
	public ArrayList<EntradaAnalisisTendencia> getListaLicitacionSolicitante() {
		return listaLicitacionSolicitante;
	}
	public ArrayList<EntradaAnalisisTendencia> getListaLicitacionContenido() {
		return listaLicitacionContenido;
	}
	private ArrayList<EntradaAnalisisTendencia> getListaPorTipoGeneral(TipoGeneral tipo){
		ArrayList<EntradaAnalisisTendencia> lista = null;
		switch(tipo){
		case LICITACION_SECTOR:
			lista = this.getListaLicitacionSector();
			break;
		case LICITACION_PAIS:
			lista = this.getListaLicitacionPais();
			break;
		case LICITACION_TIPO:
			lista = this.getListaLicitacionTipo();
			break;
		case LICITACION_SOLICITANTE:
			lista = this.getListaLicitacionSolicitante();
			break;
		case LICITACION_CONTENIDO:
			lista = this.getListaLicitacionContenido();
			break;
		case PATENTE_SECTOR:
			lista = this.getListaPatenteSector();
			break;
		case PATENTE_PAIS:
			lista = this.getListaPatentePais();
			break;
		case PATENTE_INVENTOR:
			lista = this.getListaPatenteInventor();
			break;
		case PATENTE_SOLICITANTE:
			lista = this.getListaPatenteSolicitante();
			break;
		case PATENTE_CONTENIDO:
			lista = this.getListaPatenteContenido();
			break;
		}
		return lista;
	}
	public void aniadirListaALista(TipoGeneral tipo, ArrayList<String> listaTerminos) {
		for(String termino : listaTerminos){
			termino = termino.trim();
			if (termino != null)
				if (termino.length() > 1)
					aniadirALista(tipo, termino);
		}
	}
	public void aniadirALista(TipoGeneral tipo, String termino) {
		ArrayList<EntradaAnalisisTendencia> lista = getListaPorTipoGeneral(tipo);
		boolean encontrado = false;
		for(EntradaAnalisisTendencia entrada : lista){
			if (termino.equals(entrada.getTermino())){
				encontrado = true;
				entrada.incrementar();
				break;
			}
		}
		if (!encontrado)
			lista.add(new EntradaAnalisisTendencia(termino, 1));
	}
	
}
