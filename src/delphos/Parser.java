/**
 * Parser.java - Procesa los documentos para detectar sus descriptores y enlaces a otras Fuentes. 
 *
 * @version 0.1
 * @author María José Tena
 */

package delphos;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.jboss.logging.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.spanishStemmer;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;

import delphos.iu.Delphos;

public class Parser {
	private static final double MULT_URL = 5.0;
	private static final double MULT_TITULO = 4.0;
	private static final double MULT_DESCRIPCION = 3.0;
	private static final double MULT_KEYWORDS = 3.5;
	private static final double MULT_H1 = 2.25;
	private static final double MULT_H2 = 2.0;
	private static final double MULT_H3 = 1.75;
	private static final int MAX_PROFUNDIDAD = 3;
	private final static int MODO_NUEVO = 1;
	private final static int MODO_ACTUALIZAR = 2;
	private static int modo = MODO_NUEVO;
	private static String hibernateCfgFile = "hibernate.cfg.xml";
	private static List<String> listaPalabrasVacias;
	private static List<Fuente> listaFuentes;
	private static Session session;
	private static Logger log = Logger.getLogger(Parser.class);

	/*
	 * En MODO_NUEVO (1) Parsea las Fuentes que tengan parseada a false, error a null y noActualizar a false.
	 * Al parsear: 
	 * 		- Establece el idioma de la Fuente.
	 * 		- Establece el título, descripción
	 * 		- Añade los Pesos del Documento.
	 * 			- Al crear cada Peso, si el Descriptor no existe, se crea.
	 * 		- Da de alta las Fuentes Hijas, estableciendo su URL, la fuente Padre y su profundidad.
	 * En MODO_ACTUALIZAR (2) - Desactivado
	 */
	public static void main(String[] args) throws Exception {
		//Determinamos el modo de trabajo
		if (args.length > 0){
			modo = Integer.parseInt(args[0]);
		}

		BasicConfigurator.configure();
		log.info("Arrancando.");

		Delphos.setHibernateCfgFile(hibernateCfgFile);
		session = Delphos.getSession();
		cargarPalabrasVacias();

		Fuente fuente = null;
		while (true) {
			Delphos.setHibernateCfgFile(hibernateCfgFile);
			session = Delphos.getSession();
			
			int indice = 0;
			Query query = session.createSQLQuery("SELECT MAX(id) FROM Fuente");
			int maxId = (int)query.uniqueResult();

			while (indice < maxId) {
				//log.info("Parseando fuente " + indice + " de " + maxId);
				try {
					fuente = obtenerFuente(indice);
					if (fuente == null) {
						log.info("No quedan fuentes por parsear. Deteniendo Parser.");
						System.exit(0);
					}
					indice = fuente.getId() + 1;
					
					if (modo == MODO_ACTUALIZAR){	//Primero actualizamos el documento
						fuente.actualizar();
						//Si no ha conseguido el documento, nos lo saltamos
						if (fuente.getDocumento() == null)
							continue;	
					}

					log.info("Parseando Fuente (" + fuente.getId() + "): " + fuente.getUrl().toString());
					

					session.beginTransaction();
					fuente.borrarVector();
					session.update(fuente);
					//fuente.guardarFrecuencia(); //Mejor lo hacemos a mano
					session.getTransaction().commit();

					parsear(fuente);

					// Guardamos la fuente
					session.beginTransaction();
					session.update(fuente);
					session.getTransaction().commit();
					//TODO Añadir la frecuencia de todos los descriptores de la fuente a la tabla de Peso_Frecuencia
					
				} catch (Exception e) {
					log.error("Error en la iteración del bucle principal: " + e.toString());
					e.printStackTrace();
					log.error("Error al parsear fuente " + fuente.getId());
					if (session.getTransaction().isActive())
						session.getTransaction().rollback();
					session = Delphos.reconectarSesion();
					String sql = "UPDATE Fuente SET error = 'Parser: " + e.toString() + "' WHERE id = " + fuente.getId();
					query = session.createSQLQuery(sql);
					session.beginTransaction();
					query.executeUpdate();
					session.getTransaction().commit();
				}
				
			}// while (true)
		}

		// Código inalcanzable
		// Delphos.cerrarSesion();
		// log.trace("Terminando.");
	}

	public static void anadirDescriptores(Fuente fuente, String texto, double multiplicador, boolean enBody) {
		ArrayList<String> descriptores = getDescriptores(texto);

		for (String descriptor : descriptores)
			fuente.anadirDescriptor(descriptor, multiplicador, enBody);
	}

