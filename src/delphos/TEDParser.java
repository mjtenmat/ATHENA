package delphos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import delphos.iu.Delphos;

public class TEDParser extends DefaultHandler {
	public final static String DIR_MNT = "/mnt";
	public final static String INPUT_DIR = "/home/mjtena/TED/input";
	//public final static String INPUT_DIR = "/home/mjaque/TED/pruebas";
	// Indicadores del nodo que estamos procesando:
	private final static String TD_DOCUMENT_TYPE = "TD_DOCUMENT_TYPE";
	private final static String URI_DOC = "URI_DOC";
	private final static String ML_TI_DOC = "ML_TI_DOC";
	private final static String TI_CY = "TI_CY";
	private final static String TI_TEXT = "TI_TEXT";
	private final static String TI_TOWN = "TI_TOWN";
	private final static String AA_NAME = "AA_NAME";
	private final static String DATE_PUB = "DATE_PUB";
	private final static String SHORT_CONTRACT_DESCRIPTION = "SHORT_CONTRACT_DESCRIPTION";
	private final static String SHORT_DESCRIPTION = "SHORT_DESCRIPTION";
	private final static String ORIGINAL_CPV = "ORIGINAL_CPV";
	static String codCPV;
	Licitacion licitacion;
	static boolean abortar = false;
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	String pais;
	ParLocalizacion plPais;
	static int procesados = 0;
	static int insertados = 0;
	static int abortados = 0;
	static long ultimoIdLicitacion;

	static HashMap<String, Boolean> procesando = new HashMap();
	// Código CPV -{idSector}
	static HashMap<String, HashSet<Integer>> mapaCPV_Sector = new HashMap();

	static class ParLocalizacion {
		public String nombre;
		public Integer idLocalizacion;
	}

	static HashMap<String, ParLocalizacion> mapaCodPais_Localizacion = new HashMap();
	static HashMap<String, Integer> mapaNombrePais_Localizacion = new HashMap();

	public static void main(String[] args) throws Exception {
System.out.println();
		// Descomprimimos el CD (o la imagen iso montada)
		// Hay que descomprimir todos los directorios
		Path dirInicial = Paths.get(DIR_MNT);
		String patron = "*_TED-XML_ORG.tar.gz";
		ProcesadorFicheros pf = new ProcesadorFicheros(patron, 1);
		Files.walkFileTree(dirInicial, pf);
		pf.finalizar();

		System.out.print("Cargando mapas CSV...");
		cargarProcesando();
		cargarCPV_Sector();
		//cargarPais_Localizacion();
		System.out.println(" Hecho.");

		// Iteramos en el directorio de entrada
		dirInicial = Paths.get(INPUT_DIR);
		patron = "*.xml";
		pf = new ProcesadorFicheros(patron, 2);
		Files.walkFileTree(dirInicial, pf);
		pf.finalizar();

		System.out.println("\nProcesados: " + procesados);
		System.out.println("Insertados: " + insertados);
		System.out.println("Abortados: " + abortados);
		System.out.println("Último id de Licitación insertado: " + ultimoIdLicitacion);
		System.out.println("\nFin del Programa.");
		System.exit(0);
	}

