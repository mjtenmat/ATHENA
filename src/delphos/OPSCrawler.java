package delphos;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.net.util.Base64;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.exception.ConstraintViolationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import delphos.iu.Delphos;

public class OPSCrawler extends DefaultHandler {
	private final static Logger log = Logger.getLogger(OPSCrawler.class);
	private final static int RANGO = 99;
	private static int indice = 1;
	private final static String consumerKey = "87DmoPnv82V0owMOOHCwWL4huCOHAvNb";
	private final static String consumerSecretKey = "KHGT1IXsdGME9Iun";
	public static String token;
	// http://ops.epo.org/3.1/rest-services/published-data/search?q=cpc%3D%2Flow%20H03C5%2F00&Range=1-100

	// Variables de control de SAXParser
	private static Stack<String> control = new Stack<String>();
	static CPI cpi = null; // CPI actual que estamos procesando
	static int anio;
	boolean error;
	String error_code = null;
	String error_message = null;
	String error_description = null;
	public static URI uri;
	public static String rango;

	public static Patente patente;

	static Integer totalResultCount = null;

	public static void main(String[] args) throws IOException {
		BasicConfigurator.configure();
		log.info("Arrancando.");

		token = obtenerToken();

		// Obtenemos lista de CPCs e iterar.
		Query query = Delphos.getSession().createQuery("FROM CPI");
		List<CPI> listaCPI = (List<CPI>) query.list();
		Iterator it = listaCPI.iterator();
		while (it.hasNext()) {
			cpi = (CPI) it.next();
			for (anio = 2015; anio > 1999; anio--) {
				indice = 1;
				do {
					procesarCPI(cpi, anio);
					try {
						System.out.println("Durmiendo...");
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} while (indice < totalResultCount);
			}
			totalResultCount = null;
			indice = 1;
			System.out.println("Siguiente CPI...");
			try {
				System.out.println("Durmiendo...");
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.in.read();
		}
		System.out.println("Fin del Programa.");
	}

	private static void procesarCPI(CPI cpi, int anio) {
		log.info("Procesando CPI = " + cpi.getNombre());
		// Búsqueda de Documentos en EPO
		String scheme = "https";
		String authority = "ops.epo.org";
		String path = "/3.1/rest-services/published-data/search";

		String cpc = cpi.getNombre();
		// String query = "q=cpc/low " + cpc;// + " Range=" + indice + "-" +
		// (indice + RANGO);
		String query = "q=cpc=/low " + cpc;
		log.info("Año: " + anio);
		query += " AND pd within \"" + (anio - 1) + " " + anio + "\"";
		try {
			uri = new URI(scheme, authority, path, query, null);
			log.info(uri.toString());

			// SAXParserFactory factory = SAXParserFactory.newInstance();
			// SAXParser saxParser = factory.newSAXParser();
			// saxParser.parse(uri.toString(), new OPSCrawler());

			HttpsURLConnection con = (HttpsURLConnection) (uri.toURL().openConnection());

			// add request header
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", "Bearer " + OPSCrawler.token);
			int fin = indice + RANGO;
			//Da fallos es 1.7 - Corregir y descomentar (si volvemos a utilizarlo)
//			if (totalResultCount != null) {
//				fin = Integer.min(fin, totalResultCount);
//			}
			rango = indice + "-" + fin;
			con.setRequestProperty("X-OPS-Range", rango);
			indice += RANGO;
			log.info("Rango: " + rango);
			// System.in.read();

			// Send get request
			con.setDoOutput(true);

			int responseCode = con.getResponseCode();
			log.trace("\nSending 'GET' request to URI : " + uri);
			log.trace("Response Code : " + responseCode);

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(con.getInputStream(), new OPSCrawler());

			// File file = new File(uri.toString());
			// InputStream inputStream= new FileInputStream(file);
			// Reader reader = new InputStreamReader(inputStream,"UTF-8");
			// InputSource is = new InputSource(reader);
			// is.setEncoding("UTF-8");
			// saxParser.parse(is, new OPSCrawler());

		} catch (URISyntaxException e) {
			e.printStackTrace();
			// System.exit(-1);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			// System.exit(-1);
		} catch (SAXException e) {
			e.printStackTrace();
			// System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			// TODO: Sleep y reintentar.
			// System.exit(-1);
		}
	}

	public static String obtenerToken() {
		// Login en EPO

		// Step 1
		String authorization = new String(Base64.encodeBase64((consumerKey + ":" + consumerSecretKey).getBytes()));

		// Step 2
		BufferedReader in = null;
		try {
			URL url = new URL("https://ops.epo.org/3.1/auth/accesstoken");
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization", "Basic " + authorization);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			String urlParameters = "grant_type=client_credentials";

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			log.trace("\nSending 'POST' request to URL : " + url);
			log.trace("Post parameters : " + urlParameters);
			log.trace("Response Code : " + responseCode);

			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			// print result
			log.trace(response.toString());
			String aguja = "\"access_token\" : \"";
			int inicio = response.indexOf(aguja) + aguja.length();
			int fin = response.indexOf("\"", inicio + 1);
			token = response.substring(inicio, fin);
			log.trace("Token: " + token);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			System.exit(-1);
		} catch (IOException e2) {
			e2.printStackTrace();
			System.exit(-1);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return token;
	}

	// Métodos del DefaultHandler de SAXParser
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		log.trace("Start Element :" + qName);

		control.push(qName);
		// Aquí leemos atributos
		switch (qName) {
		case "error":
			error = true;
			break;
		case "ops:publication-reference":
			patente = new Patente();
			break;
		case "ops:biblio-search":
			totalResultCount = Integer.parseInt(attributes.getValue("total-result-count"));
			log.info("Num resultados: " + totalResultCount);
			break;
		case "document-id":
			patente.documentIdType = attributes.getValue("document-id-type");
			log.trace("DocumentIdType: " + patente.documentIdType);
			break;
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {

		log.trace("End Element :" + qName);

		switch (control.pop()) {
		case "document-id":
			System.out.println("Procesado: " + patente.docNumber);
			if ("invalid result".equals(patente.docNumber))
				break;
			if (estaEnBD(patente.getLocalizacion() + patente.docNumber + patente.kind))
				break;

			//       http://worldwide.espacenet.com/publicationDetails/biblio?DB=worldwide.espacenet.com&II=0&ND=3&adjacent=true&locale=en_EP&FT=D&date=20150423&CC=US&NR=2015110598A1&KC=A1
			// String
			// sURL="http://worldwide.espacenet.com/publicationDetails/biblio?DB=worldwide.espacenet.com&II=0&ND=3&adjacent=true&locale=en_EP&FT=D";
			// sURL +=
			// ""&date=" + 20150423 + "&CC=" + patente.getLocalizacion() US&NR=2015110598A1&KC=A1"
			String sURL = "http://worldwide.espacenet.com/publicationDetails/biblio?DB=worldwide.espacenet.com";
			sURL += "&II=0&ND=3&adjacent=true&locale=en_EP&FT=D&CC=" + patente.getLocalizacion() + "&NR=" + patente.docNumber + patente.kind + "&KC=" + patente.kind;
			try {
				patente.setUrl(new URL(sURL));
			} catch (MalformedURLException e2) {
				log.error("Error en la URL de la Patente: " + sURL);
				e2.printStackTrace();
			}
			new OPSCrawlerBiblio(patente);
			patente.setDocId(patente.getLocalizacion() + patente.docNumber + patente.kind);
			log.info(patente);
			Delphos.getSession().beginTransaction();
			try {
				Delphos.getSession().save(patente);
				Delphos.getSession().getTransaction().commit();
			} catch (ConstraintViolationException e) {
				Delphos.getSession().getTransaction().rollback();
				log.trace("Patente duplicada.");
				Delphos.reconectarSesion();
			}
			break;
		case "error":
			if ("invalid_token".equals(error_message)) {
				obtenerToken();
				procesarCPI(cpi, anio);
			} else {
				log.error("ERROR (" + error_code + ") " + error_message + "\n" + error_description);
				System.exit(-1);
			}
			break;
		}

		// http://worldwide.espacenet.com/publicationDetails/biblio?DB=worldwide.espacenet.com&II=0&ND=3&adjacent=true&locale=en_EP&FT=D&date=20150331&CC=US&NR=8995573B1&KC=B1
	}

	private boolean estaEnBD(String docId) {
		Query query = Delphos.getSession().createQuery("FROM Patente where docId = :docId");
		query.setParameter("docId", docId);

		return (query.list().size() > 0);
	}

	public void characters(char ch[], int start, int length) throws SAXException {

		String tagAbierto = control.peek();
		log.trace("TagAbierto: " + tagAbierto);

		switch (tagAbierto) {
		// case "ops:biblio-search":
		case "doc-number":
			patente.docNumber = new String(ch, start, length);
			log.info("DocNumber: " + patente.docNumber);
			break;
		case "country":
			patente.setLocalizacion(new String(ch, start, length));
			log.info("Country: " + patente.getLocalizacion());
			break;
		case "kind":
			patente.kind = new String(ch, start, length);
			log.info("Kind: " + patente.kind);
			break;
		case "code":
			if (error)
				error_code = new String(ch, start, length);
			break;
		case "message":
			if (error)
				error_message = new String(ch, start, length);
			break;
		case "description":
			if (error)
				error_description = new String(ch, start, length);
			break;
		}
	}
}
