package delphos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.spanishStemmer;

import delphos.iu.Delphos;

/**
 * Cromosoma de un documento para el algoritmo genético
 *
 */
public class Cromosoma2 {
	private final static Double P_CRUCE = 0.7;
	private final static Double P_MUTACION = 0.05;
	private static Session session;
	private static List<String> listaPalabrasVacias;

	private DocumentoWeb documentoWeb;
	private ArrayList<Gen> genes = new ArrayList<>();
	private Boolean cruzable;

	public Cromosoma2() {

	}

	public Cromosoma2(DocumentoWeb documentoWeb) {
		this.documentoWeb = documentoWeb;

		// Creamos su lista de términos
		String texto = documentoWeb.getTitulo() + " " + documentoWeb.getExtracto();
		if (listaPalabrasVacias == null)
			cargarPalabrasVacias();

		spanishStemmer stemmerES = new spanishStemmer();
		englishStemmer stemmerEN = new englishStemmer();
		ArrayList<String> lista = limpiar(texto);

		// Transformamos en raíces
		boolean flagProcesado = false;
		for (String palabra : lista) {
			flagProcesado = false;
			String raiz = palabra;
			stemmerES.setCurrent(palabra);
			if (stemmerES.stem()) {
				raiz = stemmerES.getCurrent();
				if (!raiz.equals(palabra)) {// Existe raíz en español
					// Buscamos la raíz en los genes del cromosoma
					Iterator<Gen> it = this.genes.iterator();
					while (it.hasNext()) {
						Gen gen = it.next();
						if (raiz.equals(gen.getRaiz())) { // Encontrada
							if (gen.getTerminos().containsKey(palabra)) { // Si ya tiene la palabra
								gen.getTerminos().put(palabra, gen.getTerminos().get(palabra) + 1); // Incrementamos la
																									// frecuencia
								flagProcesado = true;
								// break;
							} else { // Añadimos la palabra a la lista de términos
								gen.getTerminos().put(palabra, 1);
								flagProcesado = true;
								// break;
							}
						}
					}
				} else {
					stemmerEN.setCurrent(palabra);
					if (stemmerEN.stem()) {
						raiz = stemmerEN.getCurrent();
						if (!raiz.equals(palabra)) { // Existe raíz en inglés
							// Buscamos la raíz en los genes del cromosoma
							Iterator<Gen> it = this.genes.iterator();
							while (it.hasNext()) {
								Gen gen = it.next();
								if (raiz.equals(gen.getRaiz())) { // Encontrada
									if (gen.getTerminos().containsKey(palabra)) { // Si ya tiene la palabra
										gen.getTerminos().put(palabra, gen.getTerminos().get(palabra) + 1); // Incrementamos
																											// la
										// frecuencia
										flagProcesado = true;
										break;
									} else { // Añadimos la palabra a la lista de términos
										gen.getTerminos().put(palabra, 1);
										flagProcesado = true;
										break;
									}
								}
							}
						}
					}
				}
			}
			if (!flagProcesado) { // Añadimos directamente la raíz o palabra
				Gen nuevoGen = new Gen(raiz);
				nuevoGen.getTerminos().put(palabra, 1);
				this.genes.add(nuevoGen);
			}

			// Determinamos si se cruzará
			this.cruzable = Math.random() < P_CRUCE;
		}
	}

	public ArrayList<Gen> getGenes() {
		return this.genes;
	}

	public Boolean getCruzable() {
		return cruzable;
	}

	public void cargarPalabrasVacias() {
		if (session == null)
			session = Delphos.getSession();

		// listaPalabrasVacias = (List<String>) session.createSQLQuery("SELECT palabra
		// FROM vacias WHERE lenguaje = '"+idioma+"'").list();
		listaPalabrasVacias = (List<String>) session.createSQLQuery("SELECT palabra FROM vacias").list();
	}

	public ArrayList<String> limpiar(String texto) {
		if (listaPalabrasVacias == null)
			cargarPalabrasVacias();

		texto = texto.toLowerCase(); // A minúsculas
		texto = texto.replaceAll("[^a-záéíóúü]", " "); // Reemplazamos caracteres no numéricos por espacios
		texto = texto.trim();
		texto = texto.replaceAll("\\s+", " ");
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ArrayList<String> textoLimpio = new ArrayList(Arrays.asList(texto.split(" ")));

		// Eliminamos las palabras vacías y de longitud 1
		Iterator<String> iterator = textoLimpio.iterator();
		while (iterator.hasNext()) {
			String palabra = iterator.next();
			if (listaPalabrasVacias.contains(palabra) || palabra.length() < 2) {
				// System.out.println("Eliminada la palabra " + palabra);
				iterator.remove();
			}
		}

		return textoLimpio;
	}

	public Cromosoma2 clonar() {
		Cromosoma2 clon = new Cromosoma2();
		clon.documentoWeb = this.documentoWeb; // ¡Ojo! Es una referencia, no una copia.
		clon.cruzable = this.cruzable.booleanValue();
		clon.genes = new ArrayList<>();
		for (Gen gen : this.genes)
			clon.genes.add(gen.clonar());

		return clon;
	}

	@Override
	public String toString() {
		String descripcion = "Cromosoma: URL: " + documentoWeb.getUrl() + "\n";
		for (Gen gen : genes)
			descripcion += gen.toString() + "\n";
		return descripcion;
	}

	public static void cruzar(Cromosoma2 cr1, Cromosoma2 cr2) {
		// Determinamos el punto de cruce
		int pCruce = (int) Math.floor(Math.random() * cr1.getGenes().size());
		System.out.println("Cruzando en el punto " + pCruce);

		Boolean aux;
		for (int i = pCruce; i < cr1.getGenes().size(); i++) {
			aux = cr1.getGenes().get(i).getActivo();
			cr1.getGenes().get(i).setActivo(cr2.getGenes().get(i).getActivo());
			cr2.getGenes().get(i).setActivo(aux);
		}
	}

	public void mutar() {
		for(Gen gen: this.genes)
			if (Math.random() < P_MUTACION)
				gen.mutar();
	}
	
	/*
	 * Devuelve el texto libre asociado a una lista de cromosomas
	 */
	public static String verTextoLibre(List<Cromosoma2> lista) {
		// Construimos un supercromosoma con todos los cromosomas de la lista
		Cromosoma2 superCromosoma = new Cromosoma2();

		// Le añadimos todos los genes activos de la lista de cromosomas
		for (Cromosoma2 cromosoma : lista) {
			for (Gen gen : cromosoma.getGenes())
				if (gen.getActivo()) {
					if (!superCromosoma.getGenes().contains(gen))
						superCromosoma.getGenes().add(gen.clonar());
					else // Ya contiene al gen
						superCromosoma.getGenes().get(superCromosoma.getGenes().indexOf(gen)).sumar(gen);
				}
		}

		// Creamos el texto libre para la búsqueda
		String textoLibre = "";
		for (Gen gen : superCromosoma.getGenes())
			textoLibre += gen.getTerminoPrincipal() + " ";
		
		return textoLibre;
	}
	
}