	private static void cargarCPV_Sector() {
		try (BufferedReader br = new BufferedReader(new FileReader(
				"cpv-sector.csv"))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				// System.out.println(sCurrentLine);
				String[] trozos = sCurrentLine.split(",");
				HashSet<Integer> setIdSector = new HashSet<Integer>();
				for (int i = 1; i < trozos.length; i++) {
					setIdSector.add(Integer.parseInt(trozos[i]));
				}
				mapaCPV_Sector.put(trozos[0], setIdSector);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void cargarProcesando() {
		procesando.put(TD_DOCUMENT_TYPE, false);
		procesando.put(URI_DOC, false);
		procesando.put(ML_TI_DOC, false);
		procesando.put(TI_CY, false);
		procesando.put(TI_TEXT, false);
		procesando.put(TI_TOWN, false);
		procesando.put(AA_NAME, false);
		procesando.put(DATE_PUB, false);
		procesando.put(SHORT_CONTRACT_DESCRIPTION, false);
		procesando.put(ORIGINAL_CPV, false);

	}

	public static void borrarProcesando() {
		for (String clave : procesando.keySet())
			procesando.put(clave, false);
	}

	public void startDocument() throws SAXException {
		licitacion = new Licitacion();
		abortar = false;
		borrarProcesando();
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (abortar)
			return;
		switch (qName.toUpperCase()) {
		case ML_TI_DOC:
			if (atts.getValue("LG").equals("EN"))
				procesando.put(ML_TI_DOC, true);
			else{
				if (licitacion.getLocalizacion() == null)
					procesando.put(ML_TI_DOC, true);
				else
					procesando.put(ML_TI_DOC, false);
			}
			break;

		case TI_CY:
			if (procesando.get(ML_TI_DOC))
				procesando.put(TI_CY, true);
			break;

		case TI_TOWN:
			if (procesando.get(ML_TI_DOC))
				procesando.put(TI_TOWN, true);
			break;

		case TI_TEXT:
			if (procesando.get(ML_TI_DOC))
				procesando.put(TI_TEXT, true);
			break;

		case URI_DOC:
			if (atts.getValue("LG").equals("EN"))
				procesando.put(URI_DOC, true);
			else{
				if (licitacion.getUrl() == null)
					procesando.put(URI_DOC, true);
				else
					procesando.put(URI_DOC, false);
			}
			break;

		case AA_NAME:
			if (atts.getValue("LG").equals("EN"))
				procesando.put(AA_NAME, true);
			else{
				if (licitacion.getEntidadEmisora() == null)
					procesando.put(AA_NAME, true);
				else
					procesando.put(AA_NAME, false);
			}
			break;

		case DATE_PUB:
			procesando.put(DATE_PUB, true);
			break;

		case SHORT_CONTRACT_DESCRIPTION:
		case SHORT_DESCRIPTION:			
			procesando.put(SHORT_CONTRACT_DESCRIPTION, true);
			break;

		case ORIGINAL_CPV:
			codCPV = atts.getValue("CODE");
			procesando.put(ORIGINAL_CPV, true);
			break;
			
		case TD_DOCUMENT_TYPE:
			procesando.put(TD_DOCUMENT_TYPE, true);
			break;			

		// default:
		// if (procesando.containsKey(qName.toUpperCase()))
		// procesando.put(qName.toUpperCase(), true);
		}
	}

	public void characters(char ch[], int start, int length)
			throws SAXException {
		if (abortar)
			return;
		try {
			if (procesando.get(TD_DOCUMENT_TYPE)) {
				String nombreTipoLicitacion = new String(ch, start, length);
				System.out.println("Tipo: " + nombreTipoLicitacion);
				// Buscamos el TipoLicitacion
				Query query = Delphos.getSession().createQuery(
						"FROM TipoLicitacion where nombre = :nombre");
				query.setParameter("nombre", nombreTipoLicitacion);
				TipoLicitacion tipoLicitacion = (TipoLicitacion) query
						.uniqueResult();
				if (tipoLicitacion == null) {
					System.out.println("Tipo de Licitación ("
							+ nombreTipoLicitacion + ") desconocido.");
					abortar = true;
				} else
					licitacion.setTipoLicitacion(tipoLicitacion);
				procesando.put(TD_DOCUMENT_TYPE, false);
			}
			if (procesando.get(URI_DOC)) {
				String textoURI = new String(ch, start, length);
				System.out.println("url: " + textoURI);
				licitacion.setUrl(new URL(textoURI));
				procesando.put(URI_DOC, false);
			}
			if (procesando.get(TI_CY)) {
				pais = new String(ch, start, length);
				System.out.println("País: " + pais);
				procesando.put(TI_CY, false);
			}
			if (procesando.get(TI_TOWN)) {
				String ciudad = new String(ch, start, length);
				System.out.println("Ciudad: " + ciudad);
				// Buscamos la Localización por el nombre de la ciudad
				Query query = Delphos.getSession().createQuery(
						"FROM Licitacion_Localizacion where ciudad = :ciudad");
				query.setParameter("ciudad", ciudad);
/*				Licitacion_Localizacion localizacion = (Licitacion_Localizacion) query.uniqueResult();
				if (localizacion == null) {
					localizacion = new Licitacion_Localizacion();
					localizacion.setCiudad(ciudad);
					localizacion.setPais(pais);
				}
*/
		//		licitacion.setLocalizacion(localizacion);
				pais = null;
				procesando.put(TI_TOWN, false);
			}
			if (procesando.get(TI_TEXT)) {
				String titulo = new String(ch, start, length);
				System.out.println("titulo: " + titulo);
				licitacion.setTitulo(titulo);
				procesando.put(TI_TEXT, false);
			}
			if (procesando.get(AA_NAME)) {
				String entidadEmisora = new String(ch, start, length);
				System.out.println("Entidad Emisora: " + entidadEmisora);
				licitacion.setEntidadEmisora(entidadEmisora);
				procesando.put(AA_NAME, false);
			}
			if (procesando.get(DATE_PUB)) {
				String textoFechaPublicacion = new String(ch, start, length);
				System.out.println("Fecha de Publicación: "
						+ textoFechaPublicacion);
				Date fechaPublicacion = sdf.parse(textoFechaPublicacion);
			//	licitacion.setFechaPublicacion(fechaPublicacion);
				procesando.put(DATE_PUB, false);
			}
			if (procesando.get(SHORT_CONTRACT_DESCRIPTION)) {
				String resumen = new String(ch, start, length);
				System.out.println("Resumen: " + resumen);
				licitacion.setResumen(resumen);
				procesando.put(SHORT_CONTRACT_DESCRIPTION, false);
			}
			if (procesando.get(ORIGINAL_CPV)) {
				String nombreCPV = new String(ch, start, length);
				System.out.println("Etiqueta CPV (" + codCPV + "): "
						+ nombreCPV);
				// Buscamos la etiqueta de CPV
				Query query = Delphos.getSession().createQuery(
						"FROM CPV where codigo = :codCPV");
				query.setParameter("codCPV", codCPV);
				CPV cpv = (CPV) query.uniqueResult();
				if (cpv == null) {
					System.out.println("No se ha encontrado CPV: " + codCPV
							+ " - " + nombreCPV);
					abortar = true;
				} 
//				else {
//					licitacion.getCpv().add(cpv);
//				}

				// Creamos el Set<Sector> de la licitacion a partir de los
				// idSector del mapa
				try {
					Iterator<Integer> it = (Iterator) mapaCPV_Sector.get(
							cpv.getCodigo()).iterator();
					while (it.hasNext()) {
						Sector sector = (Sector) Delphos.getSession().get(
								Sector.class, it.next());
//						licitacion.getSectores().add(sector);
						System.out.println("Sector (" + sector.getId() + "): "
								+ sector.getNombre());
					}
				} catch (NullPointerException npe) {
					// Se produce cuando el CPV no está en mapaCPV (porque no es
					// de los que nos interesa
				}

				procesando.put(ORIGINAL_CPV, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error procesando Licitación.");
			System.exit(-1);
		}
	}

	public void endDocument() throws SAXException {
		procesados++;

		if (licitacion.getTipoLicitacion() == null) {
			System.out.println("Sin tipo de licitación.");
			abortar = true;
		}
		if (abortar) {
			System.out.println("Abortamos esta licitación.");
			abortados++;
			return;
		}

		try {
			System.out.print("Insertando en BD...");
			Session session = Delphos.getSession();
			session.beginTransaction();
			session.save(licitacion.getLocalizacion());
			session.save(licitacion);
			session.getTransaction().commit();
			System.out.println("Hecho.");
			insertados++;
			ultimoIdLicitacion = ((BigInteger) session.createSQLQuery("SELECT LAST_INSERT_ID()").uniqueResult()).longValue();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}

class ProcesadorFicheros extends SimpleFileVisitor<Path> {

	public final int DESCOMPRIMIR = 1;
	public final int PARSEAR = 2;
	private final PathMatcher matcher;
	private int numMatches = 0;
	private int modo;
	SAXParserFactory spf;
	SAXParser saxParser;
	XMLReader xmlReader;

	ProcesadorFicheros(String patron, int modo) {
		matcher = FileSystems.getDefault().getPathMatcher("glob:" + patron);
		this.modo = modo;
		if (modo == PARSEAR) {
			System.out.print("Preparando parser XML...");
			try {
				spf = SAXParserFactory.newInstance();
				spf.setNamespaceAware(true);
				saxParser = spf.newSAXParser();
				xmlReader = saxParser.getXMLReader();
				xmlReader.setContentHandler(new TEDParser());
			} catch (Exception e) {
				System.out.println("Error al preparar parser.");
				e.printStackTrace();
				System.exit(-1);
			}
			System.out.println(" Hecho.");
		}
	}

	// Compares the glob pattern against
	// the file or directory name.
	void buscar(Path file) {
		Path pathFichero = file.getFileName();
		System.out.print(file.getFileName() + "...");
		if (pathFichero != null && matcher.matches(pathFichero)) {
			numMatches++;
			System.out.println("Aceptado");
			String pathCompleto = file.getParent().toString() + "/"
					+ file.getFileName().toString();
			if (modo == DESCOMPRIMIR) {
				descomprimir(pathCompleto);
			}
			if (modo == PARSEAR) {
				try {
					xmlReader.parse(pathCompleto);
				} catch (Exception e) {
					System.out.println("Error al parsear.");
					e.printStackTrace();
					System.exit(-1);
				}
			}
		} else {
			System.out.println("ignorado");
		}
	}

	private void descomprimir(String pathCompleto) {
		System.out.println("Descomprimiendo " + pathCompleto + "...");

		String[] comando = { "tar", "xzf", pathCompleto };

		ProcessBuilder pb = new ProcessBuilder(comando);
		try {
			pb.directory(new File(TEDParser.INPUT_DIR));
			Process process = pb.start();

			int retorno = process.waitFor();
			// InputStream is = process.getInputStream();
			InputStream is = process.getErrorStream();
			// Flujo de lectura para esa entrada
			InputStreamReader isr = new InputStreamReader(is);
			// Buffer para leer linea a linea
			BufferedReader br = new BufferedReader(isr);
			String line, error;
			error = "";
			while ((line = br.readLine()) != null) {
				error += line + "\n";
			}
			if (error != "") {
				System.out.println("Error de descompresión: " + error);
				System.in.read();
			}
			is.close();
			System.out
					.println("Hecho. La descompresión de devuelve " + retorno);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("Excepción de E/S!");
			System.exit(-1);
		} catch (InterruptedException ex) {
			System.err.println("El proceso hijo finalizó de forma incorrecta");
			System.exit(-1);
		}

	}

	void finalizar() {
		System.out.println("Procesados: " + numMatches);
	}

	// Invoke the pattern matching
	// method on each file.
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		Path pathFichero = file.getFileName();
		System.out.println("\nProcesando " + file.getParent() + "/" +pathFichero.toString());
		if (pathFichero != null && matcher.matches(pathFichero)) {
			numMatches++;
			String pathCompleto = file.getParent().toString() + "/"
					+ file.getFileName().toString();
			if (modo == DESCOMPRIMIR) {
				descomprimir(pathCompleto);
			}
			if (modo == PARSEAR) {
				try {
					xmlReader.parse(pathCompleto);
				} catch (Exception e) {
					System.out.println("Error al parsear.");
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}

		return FileVisitResult.CONTINUE;
	}

	// Invoke the pattern matching
	// method on each directory.
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
		buscar(dir);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		System.err.println(exc);
		return FileVisitResult.CONTINUE;
	}
}
