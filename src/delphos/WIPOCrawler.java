package delphos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WIPOCrawler {
	
	static HttpURLConnection conn;
	static String cookie = null;
	static int numDocs = 1;

	public static void main(String[] args) throws Exception {
//		CloseableHttpClient httpclient = HttpClients.createDefault();
//        HttpGet httpget = new HttpGet("http://localhost/");
            
		URL url;
		String html;
		String postParameters = null;
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

		url = new URL("http://patentscope.wipo.int/search/en/search.jsf");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Referer", "http://patentscope.wipo.int/search/en/search.jsf");
		html = verPagina(url, postParameters);
		escribirFichero(html);
		System.exit(3);


		// 1. Pedimos la página
		url = new URL("http://patentscope.wipo.int/search/en/search.jsf");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Referer", "http://patentscope.wipo.int/search/en/search.jsf");
		postParameters = "simpleSearchSearchForm=simpleSearchSearchForm";
		postParameters += "&simpleSearchSearchForm%3Aj_idt345=FP";
		postParameters += "&simpleSearchSearchForm%3AfpSearch=Energy";
		postParameters += "&simpleSearchSearchForm%3AcommandSimpleFPSearch=Search";
		postParameters += "&simpleSearchSearchForm%3Aj_idt370=workaround";
	
		System.out.println(postParameters);
		html = verPagina(url, postParameters);
		escribirFichero(html);
		System.exit(3);
		
		//Buscamos el número de páginas
		int maxPag = verNumPag(html);
		
		obtenerDocumentos(html);	//Procesamos la primera página
		
		//Obtener las siguientes páginas
		for (int pagina = 2; pagina <= maxPag; pagina ++){
			System.out.print("\nProcesando página " + pagina +": ");
			//System.out.println("http://ted.europa.eu/TED/search/searchResult.do?page=" + pagina);
			url = new URL("http://ted.europa.eu/TED/search/searchResult.do?page=" + pagina);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Referer", "http://ted.europa.eu/TED/search/searchResult.do");
			html = verPagina(url, null);
			obtenerDocumentos(html);
//			escribirFichero(html);
//			System.out.println(html);
//			System.in.read();
		}
		
		System.out.println("\nFin de Programa.");

	}

	private static int verNumPag(String html) {
		int resultado;
		
		Document doc = Jsoup.parse(html);
		Elements nodos = doc.select("a:containsOwn(Last)");
		String texto = ((Element)nodos.get(0)).attr("href");
		resultado = Integer.parseInt(texto.substring(texto.indexOf("=") + 1, texto.length()));
		System.out.println("Hay " + resultado + " páginas de resultado");
		return resultado;
	}

	private static void obtenerDocumentos(String html) throws Exception {
		Document doc = Jsoup.parse(html);
		Elements nodos = doc.select("table[id=notice] > tbody a");
		System.out.println("Hay "+ nodos.size() + " enlaces.");
		Iterator it = nodos.iterator();
		while (it.hasNext()) {
			Element link = (Element) it.next();
			System.out.println(numDocs++ + ") " + link.text() + " a " + "http://ted.europa.eu" + link.attr("href"));
			System.out.println("\nURL: " + "http://ted.europa.eu" + link.attr("href"));
			URL urlHTML = new URL("http://ted.europa.eu" + link.attr("href") + "&tabId=3");	//Directamente a la pestaña de Datos
			conn = (HttpURLConnection) urlHTML.openConnection();
			String docHTML = verPagina(urlHTML, null);
			parsear(docHTML);
			urlHTML = new URL("http://ted.europa.eu" + link.attr("href") + "&tabId=3");	//Directamente a la pestaña de Datos
			conn = (HttpURLConnection) urlHTML.openConnection();
			String que = verPagina(urlHTML, null);
			System.out.println("Qué: " + que);
		}
		
	}

	private static void parsear(String docHTML) {
		Document doc = Jsoup.parse(docHTML);
		
		//Obtenemos el título:
		Elements nodos = doc.select("th:containsOwn(TI) + td + td");
		String titulo = nodos.get(0).text();
		System.out.println("Título: " + titulo);
		
		//Obtenemos Quien:
		nodos = doc.select("th:containsOwn(AU) + td + td");
		String quien = nodos.get(0).text();
		System.out.println("Quién: " + quien);

		//Obtenemos Donde:
		nodos = doc.select("th:containsOwn(TW) + td + td");
		String donde = nodos.get(0).text();
		nodos = doc.select("th:containsOwn(CY) + td + td");
		donde += " - " + nodos.get(0).text();
		System.out.println("Dónde: " + donde);
		
		//Obtenemos Cuando:
		nodos = doc.select("th:containsOwn(PD) + td + td");
		String cuando = nodos.get(0).text();
		System.out.println("Cuándo: " + cuando);
		
	}

	private static String verPagina(URL url, String postParameters) throws Exception {
		System.out.println("\nConectando a : " + url);
		conn.setInstanceFollowRedirects(true);
		HttpURLConnection.setFollowRedirects(true);

		if (cookie != null){
			conn.setRequestProperty("Cookie", cookie);
			System.out.println("Poniendo Cookie: " + cookie);
		}

		// Establecemos las cabeceras
		conn.setReadTimeout(5000);
		conn.setRequestProperty("Host", "patentscope.wipo.int");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:33.0) Gecko/20100101 Firefox/33.0");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3");
		conn.setRequestProperty("Accept-Encoding","gzip, deflate");
		conn.setRequestProperty("DNT","1");
		conn.addRequestProperty("Connection", "keep-alive");

		if(postParameters != null){
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setInstanceFollowRedirects(true); 
			conn.setRequestMethod("POST"); 
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", "" + Integer.toString(postParameters.getBytes().length));
			conn.setUseCaches (false);
			
//			System.out.println("Poniendo parámetros de POST: " + postParameters);
			
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(postParameters);
			wr.flush();
			wr.close();			
		}
		
		boolean redirect = false;

		//Vemos las cabeceras
		System.out.println("Respuesta: " + conn.getResponseCode());
		Map<String, List<String>> map = conn.getHeaderFields();
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			System.out.println("Key : " + entry.getKey() + 
	                 " ,Value : " + entry.getValue());
		}
		
//		System.out.println("Procesando respuesta:");
		int status = conn.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
		}

		if (redirect) {

			// get redirect url from "location" header field
			String newUrl = conn.getHeaderField("Location");

			// get the cookie if need, for login
			if (conn.getHeaderField("Set-Cookie") != null){
				cookie = conn.getHeaderField("Set-Cookie");
				System.out.println("Cookie: " + cookie);
			}

			System.out.println("Redirect to URL : " + newUrl);
			conn = (HttpURLConnection) url.openConnection();
			return verPagina(new URL(newUrl), postParameters);
		}

		
//GZIPInputStream  gzip = new GZIPInputStream (new ByteArrayInputStream (tBytes));
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String inputLine;
		StringBuffer html = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			html.append(inputLine);
		}
		in.close();

		System.out.println("URL Content... \n" + html.toString());
		
		return html.toString();		
	}

	private static void escribirFichero(String texto){
		try{
		File fichero = new File("/tmp/wipo.html");
		BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
        writer.write(texto);
        
        writer.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
