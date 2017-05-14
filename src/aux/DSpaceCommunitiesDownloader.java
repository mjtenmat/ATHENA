package aux;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DSpaceCommunitiesDownloader {
	private static int id = 0;

	public static void main(String[] args) throws Exception{
		System.out.println("Iniciando DSpaceCommunitiesDownloader.");
		Document doc = Jsoup.connect("http://dspace.mit.edu/community-list").get();
		//System.out.println(doc);
		Element root = doc.select("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser>ul").get(0);
		getSubElements(root, 0, null);
	}

	private static void getSubElements(Element rootUl, int level, Integer idPadre) {
		for(Element element : rootUl.children())
			if (element.tagName().equals("li")){
				Element a = element.select("div.artifact-description>div.artifact-title>a").get(0);
				String href = a.attr("abs:href");
				String nombre = a.select("span.Z3988").get(0).text();
//				for (int i = 0;  i < level; i++)
//					System.out.print("\t");
//				System.out.println(name + ": " + href);
				System.out.println("(" + ++id + ", \"" + nombre + "\", " + idPadre + ", '" + href + "'), ");
				for (Element subElement : element.children())
					if (subElement.tagName().equals("ul"))
						getSubElements(subElement, level+1, id);
			}
	}
}

//Falta meterlos en la base de datos.
