package delphos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Stack;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OPSCrawlerBiblio extends DefaultHandler {
	private final static Logger log = Logger.getLogger(OPSCrawlerBiblio.class);
	Patente patente;
	// Variables de control de SAXParser
	boolean error;
	String error_code = null;
	String error_message = null;
	String error_description = null;
	private static Stack<String> control = new Stack<String>();
	boolean inventor_epodoc = false;
	boolean applicant_epodoc = false;
	boolean publication_reference = false;
	boolean classification_ipcr = false;
	boolean ingles = false;
	boolean enAbstract = false;

	public OPSCrawlerBiblio() {
		super();
	}

	public OPSCrawlerBiblio(Patente patente) {
		super();
		this.patente = patente;

		// Recuperación de datos bibliográficos
		String scheme = "https";
		String authority = "ops.epo.org";
		String path = "/3.1/rest-services/published-data/publication/";

		// https://ops.epo.org/3.1/rest-services/published-data/publication/docdb/US.8995573/biblio
		path += patente.documentIdType + "/" + patente.getLocalizacion() + "." + patente.docNumber + "." + patente.kind + "/biblio";
		try {
			URI uri = new URI(scheme, authority, path, null);
			log.info("URI: " + uri.toString());

			HttpsURLConnection con = (HttpsURLConnection) (uri.toURL().openConnection());

			// add request header
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", "Bearer " + OPSCrawler.token);

			// Send get request
			con.setDoOutput(true);

			int responseCode = con.getResponseCode();
			log.trace("\nSending 'GET' request to URI : " + uri);
			log.trace("Response Code : " + responseCode);

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(con.getInputStream(), this);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			//System.exit(-1);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			//System.exit(-1);
		} catch (SAXException e) {
			e.printStackTrace();
			//System.exit(-1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			//System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			//System.exit(-1);
		}

	}

	// Métodos del DefaultHandler de SAXParser
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		log.trace("Start Element :" + qName);

		// Aquí leemos atributos
		control.push(qName);
		switch (qName) {
		case "error":
			error = true;
			break;
		case "inventor":
			if ("epodoc".equals(attributes.getValue("data-format")))
				inventor_epodoc = true;
			break;
		case "applicant":
			if ("epodoc".equals(attributes.getValue("data-format")))
				applicant_epodoc = true;
			break;
		case "publication-reference":
			publication_reference = true;
			break;
		case "classification-ipcr":
			classification_ipcr = true;
			break;
		case "abstract":
			enAbstract = true;
			if ("en".equals(attributes.getValue("lang")))
				ingles = true;
			break;
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {

		log.trace("End Element :" + qName);

		switch (control.pop()) {
		case "inventor":
			inventor_epodoc = false;
			break;
		case "applicant":
			applicant_epodoc = false;
			break;
		case "publication-reference":
			publication_reference = false;
			break;
		case "classification-ipcr":
			classification_ipcr = false;
			break;
		case "abstract":
			enAbstract = false;
			ingles = false;
			break;
		case "error":
			if ("invalid_token".equals(error_message)) {
				OPSCrawler.obtenerToken();
				new OPSCrawlerBiblio(OPSCrawler.patente);
			} else {
				log.error("ERROR (" + error_code + ") " + error_message + "\n" + error_description);
				System.exit(-1);
			}
			break;
		}

		// http://worldwide.espacenet.com/publicationDetails/biblio?DB=worldwide.espacenet.com&II=0&ND=3&adjacent=true&locale=en_EP&FT=D&date=20150331&CC=US&NR=8995573B1&KC=B1
	}

	public void characters(char ch[], int start, int length) throws SAXException {

		String tagAbierto = control.peek();
		log.trace(tagAbierto);

		switch (tagAbierto) {
		case "invention-title":
			patente.setTitulo(new String(ch, start, length));
			break;
		case "name":
			if (inventor_epodoc)
				if (patente.getInventor() == null)
					patente.setInventor(new String(ch, start, length));
				else
					patente.setInventor(patente.getInventor() + "," + new String(ch, start, length));
			if (applicant_epodoc)
				if (patente.getSolicitante() == null)
					patente.setSolicitante(new String(ch, start, length));
				else
					patente.setSolicitante(patente.getSolicitante() + "," + new String(ch, start, length));
			break;
		case "date":
			if (publication_reference) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				try {
					patente.setFechaPublicacion(sdf.parse(new String(ch, start, length)));
				} catch (ParseException e) {
					log.error("Error en Fecha de Publicación: " + new String(ch, start, length));
					e.printStackTrace();
				}
			}
			break;
		case "text":
			if (classification_ipcr) {
				String sCPI = new String(ch, start, length);
				sCPI = sCPI.replaceAll("\\s", "");
				List<CPI> lCPI = CPI.getCPIs(sCPI);
				if (lCPI.size() > 0)
					patente.getCpi().addAll(lCPI);
				else
					patente.getCpi().add(new CPI(sCPI));
			}
			break;
		case "p":
			if (enAbstract)
				if ((patente.getResumen() == null) || ingles)
					patente.setResumen(new String(ch, start, length));
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
