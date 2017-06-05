package delphos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.net.util.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import delphos.iu.Delphos;

public class VigilanteTecnologico extends TimerTask {
	static HttpURLConnection conn;
	private static CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
	static String cookie = null;

	public static void main(String args[]) {
		new VigilanteTecnologico().run();
	}
	
	@Override
	public void run() {
		System.out.println("Iniciando Vigilante Tecnológico.");
		
		ArrayList<TerminoTecnologiaEmergente> terminosTecnologiasEmergentes = Crawler.cargarTerminosTecnologiasEmergentes();
		for (TerminoTecnologiaEmergente terminoTE : terminosTecnologiasEmergentes) {
			buscarAvisosTED(terminoTE);
			buscarAvisosEPO(terminoTE);
			buscarAvisosDSpace(terminoTE);
		}
	}

	private void buscarAvisosDSpace(TerminoTecnologiaEmergente terminoTE) {
		try {
			URL url = new URL("http://dspace.mit.edu/advanced-search");
			conn = (HttpURLConnection) url.openConnection();
			String params = "num_search_field=3&results_per_page=100&";
			params += "scope=%2F&";
			params += "field1=ANY";
			params += "&page=1";
			params += "&query1=" + terminoTE.getTermino().replace(" ", "+");
			params += "&conjunction2=AND&field2=";
			params += "ANY&query2=";
			params += "&conjunction3=AND&field3=ANY&query3=&rpp=10&sort_by=2&order=DESC&submit=Ir";
			
			String html = verPagina(url, params);
			escribirFichero(html);

			Document doc = Jsoup.parse(html);
			Elements listaLi = doc.select("li.ds-artifact-item");
			UltimoAvisoDSpace ultimoAviso = null;
			UltimoAvisoDSpace uad = null;
			for (Element li : listaLi) {
				Element aTitulo = li.select("div.artifact-title>a").first();
				String docTitulo = aTitulo.text();
				String docUrl = "http://dspace.mit.edu" + aTitulo.attr("href");
				String docAutor = li.select("span.author").text();
				String docEntidad = li.select("span.publisher").text();
				String docFechaPublicacion = li.select("span.date").text();
				String docResumen = li.select(".artifact-abstract").text();
				Document docDetalle = Jsoup.connect(docUrl + "?show=full").get();
				System.out.println("Título:" + docTitulo);
				System.out.println("URL: " + docUrl);
				System.out.println("Autor:" + docAutor);
				System.out.println("Entidad: " + docEntidad);
				System.out.println("FechaPublicacion: " + docFechaPublicacion);
				System.out.println("Resumen: " + docResumen);
				System.out.println();
				AvisoTecnologiasEmergentes aviso = new AvisoTecnologiasEmergentes();
				aviso.setTermino(terminoTE.getTermino());
				aviso.setTitulo(docTitulo);
				aviso.setUrl(docUrl);
				aviso.setExtracto(docResumen);
				aviso.setTipo("Documento Académico");
				aviso.setRevisado(false);
				aviso.setFecha(new Timestamp(System.currentTimeMillis()));

				//Guardamos el primero
				if (ultimoAviso == null){
					ultimoAviso = new UltimoAvisoDSpace(terminoTE.getTermino(), docUrl);
				}
				//Buscamos el último aviso registrado para este término
				uad = UltimoAvisoDSpace.buscar(terminoTE.getTermino());
				if (uad != null){
					//Si ya hemos llegado al último que teníamos registrado, paramos
					if (uad.getUrl().equals(docUrl))
						break;
				}
				Delphos.getSession().beginTransaction();
				Delphos.getSession().save(aviso);
				Delphos.getSession().getTransaction().commit();
			}
			if (ultimoAviso != null){
				if (uad==null){ //No había avisos previos
					Delphos.getSession().beginTransaction();
					Delphos.getSession().save(ultimoAviso);
					Delphos.getSession().getTransaction().commit();
				}
				else{
					//Actualizamos el último aviso del término
					uad.setUrl(ultimoAviso.getUrl());
					Delphos.getSession().beginTransaction();
					Delphos.getSession().save(uad);
					Delphos.getSession().getTransaction().commit();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void buscarAvisosTED(TerminoTecnologiaEmergente terminoTE) {
		System.out.println("Buscando avisos para " + terminoTE.getTerminoPrincipal() + " - " + terminoTE.getTermino());
		URL url;
		String html;
		String postParameters;
		CookieHandler.setDefault(cookieManager);

		try {

			url = new URL("http://ted.europa.eu/TED/");
			conn = (HttpURLConnection) url.openConnection();
			html = verPagina(url, null);
			System.out.println("\n\n  ------------ Página de Selección de Idioma CONSEGUIDA ---------------\n\n");

			url = new URL("http://ted.europa.eu/TED/misc/chooseLanguage.do?lgId=en");
			conn = (HttpURLConnection) url.openConnection();
			postParameters = "action=cl";
			html = verPagina(url, postParameters);
			System.out.println("\n\n  ------------ Idioma Seleccionado ---------------\n\n");

			url = new URL("http://ted.europa.eu/TED/search/search.do");
			conn = (HttpURLConnection) url.openConnection();
			html = verPagina(url, null);
			System.out.println("\n\n  ------------ Página de Búsqueda CONSEGUIDA ---------------\n\n");

			// 2. Búsqueda
			url = new URL("http://ted.europa.eu/TED/search/search.do?");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Referer", "http://ted.europa.eu/TED/search/search.do");
			postParameters = "action=search";
			postParameters += "&Rs.gp.11500808.pid=home";
			postParameters += "&lgId=en";
			postParameters += "&Rs.gp.11500809.pid=releaseCalendar";
			postParameters += "&quickSearchCriteria=";
			postParameters += "&Rs.gp.11500811.pid=secured";
			postParameters += "&searchCriteria.searchScopeId=1"; // Last Edition
			postParameters += "&searchCriteria.ojs=";
			postParameters += "&searchCriteria.freeText=";
			postParameters += terminoTE.getTermino().replace(" ", "+");
			postParameters += "&searchCriteria.countryList=";
			postParameters += "&searchCriteria.contractList=";
			postParameters += "&searchCriteria.documentTypeList=";
			postParameters += "'Contract+notice','Contract+award'";

			// postParameters +=
			// "&searchCriteria.cpvCodeList=%27Construction+and+Real+Estate%27";
			postParameters += "&searchCriteria.cpvCodeList=";
			postParameters += "&searchCriteria.publicationDateChoice=DEFINITE_PUBLICATION_DATE";
			java.util.Date hoy = new java.util.Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			postParameters += "&searchCriteria.publicationDate=" + sdf.format(hoy);
			postParameters += "&searchCriteria.documentationDate=";
			postParameters += "&searchCriteria.place=";
			postParameters += "&searchCriteria.procedureList=";
			postParameters += "&searchCriteria.regulationList=";
			postParameters += "&searchCriteria.nutsCodeList=";
			postParameters += "&searchCriteria.documentNumber=";
			postParameters += "&searchCriteria.deadline=";
			postParameters += "&searchCriteria.authorityName=";
			postParameters += "&searchCriteria.mainActivityList=";
			postParameters += "&searchCriteria.directiveList=";
			postParameters += "&_searchCriteria.statisticsMode=on";

			System.out.println("Parámetros POST: " + postParameters);
			html = verPagina(url, postParameters);
			escribirFichero(html);

			int numPaginas = verNumPag(html);

			if (numPaginas > 0)
				obtenerDocumentosTED(html, terminoTE.getTerminoPrincipal());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String verPagina(URL url, String postParameters) throws Exception {
		conn.setInstanceFollowRedirects(true);
		HttpURLConnection.setFollowRedirects(true);

		// Establecemos las cabeceras
		conn.setReadTimeout(5000);
		conn.setRequestProperty("Host", "ted.europa.eu");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:33.0) Gecko/20100101 Firefox/33.0");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3");
		conn.setRequestProperty("Accept-Encoding", "deflate");
		conn.setRequestProperty("DNT", "1");
		conn.addRequestProperty("Connection", "keep-alive");

		if (postParameters != null) {
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", "" + Integer.toString(postParameters.getBytes().length));
			conn.setUseCaches(false);

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(postParameters);
			wr.flush();
			wr.close();
		}

		boolean redirect = false;
		int status = conn.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
		}

		if (redirect) {

			// get redirect url from "location" header field
			String newUrl = conn.getHeaderField("Location");

			// get the cookie if need, for login
			if (conn.getHeaderField("Set-Cookie") != null) {
				cookie = conn.getHeaderField("Set-Cookie");
			}

			conn = (HttpURLConnection) url.openConnection();
			return verPagina(new URL(newUrl), postParameters);
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer html = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			html.append(inputLine);
		}
		in.close();

		return html.toString();
	}

	private static void escribirFichero(String texto) {
		try {
			File fichero = new File("/tmp/ted.html");
			BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
			writer.write(texto);

			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int verNumPag(String html) {
		int resultado = 0;
		System.out.println(html);
		Document doc = Jsoup.parse(html);
		Elements nodos = doc.select("a:containsOwn(Last)");
		if (nodos.size() > 0) {
			String texto = ((Element) nodos.get(0)).attr("href");
			resultado = Integer.parseInt(texto.substring(texto.indexOf("=") + 1, texto.length()));
		}
		System.out.println("Hay " + resultado + " páginas de resultado");
		return resultado;
	}

	private static void obtenerDocumentosTED(String html, String terminoPrincipal) throws Exception {
		Document doc = Jsoup.parse(html);
		Elements nodos = doc.select("table[id=notice] > tbody a");
		System.out.println("Hay " + nodos.size() + " enlaces.");
		Iterator it = nodos.iterator();
		while (it.hasNext()) {
			AvisoTecnologiasEmergentes aviso = new AvisoTecnologiasEmergentes();
			aviso.setTermino(terminoPrincipal);
			aviso.setTipo("Licitación");
			Element link = (Element) it.next();
			// System.out.println(numDocs++ + ") " + link.text() + " a " +
			// "http://ted.europa.eu" + link.attr("href"));
			URL urlHTML = new URL("http://ted.europa.eu" + link.attr("href") + "&tabId=3"); // Directamente
																							// a
																							// la
																							// pestaña
																							// de
																							// Datos
			aviso.setUrl(urlHTML.toString());
			System.out.println("URL: " + urlHTML.toString());
			conn = (HttpURLConnection) urlHTML.openConnection();
			String docHTML = verPagina(urlHTML, null);
			// escribirFichero(docHTML);
			Document doc2 = Jsoup.parse(docHTML);
			// Obtenemos el título:
			Elements nodos2 = doc2.select("th:containsOwn(TI) + td + td");
			String titulo = nodos2.get(0).text();
			aviso.setTitulo(titulo);

			urlHTML = new URL("http://ted.europa.eu" + link.attr("href") + "&tabId=2"); // Directamente
																						// a
																						// la
																						// pestaña
																						// de
																						// Datos
			conn = (HttpURLConnection) urlHTML.openConnection();
			docHTML = verPagina(urlHTML, null);
			doc = Jsoup.parse(docHTML);
			nodos = doc.select("span.nomark:matchesOwn(^II.1.4) + span + div");
			if (nodos.size() == 0)
				nodos = doc.select("span.nomark:matchesOwn(^II.1.5) + span + div");
			if (nodos.size() > 0)
				aviso.setExtracto(nodos.get(0).text());

			Delphos.getSession().beginTransaction();
			Delphos.getSession().save(aviso);
			Delphos.getSession().getTransaction().commit();

		}

	}

	private static void buscarAvisosEPO(TerminoTecnologiaEmergente terminoTE) {
		// Búsqueda de Documentos en EPO
		String scheme = "https";
		String authority = "ops.epo.org";
		String path = "/3.1/rest-services/published-data/search";

		ArrayList<String> condiciones = new ArrayList<String>();

		ArrayList<String> alCPI = new ArrayList<String>();

		condiciones.add("titleandabstract=\"" + terminoTE.getTermino() + "\"");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Timestamp hoy = new Timestamp(System.currentTimeMillis());
		condiciones.add("publicationdate=" + sdf.format(hoy));
		//condiciones.add("publicationdate>=20150101");

		String query = "q=";
		for (int i = 0; i < condiciones.size(); i++) {
			if (i == 0)
				query += condiciones.get(i);
			else
				query += " and " + condiciones.get(i);
		}

		System.out.println("query: " + query);

		try {
			URI uri = new URI(scheme, authority, path, query, null);

			HttpsURLConnection con = (HttpsURLConnection) (uri.toURL().openConnection());

			con.setRequestMethod("GET");
			String token = obtenerTokenEPO();
			con.setRequestProperty("Authorization", "Bearer " + token);
			// String Range = "" + indiceBusquedaEPO + "-" + (indiceBusquedaEPO
			// + 99);
			// System.out.println(Range);
			// con.setRequestProperty("X-OPS-Range", Range);

			// Send get request
			con.setDoOutput(true);

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URI : " + uri);
			System.out.println("Response Code : " + responseCode);

			/*
			 * BufferedReader in = new BufferedReader( new
			 * InputStreamReader(uri.toURL().openStream())); String inputLine;
			 * while ((inputLine = in.readLine()) != null)
			 * System.out.println(inputLine); in.close();
			 */

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(con.getInputStream());

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			Searcher.totalResultadosEPO = Integer.parseInt(doc.getElementsByTagName("ops:biblio-search").item(0).getAttributes().getNamedItem("total-result-count").getTextContent());
			System.out.println("Total Resultados: " + Searcher.totalResultadosEPO);
			org.w3c.dom.NodeList docList = doc.getElementsByTagName("ops:publication-reference");
			System.out.println("Encontradas " + docList.getLength() + " patentes.");

			for (int i = 0; i < docList.getLength(); i++) {
				if (docList.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
					org.w3c.dom.Element elemento = (org.w3c.dom.Element) docList.item(i);
					Patente patente = new Patente();

					patente.documentIdType = elemento.getElementsByTagName("document-id").item(0).getAttributes().getNamedItem("document-id-type").getTextContent();
					patente.docNumber = elemento.getElementsByTagName("doc-number").item(0).getTextContent();
					patente.kind = elemento.getElementsByTagName("kind").item(0).getTextContent();
					patente.setLocalizacion(elemento.getElementsByTagName("country").item(0).getTextContent());

					String sURL = "http://worldwide.espacenet.com/publicationDetails/biblio?DB=worldwide.espacenet.com";
					sURL += "&II=0&ND=3&adjacent=true&locale=en_EP&FT=D&CC=" + patente.getLocalizacion() + "&NR=" + patente.docNumber + patente.kind + "&KC=" + patente.kind;
					try {
						patente.setUrl(new URL(sURL));
					} catch (MalformedURLException e2) {
						System.out.println("Error en la URL de la Patente: " + sURL);
						e2.printStackTrace();
					}
					completarPatente(patente, token);
					System.out.println(patente);
					AvisoTecnologiasEmergentes aviso = new AvisoTecnologiasEmergentes();
					aviso.setTipo("Patente");
					aviso.setTermino(terminoTE.getTerminoPrincipal());
					aviso.setTitulo(patente.getTitulo());
					aviso.setExtracto(patente.getResumen());
					aviso.setUrl(patente.getUrl().toString());
					
					Delphos.getSession().beginTransaction();
					Delphos.getSession().save(aviso);
					Delphos.getSession().getTransaction().commit();
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
			// System.exit(-1);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static String obtenerTokenEPO() {
		String token = null;
		// Login en EPO

		// Step 1
		String authorization = new String(Base64.encodeBase64((Searcher.consumerKey + ":" + Searcher.consumerSecretKey).getBytes()));

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
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);

			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			// print result
			System.out.println(response.toString());
			String aguja = "\"access_token\" : \"";
			int inicio = response.indexOf(aguja) + aguja.length();
			int fin = response.indexOf("\"", inicio + 1);
			token = response.substring(inicio, fin);
			System.out.println("Token: " + token);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			//System.exit(-1);
		} catch (IOException e2) {
			e2.printStackTrace();
			//System.exit(-1);
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

	private static void completarPatente(Patente patente, String token) {
		// Recuperación de datos bibliográficos
		String scheme = "https";
		String authority = "ops.epo.org";
		String path = "/3.1/rest-services/published-data/publication/";

		// https://ops.epo.org/3.1/rest-services/published-data/publication/docdb/US.8995573/biblio
		path += patente.documentIdType + "/" + patente.getLocalizacion() + "." + patente.docNumber + "." + patente.kind + "/biblio";
		try {
			URI uri = new URI(scheme, authority, path, null);
			System.out.println("URI: " + uri.toString());

			HttpsURLConnection con = (HttpsURLConnection) (uri.toURL().openConnection());

			// add request header
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", "Bearer " + token);

			// Send get request
			con.setDoOutput(true);

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URI : " + uri);
			System.out.println("Response Code : " + responseCode);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(con.getInputStream());

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			String titulo = "";
			org.w3c.dom.NodeList listaTitulo = doc.getElementsByTagName("invention-title");
			for (int i = 0; i < listaTitulo.getLength(); i++) {
				if (titulo != "")
					titulo += ", ";
				titulo += ((org.w3c.dom.Element) listaTitulo.item(i)).getTextContent();
			}
			patente.setTitulo(titulo);

			String inventor = "";
			org.w3c.dom.NodeList listaInventores = doc.getElementsByTagName("inventor");
			for (int i = 0; i < listaInventores.getLength(); i++) {
				if (inventor != "")
					inventor += ", ";
				inventor += ((org.w3c.dom.Element) listaInventores.item(i)).getElementsByTagName("name").item(0).getTextContent();
			}
			patente.setInventor(inventor);

			String solicitante = "";
			org.w3c.dom.NodeList listaSolicitantes = doc.getElementsByTagName("applicant");
			for (int i = 0; i < listaSolicitantes.getLength(); i++) {
				if (solicitante != "")
					solicitante += ", ";
				solicitante += ((org.w3c.dom.Element) listaSolicitantes.item(i)).getElementsByTagName("name").item(0).getTextContent();
				inventor += ", ";
			}
			patente.setSolicitante(solicitante);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String sFecha = ((org.w3c.dom.Element) doc.getElementsByTagName("publication-reference").item(0)).getElementsByTagName("date").item(0).getTextContent();
			try {
				patente.setFechaPublicacion(sdf.parse(sFecha));
			} catch (ParseException e) {
				System.out.println("Error en Fecha de Publicación: " + sFecha);
				e.printStackTrace();
			}

			String resumen = "";
			org.w3c.dom.NodeList listaResumen = doc.getElementsByTagName("abstract");
			for (int i = 0; i < listaResumen.getLength(); i++)
				for (int j = 0; j < ((org.w3c.dom.Element) listaResumen.item(i)).getElementsByTagName("p").getLength(); j++)
					resumen += ((org.w3c.dom.Element) listaResumen.item(i)).getElementsByTagName("p").item(j).getTextContent();
			patente.setResumen(resumen);

		} catch (URISyntaxException e) {
			e.printStackTrace();
			// System.exit(-1);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
