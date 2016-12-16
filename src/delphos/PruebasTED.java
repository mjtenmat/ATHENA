package delphos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PruebasTED {

	public static void main(String[] args) throws Exception {
		Response resp;
		Document doc;
		String cookie, url;
		Map<String,String> postParameters = new HashMap<String,String>();
		
//		url = "http://ted.europa.eu/TED/";
//		resp = Jsoup.connect(url)
//				.execute();
//		cookie = resp.cookie("JSESSIONID");
//		System.out.println("\nPedida: " + url);
//		System.out.println("Código: " + resp.statusCode() + " " + resp.statusMessage());
//		System.out.println("Cookies: " + resp.cookies());
//		System.out.println("Url de la respuesta: " + resp.url());
//		System.out.println("Cookie JSESSIONID: " + cookie);
//		url = resp.url().toString();
//		doc = resp.parse();
		//System.out.println(doc.toString());

//		url = "http://ted.europa.eu/TED/misc/chooseLanguage.do?lgId=en";
//		postParameters.put("action", "cl");
//		resp = Jsoup.connect(url)
//				.method(Method.POST)
//				//.cookie("JSESSIONID", cookie)
//				.data(postParameters)
//				.execute();
//		cookie = resp.cookie("JSESSIONID");
//		System.out.println("\nPedida: " + url);
//		System.out.println("Código: " + resp.statusCode() + " " + resp.statusMessage());
//		System.out.println("Cookies: " + resp.cookies());
//		System.out.println("Url de la respuesta: " + resp.url());
//		System.out.println("Cookie JSESSIONID: " + cookie);
//		url = resp.url().toString();
//		doc = resp.parse();
		//System.out.println(doc.toString());

		//Login
		url = "http://ted.europa.eu/TED/";
		resp = Jsoup.connect(url)
				//.method(Method.POST)
				//.cookie("JSESSIONID", cookie)
				//.data(postParameters)
				.followRedirects(true)
				.execute();
		System.out.println("\nPedida: " + url);
		System.out.println("Código: " + resp.statusCode() + " " + resp.statusMessage());
		System.out.println("Cookies: " + resp.cookies());
		System.out.println("Url de la respuesta: " + resp.url());
		System.out.println("Redirección a:" + resp.header("location"));
		
		url = "http://ted.europa.eu/TED/misc/chooseLanguage.do?lgId=en";
		postParameters.put("action", "cl");
		resp = Jsoup.connect(url)
				.method(Method.POST)
				.cookies(resp.cookies())
				.data(postParameters)
				.followRedirects(true)
				.execute();
		System.out.println("\nPedida: " + url);
		System.out.println("Código: " + resp.statusCode() + " " + resp.statusMessage());
		System.out.println("Cookies: " + resp.cookies());
		System.out.println("Url de la respuesta: " + resp.url());
		System.out.println("Redirección a:" + resp.header("location"));
		
		url = "http://ted.europa.eu/TED/search/search.do";
		resp = Jsoup.connect(url)
				//.method(Method.POST)
				.cookies(resp.cookies())
				//.data(postParameters)
				.followRedirects(true)
				.execute();
		System.out.println("\nPedida: " + url);
		System.out.println("Código: " + resp.statusCode() + " " + resp.statusMessage());
		System.out.println("Cookies: " + resp.cookies());
		System.out.println("Url de la respuesta: " + resp.url());
		System.out.println("Redirección a:" + resp.header("location"));
		escribirFichero(resp.parse().toString());

		System.out.println("Lanzamos la consulta");
				
		url = "http://ted.europa.eu/TED/search/search.do";
		postParameters.put("action", "search");
		postParameters.put("Rs.gp.11500808.pid","home");
		postParameters.put("lgId","en");
		postParameters.put("Rs.gp.11500809.pid","releaseCalendar");
		postParameters.put("quickSearchCriteria","");
		postParameters.put("s.gp.11500811.pid","secured");
		postParameters.put("searchCriteria.searchScopeId","4");
		postParameters.put("searchCriteria.ojs","");
		postParameters.put("searchCriteria.freeText","energía");
		postParameters.put("searchCriteria.countryList","AT,BE");
		postParameters.put("searchCriteria.contractList","'Supply+contract','Service+contract'");
		postParameters.put("searchCriteria.documentTypeList","'Contract+notice','Contract+award'");
		postParameters.put("searchCriteria.cpvCodeList","09100000,09200000");
		postParameters.put("searchCriteria.publicationDateChoice","RANGE_PUBLICATION_DATE");
		postParameters.put("searchCriteria.fromPublicationDate","01-01-2010");
		postParameters.put("searchCriteria.toPublicationDate","31-12-2014");
		postParameters.put("searchCriteria.publicationDate","");
		postParameters.put("searchCriteria.documentationDate","");
		postParameters.put("searchCriteria.place","");
		postParameters.put("searchCriteria.procedureList","");
		postParameters.put("searchCriteria.regulationList","");
		postParameters.put("searchCriteria.nutsCodeList","");
		postParameters.put("searchCriteria.documentNumber","");
		postParameters.put("searchCriteria.deadline","");
		postParameters.put("searchCriteria.authorityName","");
		postParameters.put("searchCriteria.mainActivityList","");
		postParameters.put("searchCriteria.directiveList","");
		postParameters.put("searchCriteria.statisticsMode","on");
		resp = Jsoup.connect(url)
				.referrer("http://ted.europa.eu/TED/search/search.do")
				.method(Method.POST)
				.cookies(resp.cookies())
				.data(postParameters)
				.followRedirects(true)
				.execute();
		System.out.println("\nPedida: " + url);
		System.out.println("Código: " + resp.statusCode() + " " + resp.statusMessage());
		System.out.println("Cookies: " + resp.cookies());
		System.out.println("Url de la respuesta: " + resp.url());
		System.out.println("Redirección a:" + resp.header("location"));
		escribirFichero(resp.parse().toString());
		
		System.exit(0);
		
		url = "http://ted.europa.eu/TED/search/search.do?";
		postParameters.put("action", "search");
		//postParameters.put("", "");
		//postParameters.put("Rs.gp.11500808.pid","home");
		postParameters.put("lgId","en");
		//postParameters.put("Rs.gp.11500809.pid","releaseCalendar");
		postParameters.put("quickSearchCriteria","");
		//postParameters.put("s.gp.11500811.pid","secured");
		postParameters.put("searchCriteria.searchScopeId","4");
		postParameters.put("searchCriteria.ojs","");
		postParameters.put("searchCriteria.freeText","energía");
		postParameters.put("searchCriteria.countryList","AT,BE");
		postParameters.put("searchCriteria.contractList","'Supply+contract','Service+contract'");
		postParameters.put("searchCriteria.documentTypeList","'Contract+notice','Contract+award'");
		postParameters.put("searchCriteria.cpvCodeList","09100000,09200000");
		postParameters.put("searchCriteria.publicationDateChoice","RANGE_PUBLICATION_DATE");
		postParameters.put("searchCriteria.fromPublicationDate","01-01-2010");
		postParameters.put("searchCriteria.toPublicationDate","31-12-2014");
		postParameters.put("searchCriteria.publicationDate","");
		postParameters.put("searchCriteria.documentationDate","");
		postParameters.put("searchCriteria.place","");
		postParameters.put("searchCriteria.procedureList","");
		postParameters.put("searchCriteria.regulationList","");
		postParameters.put("searchCriteria.nutsCodeList","");
		postParameters.put("searchCriteria.documentNumber","");
		postParameters.put("searchCriteria.deadline","");
		postParameters.put("searchCriteria.authorityName","");
		postParameters.put("searchCriteria.mainActivityList","");
		postParameters.put("searchCriteria.directiveList","");
		postParameters.put("searchCriteria.statisticsMode","on");
		resp = Jsoup.connect(url)
				.method(Method.POST)
				//.cookie("JSESSIONID", cookie)
				.data(postParameters)
				.followRedirects(false)
				.execute();
		cookie = resp.cookie("JSESSIONID");
		System.out.println("\nPedida: " + url);
		System.out.println("Código: " + resp.statusCode() + " " + resp.statusMessage());
		System.out.println("Cookies: " + resp.cookies());
		System.out.println("Url de la respuesta: " + resp.url());
		System.out.println("Cookie JSESSIONID: " + cookie);
		System.out.println("Redirección a:" + resp.header("location"));
		//url = resp.url().toString();
		//doc = resp.parse();
		
		url = resp.header("location");
		resp = Jsoup.connect(url)
				.method(Method.POST)
				.cookies(resp.cookies())
				.data(postParameters)
				.followRedirects(false)
				.execute();
		System.out.println("\nPedida: " + url);
		System.out.println("Código: " + resp.statusCode() + " " + resp.statusMessage());
		System.out.println("Cookies: " + resp.cookies());
		System.out.println("Url de la respuesta: " + resp.url());
		System.out.println("Redirección a:" + resp.header("location"));
		escribirFichero(resp.parse().toString());
		
		
		System.exit(0);
		
		
		postParameters.put("action", "search");
		//postParameters.put("", "");
		//postParameters.put("Rs.gp.11500808.pid","home");
		postParameters.put("lgId","en");
		//postParameters.put("Rs.gp.11500809.pid","releaseCalendar");
		postParameters.put("quickSearchCriteria","");
		//postParameters.put("s.gp.11500811.pid","secured");
		postParameters.put("searchCriteria.searchScopeId","4");
		postParameters.put("searchCriteria.ojs","");
		postParameters.put("searchCriteria.freeText","energía");
		postParameters.put("searchCriteria.countryList","AT,BE");
		postParameters.put("searchCriteria.contractList","'Supply+contract','Service+contract'");
		postParameters.put("searchCriteria.documentTypeList","'Contract+notice','Contract+award'");
		postParameters.put("searchCriteria.cpvCodeList","09100000,09200000");
		postParameters.put("searchCriteria.publicationDateChoice","RANGE_PUBLICATION_DATE");
		postParameters.put("searchCriteria.fromPublicationDate","01-01-2010");
		postParameters.put("searchCriteria.toPublicationDate","31-12-2014");
		postParameters.put("searchCriteria.publicationDate","");
		postParameters.put("searchCriteria.documentationDate","");
		postParameters.put("searchCriteria.place","");
		postParameters.put("searchCriteria.procedureList","");
		postParameters.put("searchCriteria.regulationList","");
		postParameters.put("searchCriteria.nutsCodeList","");
		postParameters.put("searchCriteria.documentNumber","");
		postParameters.put("searchCriteria.deadline","");
		postParameters.put("searchCriteria.authorityName","");
		postParameters.put("searchCriteria.mainActivityList","");
		postParameters.put("searchCriteria.directiveList","");
		postParameters.put("searchCriteria.statisticsMode","on");

		resp = Jsoup.connect(url)
				.method(Method.POST)
				//.cookie("JSESSIONID", cookie)
				.data(postParameters)
				.followRedirects(false)
				.execute();
		cookie = resp.cookie("JSESSIONID");
		System.out.println("\nPedida: " + url);
		System.out.println("Código: " + resp.statusCode() + " " + resp.statusMessage());
		System.out.println("Cookies: " + resp.cookies());
		System.out.println("Url de la respuesta: " + resp.url());
		System.out.println("Cookie JSESSIONID: " + cookie);
		url = resp.url().toString();
		doc = resp.parse();
		escribirFichero(doc.toString());
			

	}
	
	private static void escribirFichero(String texto){
		try{
		File fichero = new File("/tmp/ted.html");
		BufferedWriter writer = new BufferedWriter(new FileWriter(fichero));
        writer.write(texto);
        
        writer.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

}
