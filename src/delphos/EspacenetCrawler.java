package delphos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import delphos.iu.Delphos;

public class EspacenetCrawler implements Runnable {
	private final static Logger log = Logger.getLogger(EspacenetCrawler.class);
//	String sUrl = "http://worldwide.espacenet.com/searchResults?submitted=true&locale=en_EP&DB=EPODOC&ST=advanced&TI=&AB=&PN=&AP=&PR=&PD=&PA=&IN=&CPC=&IC=H02B1%2F01&Submit=Search";
	//String sUrl = "http://worldwide.espacenet.com/searchResults?DB=EPODOC&ST=advanced&locale=en_EP&CPC=H02B13/025/low";
	
	//Búsqueda por CPI=C10B (low = con subcategorías)
	String sBaseUrl = "http://worldwide.espacenet.com/searchResults?";
	String sParamUrl = "DB=EPODOC&ST=advanced&locale=en_EP&CPC=C10B/low";
	//2ª Página: http://worldwide.espacenet.com/searchResults?page=1&ST=advanced&locale=en_EP&DB=EPODOC&CPC=C10B/low
	
	HttpURLConnection conn;
	String cookie = null;
	URL url;

	public static void main(String[] args) {
		// Thread tr = new Thread(new EspacenetCrawler());
		// tr.start();
		new EspacenetCrawler().run();
	}

	@Override
	public void run() {
		BasicConfigurator.configure();
		log.info("Arrancando.");

		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

		String html;
		PrintWriter out = null;
		try {
			//TODO: Consultar todos los CPI
			//TODO: Siguiente página
			String sUrl = "";
			int pagina = 0;
			while (true){
				System.out.println("\n\nProcesando página " + pagina + "\n");
				if (pagina == 0)
					sUrl = sBaseUrl + sParamUrl;
				else
					sUrl = sBaseUrl + "page=" + pagina + "&" + sParamUrl;
				System.out.println("URL: " + sUrl);
				html = obtenerPagina(new URL(sUrl));
				
				out = new PrintWriter("log-EspacenetCrawler.html");
				out.println(html);
				
				log.trace("HTML:" + html.length());
				parsear(html);
				pagina++;
			}
		} catch (MalformedURLException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			out.close();
		}
	}

	private String obtenerPagina(URL url) {
		StringBuffer html = new StringBuffer();
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setInstanceFollowRedirects(true);

			// // Establecemos las cabeceras
			// conn.setReadTimeout(5000);
			conn.setRequestProperty("Host", "wo.espacenet.com");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:33.0) Gecko/20100101 Firefox/33.0");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Language", "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			conn.setRequestProperty("DNT", "1");
			conn.setRequestProperty("Referer", "http://lp.espacenet.com/advancedSearch?locale=es_LP");
			conn.addRequestProperty("Connection", "keep-alive");

			int status = conn.getResponseCode();
			log.trace("ResponseCode: " + status);
			if (status != HttpURLConnection.HTTP_OK) {
				if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)
					log.trace("Redirección!");
			}

			log.trace("Status = " + status);
			String key;
			log.trace("Headers-------start-----");
			for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
				log.trace(key + ":" + conn.getHeaderField(i));
			}
			log.trace("Headers-------end-----");
			log.trace("Content-------start-----");
			String inputLine;
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((inputLine = in.readLine()) != null) {
				log.trace(inputLine);
				html.append(inputLine);
			}
			log.trace("Content-------end-----");
			in.close();

			log.trace("Terminado.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return html.toString();
	}

	private void parsear(String html) throws FileNotFoundException {
		ArrayList<Patente> listaPatentes = new ArrayList<Patente>();
		Document doc = Jsoup.parse(html);

		Element tablaAplicacion = doc.select("table.application").get(0);
		log.trace("Nodo tabla aplicación encontrado.");
		Elements listaTitleRowClass = tablaAplicacion.select("tr.titleRowClass");
		for (Element trTitleRowClass : listaTitleRowClass) {
			Patente pat = new Patente();
			pat.setTitulo(trTitleRowClass.select("a.publicationLinkClass").text().replaceAll("[^\\x20-\\x7e]", ""));
			try {
				pat.setUrl(new URL("http://worldwide.espacenet.com/" + trTitleRowClass.select("a.publicationLinkClass").attr("href")));
			} catch (MalformedURLException e) {
				log.error("No pudo crearse la url " + "http://worldwide.espacenet.com/" + trTitleRowClass.select("a.publicationLinkClass").attr("href"));
				e.printStackTrace();
			}
			Element trContentRowClass = trTitleRowClass.nextElementSibling();
			pat.setInventor(trContentRowClass.select("td.inventorColumn span").text().replaceAll("[^\\x20-\\x7e]", ""));
			pat.setSolicitante(trContentRowClass.select("td.applicantColumn span").text().replaceAll("[^\\x20-\\x7e]", ""));
			Elements listaElementosCPI = trContentRowClass.select("div.ipcColumn a.ipc");
			for (Element aCPI : listaElementosCPI)
				pat.getCpi().addAll(CPI.getCPIs(aCPI.text()));
			
			String infoPublicacion = trContentRowClass.select("td.publicationInfoColumn").get(0).text();
			log.trace("Info Publicación:" + infoPublicacion);
			if (infoPublicacion.length() > 20) {
				pat.setLocalizacion(infoPublicacion.substring("Publication info: ".length(), "Publication info: ".length() + 2));
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				try {
					pat.setFechaPublicacion(sdf.parse(infoPublicacion.substring(infoPublicacion.length() - 10, infoPublicacion.length())));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			//Vamos a por la página con el resumen
			String htmlResumen = obtenerPagina(pat.getUrl());
			PrintWriter out = new PrintWriter("log-EspacenetCrawlerResumen.html");
			out.println(htmlResumen);
			out.close();
			doc = Jsoup.parse(htmlResumen);
			pat.setResumen(doc.select("p.printAbstract").get(0).text().replaceAll("[^\\x20-\\x7e]", ""));
			
			System.out.println(pat);
//			try {
//				System.in.read();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			Delphos.getSession().beginTransaction();
			Delphos.getSession().save(pat);
			Delphos.getSession().getTransaction().commit();
		}
	}
}