	public static ArrayList<String> getDescriptores(String texto) {
		ArrayList<String> descriptores = new ArrayList<>();

		if (listaPalabrasVacias == null)
			cargarPalabrasVacias();

		spanishStemmer stemmerES = new spanishStemmer();
		englishStemmer stemmerEN = new englishStemmer();
		ArrayList<String> lista = limpiar(texto);

		int size;

		// Transformamos en raíces
		for (String palabra : lista) {
			size = descriptores.size();
			stemmerES.setCurrent(palabra);
			if (stemmerES.stem()) {
				String raiz = stemmerES.getCurrent();
				if (!raiz.equals(palabra))// Existe raíz en español
					descriptores.add(raiz); // Añadimos la raíz en español
				else {
					stemmerEN.setCurrent(palabra);
					if (stemmerEN.stem()) {
						raiz = stemmerEN.getCurrent();
						if (!raiz.equals(palabra)) // Existe raíz en inglés
							descriptores.add(raiz);// Añadimos la raíz en inglés
					}
				}
			}
			if (size == descriptores.size()) // Si no se ha añadido
				descriptores.add(palabra);// Añadimos la palabra entera
		}

		return descriptores;
	}

	
	public static void parsear(Fuente fuente) {
		Document doc = Jsoup.parse(fuente.getDocumento().getTexto());

		// Intentamos ver el idioma
		fuente.setIdioma(identificarIdiomaDocumento(fuente.getDocumento().getTexto()));

		// Añadimos los descriptores de la URL
		anadirDescriptores(fuente, fuente.getUrl().getPath(), Parser.MULT_URL, false);

		// Parseamos el título
		Elements nodos = doc.select("head > title");
		if (!nodos.isEmpty()) {
			fuente.setTitulo(nodos.get(0).text().substring(0, Math.min(256, nodos.get(0).text().length())));
			anadirDescriptores(fuente, fuente.getTitulo(), Parser.MULT_TITULO, false);
		}

		// Parseamos la descripción
		nodos = doc.select("meta[name=description]");
		Boolean encontrado = false;
		if (!nodos.isEmpty()) {
			fuente.setDescripcion(nodos.get(0).attr("content").substring(0, Math.min(1024, nodos.get(0).attr("content").length())));
			encontrado = true;
		} else {
			// si no hay description, buscar el primer párrafo <p> que tenga texto (al menos 100 char y que no diga cookie ni JavaScript, Adobe, Flash, Oracle).
			nodos = doc.select("p");
			if (!nodos.isEmpty()) {
				Iterator it = nodos.iterator();
				while (it.hasNext() && !encontrado) {
					String texto = ((Element) it.next()).text();
					if ((texto.length() > 100) && (!texto.toLowerCase().contains("cookie")) && (!texto.toLowerCase().contains("javascript")) && (!texto.toLowerCase().contains("adobe")) && (!texto.toLowerCase().contains("flash")) && (!texto.toLowerCase().contains("oracle"))) {
						fuente.setDescripcion(texto);
						encontrado = true;
					}
				}
			}
		}
		
		//Si no tiene ni título ni descripción, no la procesamos
		if ((fuente.getTitulo() == null) && (fuente.getDescripcion() == null)){
			log.info("Descartada la fuente por no tener ni título ni descripción");
			return;
		}
				
		
		if ((encontrado) && fuente.getDescripcion() != null)
			anadirDescriptores(fuente, fuente.getDescripcion(), Parser.MULT_DESCRIPCION, false);

		// Parseamos las keywords
		nodos = doc.select("meta[name=keywords]");
		if (!nodos.isEmpty())
			anadirDescriptores(fuente, nodos.get(0).attr("content"), Parser.MULT_KEYWORDS, false);

		// Parseamos el body (H1)
		nodos = doc.select("h1");
		if (!nodos.isEmpty()) {
			for (Element el : nodos)
				anadirDescriptores(fuente, el.text(), Parser.MULT_H1, true);
		}

		// Parseamos el body (H2)
		nodos = doc.select("h2");
		if (!nodos.isEmpty()) {
			for (Element el : nodos)
				anadirDescriptores(fuente, el.text(), Parser.MULT_H2, true);
		}

		// Parseamos el body (H3)
		nodos = doc.select("h3");
		if (!nodos.isEmpty()) {
			for (Element el : nodos)
				anadirDescriptores(fuente, el.text(), Parser.MULT_H3, true);
		}

		// //Parseamos el body (P)
		// nodos = doc.select("p");
		// if(!nodos.isEmpty()){
		// for (Element el : nodos)
		// anadirDescriptores(fuente, el.text(), 1);
		// }

		// Parseamos el body
		// TODO Se duplican los HX
		nodos = doc.select("body");
		if (!nodos.isEmpty())
			anadirDescriptores(fuente, nodos.get(0).text(), 1, true);

		fuente.setParseada(true);

		if (fuente.getProfundidad() >= MAX_PROFUNDIDAD)
			return;

		// Parseamos los links y los añadimos como Fuentes hijas
		nodos = doc.select("a[href]");
		for (Element link : nodos) {
			log.trace("Encontrado Link: " + link.attr("href"));
			Fuente fHija;
			if ((fHija = crearFuenteHija(fuente, link)) != null) {
				// Salvamos la Fuente
				// Primero comprobamos que no exista ninguna fuente con la misma
				// url
				Criteria crit = session.createCriteria(Fuente.class);
				crit.add(Restrictions.eq("url", fHija.getUrl()));
				if (((Long) crit.setProjection(Projections.rowCount()).uniqueResult()).intValue() == 0)
					try {
						log.trace("Guardando la Fuente Hija " + fHija.getUrl().toString());
						session.beginTransaction();
						session.save(fHija);
						session.getTransaction().commit();
					} catch (Exception e) {
						log.error("Error al guardar fuente hija: " + fHija.getUrl().toString() + " (" + e + ")");
						e.printStackTrace(System.out);
						session.getTransaction().rollback();
						session.close();
						session = Delphos.getSession();
					}
			}
		}
	}

	public static Fuente crearFuenteHija(Fuente fPadre, Element link) {
		if (formarUrl(fPadre, link.attr("href")) != null) {
			Fuente fHija = new Fuente();
			fHija.setFuentePadre(fPadre);
			fHija.setUrl(formarUrl(fPadre, link.attr("href")));
			fHija.setProfundidad(fPadre.getProfundidad() + 1);
			return fHija;
		}
		return null;
	}

	public static ArrayList<String> limpiar(String texto) {
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
			if (listaPalabrasVacias.contains(palabra) || palabra.length() < 2)
				iterator.remove();
		}

		return textoLimpio;
	}

	public static Fuente obtenerFuente(int indice) {
		Fuente resultado = null;
		Criteria crit = session.createCriteria(Fuente.class);
		//TODO Mejorar código duplicado.
		if (modo == MODO_NUEVO){
			crit.add(Restrictions.eq("parseada", false));
			crit.add(Restrictions.isNotNull("documento"));
			crit.add(Restrictions.isNull("error"));
			crit.add(Restrictions.eq("noActualizar", false));
		}
		
//		if (modo == MODO_ACTUALIZAR){
//			crit.add(Restrictions.and(Restrictions.eq("parseada", true), Restrictions.isNotNull("documento"), Restrictions.isNull("error")));
//		}
		crit.add(Restrictions.ge("id", indice));
		crit.setMaxResults(1);
		resultado = (Fuente) crit.uniqueResult();
		
		return resultado;
	}

	public static URL formarUrl(Fuente f, String link) {
		// Si tiene, le quitamos la barra final para evitar duplicados
		if (link.length() == 0)
			return null;

		if (link.charAt(link.length() - 1) == '/')
			link = link.substring(0, link.length() - 1);

		try {
			return new URL(link);
		} catch (MalformedURLException e) {
			try {
				if (link.length() == 0) // Está vacío
					return null;
				if (link.substring(0, 1).compareTo("#") == 0) // Es un ancla
					return null;
				if (link.length() > 10)
					if (link.substring(0, 10).compareToIgnoreCase("javascript") == 0) // Es un script
						return null;
				if (link.length() > 6)
					if (link.substring(0, 6).compareToIgnoreCase("mailto") == 0) // Es un mailto
						return null;
				if (link.length() > 2)
					if (link.substring(0, 2).compareTo("//") == 0) // Es un enlace externo sin protocolo
						return new URL("http:" + link);

				if (link.contains("/")) { // Es un enlace interno
					if (link.substring(0, 1).compareTo("/") == 0)
						return new URL(f.getUrl().getProtocol() + "://" + f.getUrl().getHost() + link);
					else
						return new URL(f.getUrl().getProtocol() + "://" + f.getUrl().getHost() + "/" + link);
				}
			} catch (Exception ex) {
				log.error("No sabemos procesar este link: " + link + " de la fuente " + f.getId());
				return null;
			}
			return null;
		}
	}

	public static String identificarIdiomaDocumento(String documento) {
		String idioma = null;
		Document doc = Jsoup.parse(documento);
		Elements nodos;

		// Intentamos detectar el lenguaje con el texto del body
		String text = null;
		nodos = doc.select("body");
		if (!nodos.isEmpty()) {
			text = nodos.get(0).text();
			idioma = identificarIdioma(text);
		}
		return idioma;
	}

	public static String identificarIdioma(String texto) {
		String idioma = null;
		try {
			if (DetectorFactory.getLangList().size() == 0)
				DetectorFactory.loadProfile("lang-profiles");
			Detector detector = DetectorFactory.create();
			detector.append(texto);
			idioma = detector.detect();
		} catch (Exception e) {
			log.error("Error al detectar idioma " + e.getMessage());
		}
		return idioma;
	}

	public static void cargarPalabrasVacias() {
		if (session == null)
			session = Delphos.getSession();

		// listaPalabrasVacias = (List<String>) session.createSQLQuery("SELECT palabra FROM vacias WHERE lenguaje = '"+idioma+"'").list();
		listaPalabrasVacias = (List<String>) session.createSQLQuery("SELECT palabra FROM vacias").list();
	}

	public static Session getSession() {
		return session;
	}
	
	public static String getExtractoFuente(Fuente fuente, String consulta){
		if (fuente.getDocumento() == null)
			return "";
		return getExtractoTexto(fuente.getDocumento().getTexto(), consulta);
	}

	public static String getExtractoTexto(String html, String consulta){
		int maxChars = 50; // Máximo de caracteres antes y después de la palabra buscada.
		int posicion, posInicial, posFinal, posPunto, puntoPrevio, puntoPosterior, index, primeraCoincidencia;
		String resultado = "";
		String pTexto;
		boolean extraerParrafo;
		
		Document doc = Jsoup.parse(html);
		Elements nodos = doc.select("p");
		for (Element p : nodos) {
			extraerParrafo = false; // Indica si hay que extraer el párrafo
			pTexto = p.text();
			primeraCoincidencia = Integer.MAX_VALUE;
			for (String palabra : limpiar(consulta)) {
				// pTexto = pTexto.replaceAll("(?i)" + palabra, "<b>" + palabra + "</b>");
				if ((posicion = pTexto.indexOf(palabra)) != -1) {
					extraerParrafo = true;
					if (posicion < primeraCoincidencia)
						primeraCoincidencia = posicion;
				}
			}

			if (extraerParrafo) {
				String extracto = "";
				if (pTexto.length() < 100)
					extracto = pTexto;
				else {// Extraemos 100 caracteres alrededor de la primera coincidencia
					posInicial = 0;
					posFinal = pTexto.length();

					// Buscamos los puntos del texto
					puntoPrevio = posInicial;
					puntoPosterior = posFinal;
					index = 0;
					while ((posPunto = pTexto.indexOf('.', index)) != -1) {
						if ((posPunto > puntoPrevio) && (posPunto < primeraCoincidencia))
							puntoPrevio = posPunto;
						if ((posPunto < puntoPosterior) && (posPunto > primeraCoincidencia))
							puntoPosterior = posPunto;

						index = posPunto + 1;
					}
					if (primeraCoincidencia - maxChars > 0) {
						if (puntoPrevio > primeraCoincidencia - maxChars)
							posInicial = puntoPrevio;
						else
							posInicial = primeraCoincidencia - maxChars;
					}
					if (primeraCoincidencia + maxChars < posFinal) {
						if (puntoPosterior < primeraCoincidencia + maxChars)
							posFinal = puntoPosterior + 1; // incluimos el punto posterior
						else
							posFinal = primeraCoincidencia + maxChars;
					}
					extracto = pTexto.substring(posInicial, posFinal);

					// Ajustamos a la palabra al principio y la final
					while ((extracto.charAt(0) != ' ') && (posInicial != 0) && (extracto.length() > 1))
						extracto = extracto.substring(1, extracto.length());
					while ((extracto.charAt(extracto.length() - 1) != ' ') && (extracto.charAt(extracto.length() - 1) != '.') && (extracto.length() > 1))
						extracto = extracto.substring(0, extracto.length() - 1);
				}
				if (extracto.length() > 1)
					//TODO: Puede fallar (Java heap space) ¿Demasiado grande?. Añadir try catch
					if (resultado.length() < 1000)
						resultado += resultado + extracto + " ... ";
			}
		}

		if (resultado.length() > 400)
			resultado = resultado.substring(0, 300);

		if (resultado.length() == 0)
			resultado = null;

		return resultado;

	}

}
