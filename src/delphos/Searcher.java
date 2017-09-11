package delphos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.GenericJDBCException;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import com.orsoncharts.util.json.JSONArray;
import com.orsoncharts.util.json.JSONObject;
import com.orsoncharts.util.json.parser.JSONParser;

import aux.BingAPIKey;
import delphos.iu.Delphos;
import delphos.iu.PanelTendenciasBuscador;
import delphos.iu.PanelVigilancia;

public class Searcher {
	private static final int MEJORA_NUM_ITERACIONES = Integer.parseInt(Propiedades.get("searcherNumIteracionesRRMin"));
	private static final int MEJORA_NUM_RESULTADOS = Integer.parseInt(Propiedades.get("searcherNumResultadosRRMin"));
	public static final int RESULTADOS_UMBRAL = Integer.parseInt(Propiedades.get("searcherUmbral"));
	private static final Double AG_PCT_ULTIMOS_RESULTADOS = 0.2;
	private static final Double AG_PC = 0.7; // Probabilidad de Cruce
	private static final Double AG_PM = 0.05; // Probabilidad de Mutación
	private static int numResultadosUltimaBusqueda;
	public final static String consumerKey = "87DmoPnv82V0owMOOHCwWL4huCOHAvNb";
	public final static String consumerSecretKey = "KHGT1IXsdGME9Iun";

	private static Set<Peso> consultaInicial;
	private static String idioma;
	private static ArrayList<Resultado> listaResultados;
	private static ArrayList<Resultado> listaResultadosRelevantes;
	public static int totalResultadosEPO;
	public static int totalResultadosTED;
	public static int totalResultadosDocsWeb;
	private static CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

	static HttpURLConnection conn;
	static String cookie = null;
	private static int numTotalDocs;

	private final static Logger log = Logger.getLogger(Searcher.class);

	private static Date adivinarFechaDocAcademico(String docFechaPublicacion) {
		Date fecha = null;
		SimpleDateFormat sdf, normal;
		normal = new SimpleDateFormat("yyyy-MM-dd");
		try {
			sdf = new SimpleDateFormat("©yyyy");
			fecha = sdf.parse(docFechaPublicacion);
		} catch (ParseException e1) {
			try {
				sdf = new SimpleDateFormat("'c'yyyy");
				fecha = sdf.parse(docFechaPublicacion);
			} catch (ParseException e2) {
				try {
					sdf = new SimpleDateFormat("MMMM yyyy", Locale.US);
					fecha = sdf.parse(docFechaPublicacion);
				} catch (ParseException e3) {
					try {
						sdf = new SimpleDateFormat("yyyy-MM-dd");
						fecha = sdf.parse(docFechaPublicacion);
					} catch (ParseException e4) {
						try {
							sdf = new SimpleDateFormat("yyyy-MM");
							fecha = sdf.parse(docFechaPublicacion);
						} catch (ParseException e5) {
							try {
								sdf = new SimpleDateFormat("yyyy");
								fecha = sdf.parse(docFechaPublicacion);
							} catch (ParseException e6) {
								System.out.println("Imposible adivinar fecha " + docFechaPublicacion);
							}
						}
					}
				}
			}
		}
		return fecha;
	}

	public static ArrayList<AnalisisTendencia> analizarTendencia(Tendencia tendencia, Calendar fechaDesde,
			Calendar fechaHasta) {
		ArrayList<AnalisisTendencia> resultado = new ArrayList<AnalisisTendencia>();

		// Cálculo de Periodos
		// TODO: Refactorizar con JFrameAnalisisTendencia en Java 8
		long diferencia = fechaHasta.getTimeInMillis() - fechaDesde.getTimeInMillis();
		TimeUnit tu = TimeUnit.DAYS;
		diferencia = tu.convert(diferencia, TimeUnit.MILLISECONDS);
		System.out.println("Diferencia (días): " + diferencia);
		Calendar fechaFinPeriodoAnterior = (Calendar) fechaDesde.clone();
		fechaFinPeriodoAnterior.add(Calendar.DATE, -1);
		Calendar fechaInicioPeriodoAnterior = (Calendar) fechaFinPeriodoAnterior.clone();
		fechaInicioPeriodoAnterior.add(Calendar.DATE, -((int) diferencia));
		Calendar fechaInicioPeriodoPosterior = (Calendar) fechaHasta.clone();
		fechaInicioPeriodoPosterior.add(Calendar.DATE, +1);
		Calendar fechaFinPeriodoPosterior = (Calendar) fechaInicioPeriodoPosterior.clone();
		fechaFinPeriodoPosterior.add(Calendar.DATE, ((int) diferencia));

		AnalisisTendencia atPeriodoAnterior = analizarTendenciaPeriodo(tendencia, fechaInicioPeriodoAnterior,
				fechaFinPeriodoAnterior);
		resultado.add(atPeriodoAnterior);

		AnalisisTendencia atPeriodoActual = analizarTendenciaPeriodo(tendencia, fechaDesde, fechaHasta);
		resultado.add(atPeriodoActual);

		AnalisisTendencia atPeriodoPosterior = analizarTendenciaPeriodo(tendencia, fechaInicioPeriodoPosterior,
				fechaFinPeriodoPosterior);
		resultado.add(atPeriodoPosterior);

		return resultado;
	}

	public static AnalisisTendencia analizarTendenciaPeriodo(Tendencia tendencia, Calendar fechaDesde,
			Calendar fechaHasta) {
		AnalisisTendencia resultado = new AnalisisTendencia();

		if (tendencia.getIndicadorLicitaciones()) {
			ArrayList<Licitacion> listaLicitaciones;
			try {
				listaLicitaciones = buscarLicitacionesEnLineaModoExperto(tendencia.getTextoLibre(),
						(GregorianCalendar) fechaDesde, (GregorianCalendar) fechaHasta,
						tendencia.getListaLicitacionLocalizacion(), tendencia.getLicitacionTipo(),
						tendencia.getLicitacionEntidadSolicitante(), tendencia.getListaLicitacionSector(), null);
				for (Licitacion lic : listaLicitaciones) {
					// System.out.println("TRON - lic.getListaCPV()" +
					// lic.getListaCPV());
					String patronCPV = "(\\d{8} - )"; // ocho dígitos, espacio,
														// guión, espacio
					String listaCPV = lic.getListaCPV();
					listaCPV = listaCPV.replaceAll(patronCPV, "##$1"); // $1
																		// repite
																		// el
																		// primer
																		// grupo
																		// de la
																		// Regexp
					ArrayList<String> sectores = new ArrayList<String>(Arrays.asList(listaCPV.split("##")));
					resultado.aniadirListaALista(AnalisisTendencia.TipoGeneral.LICITACION_SECTOR, sectores);

					ArrayList<String> paises = new ArrayList<String>();
					;
					if (tendencia.getListaLicitacionLocalizacion().size() == 1) {
						// Lista de localizaciones (por ciudad)
						paises = new ArrayList<String>(Arrays.asList(lic.getLocalizacion().split(",")));
					} else {
						// Elaboramos lista de países
						for (String ciudad : lic.getLocalizacion().split(","))
							paises.add(ciudad.split(" - ")[1]);
					}
					resultado.aniadirListaALista(AnalisisTendencia.TipoGeneral.LICITACION_PAIS, paises);

					ArrayList<String> tipos = new ArrayList<String>(Arrays.asList(lic.getTipoDocumento().split(",")));
					resultado.aniadirListaALista(AnalisisTendencia.TipoGeneral.LICITACION_TIPO, tipos);
					ArrayList<String> solicitantes = new ArrayList<String>(
							Arrays.asList(lic.getEntidadEmisora().split(",")));
					resultado.aniadirListaALista(AnalisisTendencia.TipoGeneral.LICITACION_SOLICITANTE, solicitantes);
					ArrayList<String> contenido = Parser.limpiar(lic.getTitulo() + " " + lic.getResumen());
					// Quitamos los repetidos de contenido y así estaremos
					// contando por documentos
					Set<String> contenidoSinRepetidos = new HashSet<String>();
					contenidoSinRepetidos.addAll(contenido);
					contenido.clear();
					contenido.addAll(contenidoSinRepetidos);
					resultado.aniadirListaALista(AnalisisTendencia.TipoGeneral.LICITACION_CONTENIDO, contenido);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (tendencia.getIndicadorPatentes()) {
			ArrayList<Patente> listaPatentes;
			try {
				listaPatentes = buscarTodasPatentesEnLinea(tendencia.getTextoLibre(), (GregorianCalendar) fechaDesde,
						(GregorianCalendar) fechaHasta, tendencia.getPatenteInventor(),
						tendencia.getPatenteSolicitante(), tendencia.getListaPatenteSector(),
						tendencia.getListaPatenteLocalizacion());
				for (Patente pat : listaPatentes) {
					// for(CPI sector : pat.getCpi())
					// resultado.aniadirALista(AnalisisTendencia.TipoGeneral.PATENTE_SECTOR,
					// sector.toString());
					ArrayList<String> sectores = new ArrayList<String>(Arrays.asList(pat.getListaCPI().split(",")));
					resultado.aniadirListaALista(AnalisisTendencia.TipoGeneral.PATENTE_SECTOR, sectores);
					ArrayList<String> paises = new ArrayList<String>(Arrays.asList(pat.getLocalizacion().split(",")));
					resultado.aniadirListaALista(AnalisisTendencia.TipoGeneral.PATENTE_PAIS, paises);
					ArrayList<String> inventores = new ArrayList<String>(Arrays.asList(pat.getInventor().split(",")));
					resultado.aniadirListaALista(AnalisisTendencia.TipoGeneral.PATENTE_INVENTOR, inventores);
					ArrayList<String> solicitantes = new ArrayList<String>(
							Arrays.asList(pat.getSolicitante().split(",")));
					resultado.aniadirListaALista(AnalisisTendencia.TipoGeneral.PATENTE_SOLICITANTE, solicitantes);
					ArrayList<String> contenido = Parser.limpiar(pat.getTitulo() + " " + pat.getResumen());
					// Quitamos los repetidos de contenido y así estaremos
					// contando por documentos
					Set<String> contenidoSinRepetidos = new HashSet<String>();
					contenidoSinRepetidos.addAll(contenido);
					contenido.clear();
					contenido.addAll(contenidoSinRepetidos);
					resultado.aniadirListaALista(AnalisisTendencia.TipoGeneral.PATENTE_CONTENIDO, contenido);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return resultado;
	}

	private static List buscar(Set<Peso> descriptores, Set<Jerarquia> sectores, Set<Jerarquia> tiposOrganizacion,
			Set<Jerarquia> localizaciones) {

		String select, from, groupBy, orderBy;
		ArrayList<String> joins = new ArrayList<String>();
		ArrayList<String> wheres = new ArrayList<String>();

		// Obtenemos todas las fuentes que tienen alguno de los descriptores de
		// la consulta
		select = "SELECT Fuente.id, ";
		select += "COUNT(*)/" + descriptores.size() + " AS cobertura";

		// Cálculo original de similitud
		// select += ", SUM(";
		// String parentesis = "";
		// for (Peso peso : descriptores) {
		// select += "IF(Descriptor.id = " + peso.getDescriptor().getId() + "," +
		// peso.getPeso() + ",";
		// parentesis += ")";
		// }
		// select += "0" + parentesis;
		// select += "* Peso.peso) as similitud ";

		from = "FROM Fuente";

		groupBy = "GROUP BY Fuente.id";
		orderBy = "ORDER BY cobertura DESC";

		// Si hay texto libre, unimos Descriptor y Peso y ponemos claúsula where
		String idsDescriptoresSeparadosComas;
		if (descriptores.size() > 0) {
			// Solo se valoran los pesos de descriptores que estén en el body.
			joins.add("INNER JOIN Peso ON Fuente.id = Peso.idFuente AND Peso.peso IS NOT NULL AND Peso.enBody = 1");
			joins.add("INNER JOIN Descriptor ON Peso.idDescriptor = Descriptor.id");
			Iterator it = descriptores.iterator();
			idsDescriptoresSeparadosComas = "'" + ((Peso) it.next()).getDescriptor().getId() + "'";
			while (it.hasNext())
				idsDescriptoresSeparadosComas += ", '" + ((Peso) it.next()).getDescriptor().getId() + "'";
			wheres.add("Descriptor.id IN (" + idsDescriptoresSeparadosComas + ")");
		}

		// Si hay Sectores, Tipos de Organización y/o Localización, unimos Hosts
		// y ponemos claúsulas where
		if ((sectores.size() > 0) || (tiposOrganizacion.size() > 0) || (localizaciones.size() > 0)) {
			joins.add("INNER JOIN Host ON Fuente.idHost = Host.id");
			if (sectores.size() > 0) {
				wheres.add("Sector.id IN (" + verIdsSeparadosPorComas(sectores) + ")");
				joins.add("INNER JOIN Host_Sector ON Host.id = Host_Sector.idSector");
				joins.add("INNER JOIN Sector ON Host_Sector.idSector = Sector.id");
			}
			if (tiposOrganizacion.size() > 0) {
				wheres.add("TipoOrganizacion.id IN (" + verIdsSeparadosPorComas(tiposOrganizacion) + ")");
				joins.add("INNER JOIN TipoOrganizacion ON Host.idTipoOrganizacion = TipoOrganizacion.id");
			}
			if (localizaciones.size() > 0) {
				wheres.add("Localizacion.id IN (" + verIdsSeparadosPorComas(localizaciones) + ")");
				joins.add("INNER JOIN Localizacion ON Host.idLocalizacion = Localizacion.id");
			}
		}

		String sql = select + " " + from + " ";
		for (String join : joins)
			sql += join + " ";
		if (wheres.size() > 0)
			sql += "WHERE " + wheres.get(0);
		for (int i = 1; i < wheres.size(); i++)
			sql += " AND " + wheres.get(i) + " ";
		sql += " " + groupBy;
		sql += " " + orderBy;

		System.out.println("SQL de búsqueda: " + sql);

		Query query = Delphos.getSession().createSQLQuery(sql);
		List listaObjetos = null;
		try {
			listaObjetos = query.list();
			System.out.println("Hay " + listaObjetos.size() + " fuentes coincidentes");
		} catch (GenericJDBCException ex) {
			System.out.println("Excepcion: " + ex.getMessage());
			if (ex.getMessage().contains("could not extract Resultset"))
				System.out.println(
						"Es necesario cambiar la configuración de MySQL. Incremente la pila de hilos (thread_stack) modificando el parámetro thread_stack en mysqld.conf. Ponga al menos 512K.");
		}

		// if (listaObjetos.size() == 0)
		// return null;
		//
		// //Formamos la lista de resultados
		// ArrayList<Resultado> listaResultados = new ArrayList<Resultado>();
		// Iterator it = listaObjetos.iterator();
		// while (it.hasNext()){
		// Object[] row = (Object[]) it.next();
		// Resultado resultado = new Resultado((Integer)row[0],
		// ((BigDecimal)row[1]).doubleValue(),(double)row[2]);
		// listaResultados.add(resultado);
		// }

		return listaObjetos;
	}

	public static ArrayList<Resultado> buscar(String textoConsulta, Set<Jerarquia> sectores,
			Set<Jerarquia> tiposOrganizacion, Set<Jerarquia> localizaciones) {
		// public static ArrayList<Resultado> buscar(String textoConsulta,
		// Set<Jerarquia> sectores, Set<Jerarquia> tiposOrganizacion,
		// Set<Jerarquia> localizaciones) {
		log.trace("En buscar");
		// listaResultados = new ArrayList<Resultado>();

		// idioma = Parser.identificarIdioma(textoConsulta);
		// log.trace("Idioma identificado: " + idioma);

		// Creamos la consulta como una Fuente
		Fuente fuenteConsulta = new Fuente();
		Parser.anadirDescriptores(fuenteConsulta, textoConsulta, 1, false);
		for (Peso descriptorConsulta : fuenteConsulta.getPesos()) {
			descriptorConsulta.setPeso(Weigher.calcularPeso(descriptorConsulta));
		}

		consultaInicial = fuenteConsulta.getPesos();

		List listaObjetosResultado = buscar(consultaInicial, sectores, tiposOrganizacion, localizaciones);

		listaResultados = expandir(listaObjetosResultado);

		// quitarImpresentables(textoConsulta);

		numResultadosUltimaBusqueda = listaResultados.size();

		// limitarResultados();

		return listaResultados;

	}

	public static ArrayList<DocumentoAcademico> buscarDocsDSpace(String textoLibreCompleto,
			GregorianCalendar fechaDesde, GregorianCalendar fechaHasta, String autor, String entidad, String sUrl,
			int indiceBusquedaDocumentosAcademicos) {

		ArrayList<DocumentoAcademico> listaDocumentosAcademicos = new ArrayList<DocumentoAcademico>();

		try {
			URL url = new URL(sUrl);
			conn = (HttpURLConnection) url.openConnection();
			String params = "num_search_field=3&results_per_page=100&";
			if (url.equals("http://dspace.mit.edu/advanced-search"))
				params += "scope=%2F&";
			params += "field1=ANY";
			params += "&page=" + ((indiceBusquedaDocumentosAcademicos / 100) + 1);
			params += "&query1=" + textoLibreCompleto.replace(" ", "+");
			params += "&conjunction2=AND&field2=";
			if (autor.isEmpty())
				params += "ANY&query2=";
			else
				params += "author&query2=" + autor.replace(" ", "+");
			params += "&conjunction3=AND&field3=ANY&query3=&rpp=10&sort_by=2&order=DESC&submit=Ir";

			// params =
			// "order=DESC&rpp=100&sort_by=2&page=1&conjunction1=AND&results_per_page=10&etal=0&field1=ANY&num_search_field=3&query1=energy";

			String html = verPagina(url, params);
			escribirFichero(html);

			Document doc = Jsoup.parse(html);
			Elements listaLi = doc.select("li.ds-artifact-item");
			for (Element li : listaLi) {
				Element aTitulo = li.select("div.artifact-title>a").first();
				String docTitulo = aTitulo.text();
				String docUrl = "http://dspace.mit.edu" + aTitulo.attr("href");
				String docAutor = li.select("span.author").text();
				String docEntidad = li.select("span.publisher").text();
				String docFechaPublicacion = li.select("span.date").text();
				String docResumen = li.select(".artifact-abstract").text();
				Document docDetalle = Jsoup.connect(docUrl + "?show=full").get();
				// String docFechaDisponibilidad = null;
				// try {
				// docFechaDisponibilidad =
				// docDetalle.select("td:contains(dc.date.available)").first()
				// .nextElementSibling().text();
				// } catch (Exception e) {
				// ;
				// }
				System.out.println("Título:" + docTitulo);
				System.out.println("URL: " + docUrl);
				System.out.println("Autor:" + docAutor);
				System.out.println("Entidad: " + docEntidad);
				System.out.println("FechaPublicacion: " + docFechaPublicacion);
				System.out.println("Resumen: " + docResumen);
				System.out.println();
				DocumentoAcademico docAcademico = new DocumentoAcademico();
				docAcademico.setTitulo(docTitulo);
				docAcademico.setHref(docUrl);
				if (docAutor.length() > 100)
					docAutor = docAutor.substring(0, 100);
				docAcademico.setAutor(docAutor);
				docAcademico.setEntidad(docEntidad);
				// try{
				// docAcademico.setFechaPublicacion(sdf.parse(docFechaPublicacion));
				// }catch(Exception e){
				// System.out.println("Fecha incorrecta: " +
				// docFechaPublicacion);
				// }
				docAcademico.setFechaPublicacion(docFechaPublicacion);
				docAcademico.setResumen(docResumen);

				// Si hay rango de fechas
				Date fechaCandidato = adivinarFechaDocAcademico(docFechaPublicacion);
				if (fechaCandidato == null)
					continue;

				Date fechaDesde2 = fechaDesde.getTime();
				if (fechaCandidato.compareTo(fechaDesde2) < 0)
					continue;
				Date fechaHasta2 = fechaHasta.getTime();
				if (fechaCandidato.compareTo(fechaHasta2) > 0)
					continue;
				// Es válido, lo añadimos a la lista de resultados

				// Si hay entidad
				if (!entidad.isEmpty()) {
					if (!docEntidad.toLowerCase().contains(entidad.toLowerCase()))
						continue;
				}

				listaDocumentosAcademicos.add(docAcademico);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return listaDocumentosAcademicos;
	}

	public static ArrayList<DocumentoWeb> buscarDocumentosWeb(String textoLibre, Set<Localizacion> listaLocalizacion,
			Set<Sector> listaSector, Set<TipoOrganizacion> listaTipoOrganizacion, int offset,
			Set<ContrastarCon> listaConstrastarCon, boolean inBody, boolean inTitle, boolean inKeywords,
			String freshness) throws Exception {
		
//TODO: Este método necesita una REFACTORIZACIÓN. Hay MUCHO código repetido.
		
		// Busca documentos web en Bing

		ArrayList<DocumentoWeb> listaResultados = new ArrayList<DocumentoWeb>();
		ArrayList<String> queries = new ArrayList<>(); // Lista de consultas en que se divide la query

		// Añadimos los términos para contrastar
		textoLibre = generarTextoLibreConOrganizaciones(textoLibre, listaConstrastarCon);
		// textoLibre = textoLibre.replace("'", "\\'");
		// textoLibre = textoLibre.replace(":", " ");

		textoLibre = URLEncoder.encode(textoLibre, "UTF-8");

		// Construimos la query
		String baseURL = "https://api.cognitive.microsoft.com/bing/v5.0/search?";
		ArrayList<String> queryParams = new ArrayList<>();
		queryParams.add("count=50");
		queryParams.add("resonseFilter=Webpages");
		if (freshness != "") {
			String freshnessText = "";
			if (freshness.equals(PanelVigilancia.ULTIMAS_24_HORAS))
				freshnessText = "Day";
			if (freshness.equals(PanelVigilancia.ULTIMA_SEMANA))
				freshnessText = "Week";
			if (freshness.equals(PanelVigilancia.ULTIMO_MES))
				freshnessText = "Month";
			queryParams.add("freshness=" + freshnessText);
		}
		if (offset != 0)
			queryParams.add("offet=" + offset);

		ArrayList<String> advancedOperators = new ArrayList<>();

		if (inBody)
			advancedOperators.add("inBody:" + textoLibre);
		if (inTitle)
			advancedOperators.add("inTitle:" + textoLibre);
		if (inKeywords)
			advancedOperators.add("inKeywords:" + textoLibre);

		String advancedOperatorsText = "q=" + textoLibre;
		for (int i = 0; i < advancedOperators.size(); i++)
			advancedOperatorsText += "+AND+%28" + advancedOperators.get(i) + "%29";

		String query = advancedOperatorsText;
		for (String queryParam : queryParams)
			query += "&" + queryParam;

		String urlText = baseURL + query;
		// System.out.println("URL: " + urlText);
		if (query.length() > 1500) {
			System.out.println(query);
			System.out.println("Consulta imposible de partir.");
			throw new Exception("Query demasiado larga (" + query.length() + ")");
		} else {
			ArrayList<String> listaSites = null;
			String sites = null;
			if (!listaLocalizacion.isEmpty() || !listaSector.isEmpty() || !listaTipoOrganizacion.isEmpty()) {
				listaSites = getListaSites(listaLocalizacion, listaSector, listaTipoOrganizacion, 0);

				int i = 0;
				while (i < listaSites.size()) {
					int espacioSites = 1500 - query.length();

					StringBuilder sb = new StringBuilder();
					sb.append("site:" + listaSites.get(i++));

					while ((sb.length() + listaSites.get(i).length()) < espacioSites) {
						sb.append("+OR+site:" + listaSites.get(i++));
						if (i >= listaSites.size())
							break;
					}
					// Construimos la consulta
					advancedOperators.remove(sites);
					sites = sb.toString();
					advancedOperators.add(sites);
					String advancedOperatorsText2 = "q=" + textoLibre;
					for (int i2 = 0; i2 < advancedOperators.size(); i2++)
						advancedOperatorsText2 += "+AND+%28" + advancedOperators.get(i2) + "%29";

					String query2 = advancedOperatorsText2;
					for (String queryParam : queryParams)
						query2 += "&" + queryParam;

					queries.add(query2);
					advancedOperators.remove(sites); //Quitamos la última vez para la segunda vuelta
				}

			} else {
				// No hay problema. No hay parámetro de sites
				queries.add(query);
			}
		}
		
		System.out.println("Primera vuelta: Hay " + queries.size() + " consultas.");
		if (queries.size() == 0)
			return listaResultados;
		
		for (String q : queries)
				System.out.println(q);

		//Construimos un ArrayList de ArrayList para los hosts
		ArrayList<ArrayList<String>> hosts = new ArrayList<>();

		for (String q : queries) {
			hosts.add(new ArrayList<String>());
			// urlText = "http://19e37.com/tmp/bing.txt";
			URL obj = new URL(baseURL + q);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Ocp-Apim-Subscription-Key", BingAPIKey.KEY); // Obligatoria

			int responseCode = con.getResponseCode();
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());

			// Parseamos el resultado
			JSONParser parser = new JSONParser();
			Object json = parser.parse(response.toString());
			JSONObject jsonObject = (JSONObject) json;
			JSONObject webPages = (JSONObject) jsonObject.get("webPages");
			JSONArray value = (JSONArray) webPages.get("value");
			Iterator<JSONObject> iterator = value.iterator();
			while (iterator.hasNext()) {
				
				try {
					JSONObject next = iterator.next();
					String titulo = next.get("name").toString();
					String bingUrl = next.get("url").toString();
					String displayUrl = next.get("displayUrl").toString();
					if (!displayUrl.startsWith("http"))
						displayUrl = "http://" + displayUrl;
					//Obtenemos y Añadimos el host
					URI uri = new URI(displayUrl.replaceAll(" ", "%20"));	//Fallo frecuente en la URIs de Bing
					hosts.get(hosts.size()-1).add(uri.getHost());
					
					String extracto = next.get("snippet").toString();
					listaResultados.add(new DocumentoWeb(titulo, new URL(bingUrl), new URL(displayUrl), extracto, 0,
							"localizacion", "sector", "tipoOrgnizacion"));
				} catch (Exception e) {
					System.out.println("ERROR: " + e.getMessage());
					System.out.println("Seguimos");
				}
			}
		}
		
		if (queries.size() == 1)
			return listaResultados;
		
		//Escogemos los mejores 30 hosts
		Set<String> mejoresHosts = new HashSet<>();
		boolean quedan;
		do {
			quedan = false;
			for (int i = 0; i < hosts.size(); i++) {
				if (!hosts.get(i).isEmpty()) {
					mejoresHosts.add(hosts.get(i).remove(0));
					quedan = true;
				}
			}
			if (!quedan)
				break;
		}while (mejoresHosts.size()< 30);
		
		System.out.print("Mejores hosts: ");
		System.out.println(mejoresHosts);
		
		//Relanzamos la/s consulta/s con los "mejores hosts"
		queries = new ArrayList<>();
		int i = 0;
		String sites = null;
		ArrayList<String>listaSites = new ArrayList<>(mejoresHosts);
		while (i < listaSites.size()) {
			int espacioSites = 1500 - query.length();

			StringBuilder sb = new StringBuilder();
			sb.append("site:" + listaSites.get(i++));

			while ((sb.length() + listaSites.get(i).length()) < espacioSites) {
				sb.append("+OR+site:" + listaSites.get(i++));
				if (i >= listaSites.size())
					break;
			}
			// Construimos la consulta
			advancedOperators.remove(sites);
			sites = sb.toString();
			advancedOperators.add(sites);
			String advancedOperatorsText2 = "q=" + textoLibre;
			for (int i2 = 0; i2 < advancedOperators.size(); i2++)
				advancedOperatorsText2 += "+AND+%28" + advancedOperators.get(i2) + "%29";

			String query2 = advancedOperatorsText2;
			for (String queryParam : queryParams)
				query2 += "&" + queryParam;

			queries.add(query2);
		}
		
		System.out.println("Segunda vuelta: Hay " + queries.size() + " consultas.");
		
		for (String q : queries)
				System.out.println(q);

		listaResultados = new ArrayList<>(); //Borramos los anteriores
		for (String q : queries) {
			hosts.add(new ArrayList<String>());
			// urlText = "http://19e37.com/tmp/bing.txt";
			URL obj = new URL(baseURL + q);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Ocp-Apim-Subscription-Key", BingAPIKey.KEY); // Obligatoria

			int responseCode = con.getResponseCode();
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());

			// Parseamos el resultado
			JSONParser parser = new JSONParser();
			Object json = parser.parse(response.toString());
			JSONObject jsonObject = (JSONObject) json;
			JSONObject webPages = (JSONObject) jsonObject.get("webPages");
			JSONArray value = (JSONArray) webPages.get("value");
			Iterator<JSONObject> iterator = value.iterator();
			while (iterator.hasNext()) {
				JSONObject next = iterator.next();
				String titulo = next.get("name").toString();
				String bingUrl = next.get("url").toString();
				String displayUrl = next.get("displayUrl").toString();
				if (!displayUrl.startsWith("http"))
					displayUrl = "http://" + displayUrl;
				String extracto = next.get("snippet").toString();
				listaResultados.add(new DocumentoWeb(titulo, new URL(bingUrl), new URL(displayUrl), extracto, 0,
						"localizacion", "sector", "tipoOrgnizacion"));
			}
		}

		return listaResultados;
	}			    

	public static ArrayList<Licitacion> buscarLicitaciones(String textoLibre, GregorianCalendar fechaDesde,
			GregorianCalendar fechaHasta, String pais, String ciudad, String tipoLicitacion, String entidadEmisora,
			Set<Sector> sectores, Set<CPV> codCPV) {

		// String sql =
		// "SELECT id, titulo, entidadEmisora, pais, ciudad, url,
		// fechaPublicacion, resumen ";
		String sql = "SELECT Licitacion.id ";
		sql += "FROM Licitacion ";
		if ((pais != null) || (ciudad != null)) {
			sql += "JOIN Licitacion_Localizacion ON ";
			sql += "idLocalizacion = Licitacion_Localizacion.id ";
			if (pais != null)
				sql += "AND Licitacion_Localizacion.pais = '" + pais + "' ";
			if (ciudad != null)
				sql += "AND Licitacion_Localizacion.ciudad = '" + ciudad + "' ";
		}
		if (tipoLicitacion != null) {
			sql += "JOIN TipoLicitacion ON ";
			sql += "idTipoLicitacion = TipoLicitacion.id ";
			sql += "AND TipoLicitacion.nombre = '" + tipoLicitacion + "' ";
		}
		if (sectores.size() > 0) {
			sql += " JOIN Licitacion_Sector ON ";
			sql += " Licitacion.id = Licitacion_Sector.idLicitacion ";
			ArrayList<Integer> listaIdSectores = verIdSectores(sectores);
			String sListaIdSectores = String.valueOf(listaIdSectores.get(0));
			for (int i = 1; i < listaIdSectores.size(); i++)
				sListaIdSectores += "," + listaIdSectores.get(i);
			sql += " AND Licitacion_Sector.idSector IN (" + sListaIdSectores + ")";
		}
		if (codCPV.size() > 0) {
			sql += " JOIN Licitacion_CPV ON ";
			sql += " Licitacion.id = Licitacion_CPV.idLicitacion ";
			String listaCodCPV = "";
			for (CPV cod : codCPV)
				listaCodCPV += cod.getCodigo() + ",";
			// Quitamos la última coma
			listaCodCPV = listaCodCPV.substring(0, listaCodCPV.length() - 1);
			sql += " AND Licitacion_CPV.idCPV IN (" + listaCodCPV + ") ";
		}

		// WHERE
		ArrayList<String> where = new ArrayList<String>();

		if (textoLibre != null) {
			String[] palabras = textoLibre.split(" ");
			String clausula = "UPPER(resumen) LIKE '%" + palabras[0].toUpperCase() + "%' ";
			clausula += " OR UPPER(titulo) LIKE '%" + palabras[0].toUpperCase() + "%' ";
			for (int i = 1; i < palabras.length; i++) {
				clausula += " AND UPPER(resumen) LIKE '%" + palabras[i].toUpperCase() + "%' ";
				clausula += " AND UPPER(titulo) LIKE '%" + palabras[i].toUpperCase() + "%' ";
			}
			where.add("(" + clausula + ")");
		}
		if (fechaDesde != null) {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			fmt.setCalendar(fechaDesde);
			where.add("fechaPublicacion >= '" + fmt.format(fechaDesde.getTime()) + "' ");
		}
		if (fechaHasta != null) {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			fmt.setCalendar(fechaHasta);
			where.add("fechaPublicacion <= '" + fmt.format(fechaHasta.getTime()) + "' ");
		}
		if (entidadEmisora != null) {
			where.add("entidadEmisora = '" + entidadEmisora + "'");
		}

		// Formamos el where
		if (where.size() > 0) {
			String sWhere;
			sWhere = " WHERE " + where.get(0);
			for (int i = 1; i < where.size(); i++)
				sWhere += " AND " + where.get(i);
			sql += sWhere;
		}

		sql += " ORDER BY fechaPublicacion DESC ";
		sql += " LIMIT 100 ";

		System.out.println(sql);
		Query query = Delphos.getSession().createSQLQuery(sql);
		List lista = query.list();

		ArrayList<Licitacion> resultado = new ArrayList<Licitacion>();

		System.out.println("Iterando");
		Iterator it = lista.iterator();
		while (it.hasNext())
			resultado.add((Licitacion) Delphos.getSession().get(Licitacion.class, (Integer) it.next()));

		return resultado;
	}

	public static ArrayList<Licitacion> buscarLicitacionesEnLinea(String textoLibre, GregorianCalendar fechaDesde,
			GregorianCalendar fechaHasta, Set<Licitacion_Localizacion> paises, String tipoLicitacion,
			String entidadEmisora, Set<Licitacion_Sector> listaSectores) throws Exception {
		ArrayList<Licitacion> listaLicitaciones = new ArrayList<Licitacion>();

		URL url;
		String html;
		String postParameters;
		CookieHandler.setDefault(cookieManager);

		url = new URL("http://ted.europa.eu/TED/");
		conn = (HttpURLConnection) url.openConnection();
		html = verPagina(url, null);
		System.out.println("\n\n  ------------ Página de Selección de Idioma CONSEGUIDA ---------------\n\n");
		verCookies();

		url = new URL("http://ted.europa.eu/TED/misc/chooseLanguage.do?lgId=en");
		conn = (HttpURLConnection) url.openConnection();
		postParameters = "action=cl";
		html = verPagina(url, postParameters);
		System.out.println("\n\n  ------------ Idioma Seleccionado ---------------\n\n");
		verCookies();

		url = new URL("http://ted.europa.eu/TED/search/search.do");
		conn = (HttpURLConnection) url.openConnection();
		html = verPagina(url, null);
		System.out.println("\n\n  ------------ Página de Búsqueda CONSEGUIDA ---------------\n\n");
		verCookies();

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
		postParameters += "&searchCriteria.searchScopeId=4";
		postParameters += "&searchCriteria.ojs=";
		postParameters += "&searchCriteria.freeText=";
		if (!textoLibre.isEmpty())
			;
		postParameters += textoLibre.replace(" ", "+");
		postParameters += "&searchCriteria.countryList=";
		if (paises != null) {
			for (Licitacion_Localizacion localizacion : paises)
				postParameters += localizacion.getListaCodigos() + ",";

			postParameters = postParameters.substring(0, postParameters.length() - 1);
		}
		postParameters += "&searchCriteria.contractList=";
		postParameters += "&searchCriteria.documentTypeList=";
		if (tipoLicitacion == null)
			postParameters += "'Contract+notice','Contract+award'";
		else {
			postParameters += "'" + tipoLicitacion.replace(" ", "+") + "'";
		}
		// postParameters +=
		// "&searchCriteria.cpvCodeList=%27Construction+and+Real+Estate%27";
		postParameters += "&searchCriteria.cpvCodeList=";
		if (listaSectores != null) {
			for (Licitacion_Sector sector : listaSectores)
				postParameters += sector.getListaCPV() + ",";

			postParameters = postParameters.substring(0, postParameters.length() - 1);
		}

		if ((fechaDesde == null) && (fechaHasta == null))
			postParameters += "&searchCriteria.publicationDateChoice=DEFINITE_PUBLICATION_DATE";
		else {
			postParameters += "&searchCriteria.publicationDateChoice=RANGE_PUBLICATION_DATE";
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			postParameters += "&searchCriteria.fromPublicationDate=";
			if (fechaDesde != null)
				postParameters += sdf.format(fechaDesde.getTime());
			postParameters += "&searchCriteria.toPublicationDate=";
			if (fechaHasta != null)
				postParameters += sdf.format(fechaHasta.getTime());
		}
		postParameters += "&searchCriteria.publicationDate=";
		postParameters += "&searchCriteria.documentationDate=";
		postParameters += "&searchCriteria.place=";
		postParameters += "&searchCriteria.procedureList=";
		postParameters += "&searchCriteria.regulationList=";
		postParameters += "&searchCriteria.nutsCodeList=";
		postParameters += "&searchCriteria.documentNumber=";
		postParameters += "&searchCriteria.deadline=";
		postParameters += "&searchCriteria.authorityName=";
		if (entidadEmisora != null)
			postParameters += entidadEmisora;
		postParameters += "&searchCriteria.mainActivityList=";
		postParameters += "&searchCriteria.directiveList=";
		postParameters += "&_searchCriteria.statisticsMode=on";

		System.out.println("Parámetros POST: " + postParameters);

		html = verPagina(url, postParameters);
		System.out.println("HTML: " + html);
		escribirFichero(html);

		int numPaginas = verNumPag(html);

		totalResultadosTED = numPaginas * 25;
		verCookies();

		listaLicitaciones.addAll(obtenerDocumentosTED(html));

		// for (int pagina = 2; pagina <= 4; pagina ++){
		// System.out.print("\nProcesando página " + pagina +": ");
		// //System.out.println("http://ted.europa.eu/TED/search/searchResult.do?page="
		// + pagina);
		// url = new URL("http://ted.europa.eu/TED/search/searchResult.do?page="
		// + pagina);
		// conn = (HttpURLConnection) url.openConnection();
		// conn.setRequestProperty("Referer",
		// "http://ted.europa.eu/TED/search/searchResult.do");
		// html = verPagina(url, null);
		// listaLicitaciones.addAll(obtenerDocumentosTED(html));
		// escribirFichero(html);
		// // System.out.println(html);
		// System.in.read();
		// }

		// listaLicitaciones.addAll(buscarLicitacionesEnLineaPorPagina(2));

		return listaLicitaciones;
	}

	public static ArrayList<Licitacion> buscarLicitacionesEnLineaModoExperto(String textoLibre,
			GregorianCalendar fechaDesde, GregorianCalendar fechaHasta, Set<Licitacion_Localizacion> paises,
			String tipoLicitacion, String entidadEmisora, Set<Licitacion_Sector> listaSectores,
			Set<ContrastarCon> listaContrastarCon) throws Exception {
		ArrayList<Licitacion> listaLicitaciones = new ArrayList<Licitacion>();

		URL url = new URL("http://ted.europa.eu/TED/search/search.do?");

		textoLibre = generarTextoLibreConOrganizaciones(textoLibre, listaContrastarCon);
		System.out.println("textoLibre en buscarLicitacionesEnLineaModoExperto = " + textoLibre);

		String html = verPagina(url, construirParametrosPostBusquedaExpertaLicitaciones(textoLibre, fechaDesde,
				fechaHasta, paises, tipoLicitacion, entidadEmisora, listaSectores));
		escribirFichero(html);

		int numPaginas = verNumPag(html);

		totalResultadosTED = numPaginas * 25;
		verCookies();

		listaLicitaciones.addAll(obtenerDocumentosTED(html));

		return listaLicitaciones;
	}

	public static ArrayList<Licitacion> buscarLicitacionesEnLineaPorPagina(int pagina) throws Exception {
		URL url;
		String html;

		// System.out.println("http://ted.europa.eu/TED/search/searchResult.do?page="
		// + pagina);
		url = new URL("http://ted.europa.eu/TED/search/searchResult.do?page=" + pagina);
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Referer", "http://ted.europa.eu/TED/search/searchResult.do");
		html = verPagina(url, null);

		return obtenerDocumentosTED(html);

	}

	public static ArrayList<Patente> buscarPatentes(String textoLibre, GregorianCalendar fechaDesde,
			GregorianCalendar fechaHasta, String inventor, String solicitante, Set<CPI> listaCPI) {
		ArrayList<Patente> resultado = new ArrayList<Patente>();

		String sql = "SELECT Patente.id ";
		sql += "FROM Patente ";
		ArrayList<String> where = new ArrayList<String>();

		if (textoLibre != null) {
			String[] palabras = textoLibre.split(" ");
			String clausula = "UPPER(resumen) LIKE '%" + palabras[0].toUpperCase() + "%' ";
			clausula += " OR UPPER(titulo) LIKE '%" + palabras[0].toUpperCase() + "%' ";
			for (int i = 1; i < palabras.length; i++) {
				clausula += " AND UPPER(resumen) LIKE '%" + palabras[i].toUpperCase() + "%' ";
				clausula += " AND UPPER(titulo) LIKE '%" + palabras[i].toUpperCase() + "%' ";
			}
			where.add("(" + clausula + ")");
		}
		if (fechaDesde != null) {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			fmt.setCalendar(fechaDesde);
			where.add("fechaPublicacion >= '" + fmt.format(fechaDesde.getTime()) + "' ");
		}
		if (fechaHasta != null) {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			fmt.setCalendar(fechaHasta);
			where.add("fechaPublicacion <= '" + fmt.format(fechaHasta.getTime()) + "' ");
		}
		if (inventor != null) {
			where.add("UPPER(inventor) LIKE '%" + inventor.toUpperCase() + "%' ");
		}
		if (solicitante != null) {
			where.add("UPPER(solicitante) LIKE '%" + inventor.toUpperCase() + "%' ");
		}

		// Formamos el where
		if (where.size() > 0) {
			String sWhere;
			sWhere = " WHERE " + where.get(0);
			for (int i = 1; i < where.size(); i++)
				sWhere += " AND " + where.get(i);
			sql += sWhere;
		}

		sql += " ORDER BY fechaPublicacion DESC ";
		sql += " LIMIT 100 ";

		System.out.println(sql);
		Query query = Delphos.getSession().createSQLQuery(sql);
		List lista = query.list();

		System.out.println("Iterando");
		Iterator it = lista.iterator();
		while (it.hasNext())
			resultado.add((Patente) Delphos.getSession().get(Patente.class, (Integer) it.next()));

		return resultado;
	}

	public static ArrayList<Patente> buscarPatentesEnLinea(String textoLibre, GregorianCalendar fechaDesde,
			GregorianCalendar fechaHasta, String inventor, String solicitante, Set<Patente_Sector> listaCPI,
			Set<Patente_Localizacion> listaLocalizacion, int indiceBusquedaEPO, Set<ContrastarCon> listaContrastarCon)
			throws Exception {

		ArrayList<Patente> listaPatentes = new ArrayList<Patente>();

		// Búsqueda de Documentos en EPO
		String scheme = "https";
		String authority = "ops.epo.org";
		String path = "/3.1/rest-services/published-data/search";

		// String query = "q=";
		ArrayList<String> condiciones = new ArrayList<String>();

		// if (listaCPI.size() == 0) {
		// // Restringimos a los CPI que nos interesan
		// String sql =
		// "SELECT distinct(nombre) FROM CPI WHERE idPatente_Sector is not
		// null";
		// Query querySQL = Delphos.getSession().createSQLQuery(sql);
		// List list = querySQL.list();
		// if (list.size() > 0) {
		// queryCPI = "(cpc=/low " + list.get(0).toString() + " ";
		// for (int i = 1; i < list.size(); i++)
		// queryCPI += "or cpc=/low " + list.get(i).toString() + " ";
		// queryCPI += ")";
		// queryCPI = ""; //Demasiado larga
		// }
		// }
		// else{
		ArrayList<String> alCPI = new ArrayList<String>();
		String queryCPI = null;
		if (listaCPI.size() != 0) {
			Iterator<Patente_Sector> it = listaCPI.iterator();
			while (it.hasNext()) {
				Patente_Sector ps = it.next();
				if (ps.descripcion == null) // Si es null, es un sector sin
											// código de CPI
					// Cargamos todos sus subcódigos
					alCPI.addAll(verCodigosPatente_Sector(ps.id));
				else
					alCPI.add(ps.getNombre());
			}
			queryCPI = "(ipc=/low ";
			for (String cpi : alCPI)
				queryCPI += cpi + " or ipc=/low ";
			queryCPI = queryCPI.substring(0, queryCPI.length() - " or ipc=/low ".length());
			queryCPI += ")";
		}
		if (alCPI.size() > 10)
			throw new Exception("Demasiados códigos CPI");

		if (queryCPI != null) {
			System.out.println("Añadiendo: " + queryCPI);
			condiciones.add(queryCPI);
		}

		ArrayList<String> alLocalizacion = new ArrayList<String>();
		String queryLocalizacion = null;
		if (listaLocalizacion.size() != 0) {
			Iterator<Patente_Localizacion> it2 = listaLocalizacion.iterator();
			while (it2.hasNext()) {
				alLocalizacion.add(it2.next().getNombre());
			}
			queryLocalizacion = "(pn= ";
			for (String loc : alLocalizacion)
				queryLocalizacion += loc + " or ";
			queryLocalizacion = queryLocalizacion.substring(0, queryLocalizacion.length() - " or ".length());
			queryLocalizacion += ")";
		}
		if (queryLocalizacion != null) {
			System.out.println("Añadiendo: " + queryLocalizacion);
			condiciones.add(queryLocalizacion);
		}

		if (listaContrastarCon == null) {
			if (!textoLibre.isEmpty()) {
				if (textoLibre.contains("\""))
					condiciones.add("ta=" + textoLibre); // ta es
															// titleandabstract
				else
					condiciones.add("ta all \"" + textoLibre + "\"");
			}
		} else {
			if (!listaContrastarCon.isEmpty()) {
				String texto = "(ta all \"" + textoLibre + "\")";
				String sql = "SELECT nombre, siglas FROM Organizacion";
				String sIdTipoOrganizacion = "";
				for (ContrastarCon cc : listaContrastarCon)
					if (sIdTipoOrganizacion.length() == 0)
						sIdTipoOrganizacion += cc.getId();
					else
						sIdTipoOrganizacion += "," + cc.getId();
				sql += " WHERE idTipoOrganizacion IN (" + sIdTipoOrganizacion + ")";
				Query query = Delphos.getSession().createSQLQuery(sql);
				List<Object[]> listaOrganizacion = query.list();

				if (listaOrganizacion.size() > 0) {
					texto += " and (ta all \"";
					// Modificamos el textxo libre para que incluya las
					// Organizaciones
					Iterator<Object[]> it = listaOrganizacion.iterator();
					while (it.hasNext()) {
						Object row[] = (Object[]) it.next();
						if (row[0] != null)
							if (!((String) row[0]).isEmpty())
								texto += (String) row[0] + " ";
						if (row[1] != null)
							if (!((String) row[1]).isEmpty())
								textoLibre += (String) row[1] + " ";
					}
					texto += "\")";
				}
				condiciones.add(texto);
			} else {
				if (!textoLibre.isEmpty()) {
					if (textoLibre.contains("\""))
						condiciones.add("ta=" + textoLibre); // ta es
																// titleandabstract
					else
						condiciones.add("ta all \"" + textoLibre + "\"");
				}
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if ((fechaDesde != null) && (fechaHasta != null)) {
			String condicion = "publicationdate within \"";
			sdf.setCalendar(fechaDesde);
			condicion += sdf.format(fechaDesde.getTime());
			sdf.setCalendar(fechaHasta);
			condicion += " " + sdf.format(fechaHasta.getTime()) + "\"";
			condiciones.add(condicion);
		} else {
			if (fechaDesde != null) {
				sdf.setCalendar(fechaDesde);
				condiciones.add("publicationdate>=" + sdf.format(fechaDesde.getTime()));
			}
			if (fechaHasta != null) {
				sdf.setCalendar(fechaHasta);
				condiciones.add("publicationdate<=" + sdf.format(fechaHasta.getTime()));
			}
		}

		if (!"".equals(inventor))
			condiciones.add("inventor=\"" + inventor + "\"");
		if (!"".equals(solicitante))
			condiciones.add("applicant=\"" + solicitante + "\"");

		String query = "q=";
		for (int i = 0; i < condiciones.size(); i++) {
			if (i == 0)
				query += "(" + condiciones.get(i) + ")";
			else
				query += " and (" + condiciones.get(i) + ")";
		}
		query = query.replace("\"\"", "\"");
		System.out.println("query: " + query);

		try {
			// URI uri = new URI(scheme, authority, path, query, null);
			// String scheme = "https";
			// String authority = "ops.epo.org";
			// String path = "/3.1/rest-services/published-data/search";
			// URI uri = new
			// URI("https://ops.epo.org/3.1/rest-services/published-data/search?"+URLEncoder.encode(query,
			// "UTF-8"));
			String sUrl = "https://ops.epo.org/3.1/rest-services/published-data/search?q=ta%3D\"battery\" and \"energy storage\""; // FALLA
			sUrl = "https://ops.epo.org/3.1/rest-services/published-data/search?q=ta=battery"; // OK
			sUrl = "https://ops.epo.org/3.1/rest-services/published-data/search?q=ta=\"battery\""; // Illegal
																									// char
			sUrl = "https://ops.epo.org/3.1/rest-services/published-data/search?q=ta=%22battery%22"; // OK
			sUrl = "https://ops.epo.org/3.1/rest-services/published-data/search?q=ta=%22battery%22 and %22energy%22"; // Illegal
			sUrl = "https://ops.epo.org/3.1/rest-services/published-data/search?q=ta=%22battery%22+and+%22energy+storage%22"; // OK
			query = query.replace("\"", "%22");
			query = query.replace(" ", "+");
			query = query.replace("(", "%28");
			query = query.replace(")", "%29");
			sUrl = "https://ops.epo.org/3.1/rest-services/published-data/search?" + query;
			URI uri = new URI(sUrl);
			System.out.println("URI: " + uri.toString());
			HttpsURLConnection con = (HttpsURLConnection) (uri.toURL().openConnection());

			con.setRequestMethod("GET");
			String token = obtenerTokenEPO();
			con.setRequestProperty("Authorization", "Bearer " + token);
			String Range = "" + indiceBusquedaEPO + "-" + (indiceBusquedaEPO + 99);
			System.out.println(Range);
			con.setRequestProperty("X-OPS-Range", Range);

			// Send get request
			con.setDoOutput(true);

			int responseCode = con.getResponseCode();
			// System.out.println("\nSending 'GET' request to URI : " + uri);
			System.out.println("Response Code : " + responseCode);

			/*
			 * BufferedReader in = new BufferedReader( new
			 * InputStreamReader(uri.toURL().openStream())); String inputLine; while
			 * ((inputLine = in.readLine()) != null) System.out.println(inputLine);
			 * in.close();
			 */

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(con.getInputStream());

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			Searcher.totalResultadosEPO = Integer.parseInt(doc.getElementsByTagName("ops:biblio-search").item(0)
					.getAttributes().getNamedItem("total-result-count").getTextContent());
			log.info("Total Resultados: " + Searcher.totalResultadosEPO);
			org.w3c.dom.NodeList docList = doc.getElementsByTagName("ops:publication-reference");
			log.info("Encontradas " + docList.getLength() + " patentes.");

			for (int i = 0; i < docList.getLength(); i++) {
				if (docList.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
					org.w3c.dom.Element elemento = (org.w3c.dom.Element) docList.item(i);
					Patente patente = new Patente();
					listaPatentes.add(patente);
					patente.documentIdType = elemento.getElementsByTagName("document-id").item(0).getAttributes()
							.getNamedItem("document-id-type").getTextContent();
					patente.docNumber = elemento.getElementsByTagName("doc-number").item(0).getTextContent();
					patente.kind = elemento.getElementsByTagName("kind").item(0).getTextContent();
					patente.setLocalizacion(elemento.getElementsByTagName("country").item(0).getTextContent());
					log.trace(patente);
					String sURL = "http://worldwide.espacenet.com/publicationDetails/biblio?DB=worldwide.espacenet.com";
					sURL += "&II=0&ND=3&adjacent=true&locale=en_EP&FT=D&CC=" + patente.getLocalizacion() + "&NR="
							+ patente.docNumber + patente.kind + "&KC=" + patente.kind;
					try {
						patente.setUrl(new URL(sURL));
					} catch (MalformedURLException e2) {
						log.error("Error en la URL de la Patente: " + sURL);
						e2.printStackTrace();
					}
					completarPatente(patente, token);
					System.out.println(patente);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// System.exit(-1);
		}
		Collections.sort(listaPatentes);// Ordenamos por fecha de publicación
										// descendente
		return listaPatentes;
	}

	public static DefaultCategoryDataset buscarTendencia(Tendencia tendencia, int periodoIndicado,
			GregorianCalendar fechaDesde, GregorianCalendar fechaHasta) throws Exception {
		DefaultCategoryDataset resultado = new DefaultCategoryDataset();

		int tipoPeriodo = 0, periodo = 0, tipoIntervalo = 0, intervalo = 0;

		switch (periodoIndicado) {
		case PanelTendenciasBuscador.PERIODO_ULTIMO_MES:
			// Si es el último mes: Dividimos el intervalo en dos días
			tipoPeriodo = Calendar.MONTH;
			periodo = 1;
			tipoIntervalo = Calendar.DATE;
			intervalo = 2;
			break;
		case PanelTendenciasBuscador.PERIODO_ULTIMO_ANIO:
			// El último año: valores para cada mes.
			tipoPeriodo = Calendar.YEAR;
			periodo = 1;
			tipoIntervalo = Calendar.MONTH;
			intervalo = 1;
			break;
		case PanelTendenciasBuscador.PERIODO_ULTIMO_SEMESTRE:
			// El último semestre: para cada dos semanas.
			tipoPeriodo = Calendar.MONTH;
			periodo = 6;
			tipoIntervalo = Calendar.WEEK_OF_YEAR;
			intervalo = 2;
			break;
		case PanelTendenciasBuscador.PERIODO_ULTIMO_TRIMESTRE:
			// El último trimestre: para cada semana.
			tipoPeriodo = Calendar.MONTH;
			periodo = 3;
			tipoIntervalo = Calendar.WEEK_OF_YEAR;
			intervalo = 1;
			break;
		case PanelTendenciasBuscador.PERIODO_ULTIMOS_3_ANIOS:
			// - Los últimos 3 años: se muestran los valores para cada 3 meses
			// (12 trimestres)
			tipoPeriodo = Calendar.YEAR;
			periodo = 3;
			tipoIntervalo = Calendar.MONTH;
			intervalo = 3;
			break;
		}

		// Bucle de generación de fechas
		Calendar fechaFinPeriodo = Calendar.getInstance();
		Calendar fechaInicioPeriodo = Calendar.getInstance();
		fechaInicioPeriodo.add(tipoPeriodo, -periodo);

		if (periodoIndicado == PanelTendenciasBuscador.PERIODO_USUARIO) {
			fechaFinPeriodo = fechaHasta;
			fechaInicioPeriodo = fechaDesde;
			long dif = fechaFinPeriodo.getTimeInMillis() - fechaInicioPeriodo.getTimeInMillis();
			long milisegPorDia = 24 * 60 * 60 * 1000;
			if (dif > 6 * 365 * milisegPorDia) {
				// Más de 6 años, valores por año
				tipoIntervalo = Calendar.YEAR;
				intervalo = 1;
			} else if (dif > 3 * 365 * milisegPorDia) {
				// De 3 a 6 años, valores cada 6 meses.
				tipoIntervalo = Calendar.MONTH;
				intervalo = 6;
			} else if (dif > 1 * 365 * milisegPorDia) {
				// De 1 a 3 años, valores cada 3 meses.
				tipoIntervalo = Calendar.MONTH;
				intervalo = 3;
			} else if (dif > 6 * 30 * milisegPorDia) {
				// - De 6 meses a 1 año: cada mes.
				tipoIntervalo = Calendar.MONTH;
				intervalo = 1;
			} else if (dif > 3 * 30 * milisegPorDia) {
				// De 3 a 6 meses: cada 2 semanas.
				tipoIntervalo = Calendar.WEEK_OF_YEAR;
				intervalo = 2;
			} else if (dif > 1 * 30 * milisegPorDia) {
				// De 1 a 3 meses: cada semana.
				tipoIntervalo = Calendar.WEEK_OF_YEAR;
				intervalo = 1;
			} else {
				// Menos de un mes: cada dos días.
				tipoIntervalo = Calendar.DATE;
				intervalo = 2;
			}
		}

		// Añadimos un primer intervalo
		fechaInicioPeriodo.add(tipoIntervalo, -intervalo);

		// Calculamos la tendenciaClon para los totales
		Tendencia tendenciaClon = tendencia.crearClonParaTotales();

		SimpleDateFormat sdf;
		if (tipoIntervalo == Calendar.YEAR)
			sdf = new SimpleDateFormat("yyyy");
		else if (tipoIntervalo == Calendar.MONTH)
			sdf = new SimpleDateFormat("MM/yyyy");
		else
			sdf = new SimpleDateFormat("dd/MM/yyyy");

		while (fechaInicioPeriodo.before(fechaFinPeriodo)) {
			// System.out.print("FechaInicioPeriodo: " +
			// sdf.format(fechaInicioPeriodo.getTime()));
			// System.out.println("FechaFinPeriodo: " +
			// sdf.format(fechaFinPeriodo.getTime()));
			// Movemos la fechaInicioPeriodo hasta que llegue a fechaFinPeriodo
			Calendar fechaFinIntervalo = (Calendar) fechaInicioPeriodo.clone();
			fechaFinIntervalo.add(tipoIntervalo, intervalo);

			System.out.println("Calculando: " + sdf.format(fechaInicioPeriodo.getTime()) + " - "
					+ sdf.format(fechaFinIntervalo.getTime()));

			if (tendencia.getIndicadorLicitaciones()) {
				int numEncontrados = verNumLicitacionesModoExperto(fechaInicioPeriodo, fechaFinIntervalo, tendencia);
				int numTotal = verNumLicitacionesModoExperto(fechaInicioPeriodo, fechaFinIntervalo, tendenciaClon);
				System.out.println("Licitaciones: Fecha " + sdf.format(fechaInicioPeriodo.getTime()) + " = "
						+ numEncontrados + "/" + numTotal + " = " + (double) numEncontrados / numTotal);
				if ((numTotal == 0) || (numEncontrados == 0))
					resultado.addValue(0, "Licitaciones", sdf.format(fechaFinIntervalo.getTime()));
				else {
					double dato = (double) numEncontrados / numTotal * 1000;
					resultado.addValue(dato / 10, "Licitaciones", sdf.format(fechaFinIntervalo.getTime()));
				}
			}
			if (tendencia.getIndicadorPatentes()) {
				int numEncontrados = verNumPatentes(fechaInicioPeriodo, fechaFinIntervalo, tendencia);
				int numTotal = verNumPatentes(fechaInicioPeriodo, fechaFinIntervalo, tendenciaClon);
				System.out.println("Patentes: Fecha " + sdf.format(fechaInicioPeriodo.getTime()) + " = "
						+ numEncontrados + "/" + numTotal + " = " + (double) numEncontrados / numTotal);
				if ((numTotal == 0) || (numEncontrados == 0))
					resultado.addValue(0, "Patentes", sdf.format(fechaFinIntervalo.getTime()));
				else {
					double dato = (double) numEncontrados / numTotal * 1000;
					resultado.addValue(dato / 10, "Patentes", sdf.format(fechaFinIntervalo.getTime()));
				}
			}
			if (tendencia.getIndicadorDocs()) {
				int numEncontrados = verNumDocs(fechaInicioPeriodo, fechaFinIntervalo, tendencia);
				// int numTotal = verNumDocs(fechaInicioPeriodo,
				// fechaFinIntervalo, tendenciaClon);
				if (numTotalDocs == 0 && numEncontrados > 0)
					numTotalDocs = verNumTotalDocsDSpace();
				System.out.println("Documentos: Fecha " + sdf.format(fechaInicioPeriodo.getTime()) + " = "
						+ numEncontrados + "/" + numTotalDocs + " = " + (double) numEncontrados / numTotalDocs);
				if ((numTotalDocs == 0) || (numEncontrados == 0))
					resultado.addValue(0, "Documentos", sdf.format(fechaFinIntervalo.getTime()));
				else {
					double dato = (double) numEncontrados / numTotalDocs * 1000;
					resultado.addValue(dato / 10, "Documentos", sdf.format(fechaFinIntervalo.getTime()));
				}
			}

			fechaInicioPeriodo.add(tipoIntervalo, intervalo);

		}

		return resultado;
	}

	public static ArrayList<Patente> buscarTodasPatentesEnLinea(String textoLibre, GregorianCalendar fechaDesde,
			GregorianCalendar fechaHasta, String inventor, String solicitante, Set<Patente_Sector> listaCPI,
			Set<Patente_Localizacion> listaLocalizacion) {
		ArrayList<Patente> resultado = new ArrayList<Patente>();
		int aux, indiceBusquedaEPO = 1;

		do {
			aux = resultado.size();
			try {
				resultado.addAll(buscarPatentesEnLinea(textoLibre, fechaDesde, fechaHasta, inventor, solicitante,
						listaCPI, listaLocalizacion, indiceBusquedaEPO, null));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			indiceBusquedaEPO += 100;

		} while (aux < resultado.size());
		return resultado;
	}

	/**
	 * Calcula la similitud entre una consulta y un documento de la base de datos.
	 */
	private static double calcularSimilitud(Set<Peso> consulta, int idFuente) {
		double resultado = 0;

		// Creamos un mapa de idDescriptor->Peso para la consulta
		Map<Integer, Peso> t = new HashMap<>();
		// Creamos una lista de idDescriptores de la consulta
		String listaDescriptoresConsulta = " ";
		for (Peso p : consulta) {
			listaDescriptoresConsulta += p.getDescriptor().getId() + ",";
			t.put(p.getDescriptor().getId(), p);
		}
		listaDescriptoresConsulta = listaDescriptoresConsulta.substring(0, listaDescriptoresConsulta.length() - 1);

		// Para cada documento de la Base de Datos
		String sql = "SELECT idDescriptor, idFuente, peso FROM Peso ";
		sql += "WHERE idDescriptor IN (" + listaDescriptoresConsulta + ") " + " AND idFuente = " + idFuente
				+ " ORDER BY idFuente";
		Query query = Delphos.getSession().createSQLQuery(sql);
		List listaPesos = query.list();
		Iterator it = listaPesos.iterator();
		Integer idUltimaFuente = -1;
		double numerador = 0.0;
		double denominador = 0.0;
		while (it.hasNext()) {
			Object[] row = (Object[]) it.next();
			Integer idDescriptor = (Integer) row[0];
			idFuente = (Integer) row[1];
			idUltimaFuente = idFuente;
			double qi = (Double) row[2]; // peso del término en la consulta
			double ti = t.get(idDescriptor).getPeso();
			numerador += qi * ti;
			denominador += Math.pow(qi * ti, 2);
		}
		resultado = numerador / Math.sqrt(denominador);

		return resultado;
	}

	/**
	 * Calcula la similitud entre una consulta y los documentos de la base de datos.
	 */
	private static ArrayList<SimilitudDocumento> calcularSimilitudes(Set<Peso> consulta) {
		ArrayList<SimilitudDocumento> resultado = new ArrayList<SimilitudDocumento>();

		// Creamos un mapa de idDescriptor->Peso para la consulta
		Map<Integer, Peso> t = new HashMap<>();
		// Creamos una lista de idDescriptores de la consulta
		String listaDescriptoresConsulta = " ";
		for (Peso p : consulta) {
			listaDescriptoresConsulta += p.getDescriptor().getId() + ",";
			t.put(p.getDescriptor().getId(), p);
		}
		listaDescriptoresConsulta = listaDescriptoresConsulta.substring(0, listaDescriptoresConsulta.length() - 1);

		// Para cada documento de la Base de Datos
		String sql = "SELECT idDescriptor, idFuente, peso FROM Peso ";
		sql += "WHERE idDescriptor IN (" + listaDescriptoresConsulta + ") ORDER BY idFuente";
		Query query = Delphos.getSession().createSQLQuery(sql);
		List listaPesos = query.list();
		Iterator it = listaPesos.iterator();
		Integer idUltimaFuente = -1;
		Integer idFuente = 0;
		double numerador = 0.0;
		double denominador = 0.0;
		while (it.hasNext()) {
			// Si es un idFuente nuevo
			if (idFuente != idUltimaFuente) {
				if (idUltimaFuente != -1)
					resultado.add(new SimilitudDocumento(numerador / Math.sqrt(denominador), idFuente));
				numerador = 0.0;
				denominador = 0.0;
			}
			Object[] row = (Object[]) it.next();
			Integer idDescriptor = (Integer) row[0];
			idFuente = (Integer) row[1];
			idUltimaFuente = idFuente;
			double qi = (Double) row[2]; // peso del término en la consulta
			double ti = t.get(idDescriptor).getPeso();
			numerador += qi * ti;
			denominador += Math.pow(qi * ti, 2);
		}
		// Añadimos el último, si lo hay
		if (idUltimaFuente != -1)
			resultado.add(new SimilitudDocumento(numerador / Math.sqrt(denominador), idFuente));

		Collections.sort(resultado); // Hay SimilitudDocumento.compareTo, ordena por similitud.
		return resultado;
	}

	private static String codificar(String textoLibre, String tipo) {
		String preExpresion = null, postExpresion = null;
		switch (tipo) {
		case "TED":
			preExpresion = "FT=[";
			postExpresion = "]";
			break;
		case "SQL":
			preExpresion = "texto LIKE '%";
			postExpresion = "%'";
			break;
		default:
			return textoLibre;
		}

		boolean comillasAbiertas = false;
		boolean caracterEscape = false;
		boolean operador = true; // Indica si hemos puesto un operador en la
									// última expresión

		String query = "";
		String expresion = ""; // literal u operador
		String prefijo = "";
		String posfijo = "";

		for (int i = 0; i < textoLibre.length(); i++) {
			boolean procesarExpresion = false;
			if (caracterEscape) {
				expresion += textoLibre.charAt(i);
				caracterEscape = false;
				continue;
			}
			switch (textoLibre.charAt(i)) {
			case '\\':
				caracterEscape = true;
				expresion += textoLibre.charAt(i);
				continue;
			case '"':
				if (tipo == "TED")
					expresion += textoLibre.charAt(i);
				if (comillasAbiertas)
					procesarExpresion = true;
				comillasAbiertas = !comillasAbiertas;
				break;
			case '(':
				if (!comillasAbiertas) {
					procesarExpresion = true;
					prefijo = "(";
					break;
				}
			case ')':
				if (!comillasAbiertas) {
					procesarExpresion = true;
					posfijo = ")";
					break;
				}
			case ' ':
				if (!comillasAbiertas) {
					procesarExpresion = true;
					break;
				}
			default:
				if (comillasAbiertas)
					expresion += textoLibre.charAt(i);
				else
					expresion += textoLibre.charAt(i);
			}
			if (procesarExpresion) {
				if (expresion.length() > 0) {
					if ("not".equals(expresion) || "and".equals(expresion) || "or".equals(expresion)) {
						// Ponemos operador
						if (tipo.equals("SQL"))
							if (expresion.equals("not"))
								expresion = "and not";
						query += " " + expresion + " ";
						operador = true;
					} else {
						// Literal
						if (!operador) // Si no hemos puesto un operador
							query += " AND ";
						if (tipo == "SQL") {
							expresion = expresion.replace("'", "\\'");
						}
						query += prefijo + preExpresion + expresion + postExpresion + posfijo;
						prefijo = "";
						posfijo = "";
						operador = false;
					}
				} else {
					query += prefijo + posfijo; // dobles paréntesis
					prefijo = "";
					posfijo = "";
				}
				expresion = "";
			}
		} // Fin del for
			// Procesamos la última expresión
		if (expresion.length() > 0) {
			if ("not".equals(expresion) || "and".equals(expresion) || "or".equals(expresion)) {
				// Ponemos operador
				query += " " + expresion + " ";
			} else {
				// Literal
				if (!operador) // Si no hemos puesto un operador
					query += " AND ";
				if (tipo == "SQL") {
					expresion = expresion.replace("'", "\\'");
				}
				query += preExpresion + expresion + postExpresion;
			}
		}

		return query;
	}

	private static void completarPatente(Patente patente, String token) {
		// Recuperación de datos bibliográficos
		String scheme = "https";
		String authority = "ops.epo.org";
		String path = "/3.1/rest-services/published-data/publication/";

		// https://ops.epo.org/3.1/rest-services/published-data/publication/docdb/US.8995573/biblio
		path += patente.documentIdType + "/" + patente.getLocalizacion() + "." + patente.docNumber + "." + patente.kind
				+ "/biblio";
		try {
			URI uri = new URI(scheme, authority, path, null);
			System.out.println("URI patente: " + uri.toString());

			HttpsURLConnection con = (HttpsURLConnection) (uri.toURL().openConnection());

			// add request header
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", "Bearer " + token);

			// Send get request
			con.setDoOutput(true);

			int responseCode = con.getResponseCode();
			log.trace("\nSending 'GET' request to URI : " + uri);
			log.trace("Response Code : " + responseCode);
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

			// Aquí hay que leer la listaCPI de la Patente
			String slistaCPI = "";
			org.w3c.dom.NodeList listaCPI = doc.getElementsByTagName("classification-ipcr");
			for (int i = 0; i < listaCPI.getLength(); i++) {
				if (slistaCPI != "")
					slistaCPI += ", ";
				String codigoCPI = ((org.w3c.dom.Element) listaCPI.item(i)).getElementsByTagName("text").item(0)
						.getTextContent();
				Query query = Delphos.getSession()
						.createSQLQuery("SELECT descripcion FROM CPI_Oficial WHERE codigo LIKE '"
								+ codigoCPI.replace(" ", "").substring(0, codigoCPI.replace(" ", "").length() - 2)
								+ "%' ORDER BY codigo LIMIT 1");
				String descripcion = (String) query.uniqueResult();
				slistaCPI += codigoCPI + " - " + descripcion;
				// slistaCPI += ((org.w3c.dom.Element)
				// listaCPI.item(i)).getElementsByTagName("section").item(0).getTextContent();
				// slistaCPI += ((org.w3c.dom.Element)
				// listaCPI.item(i)).getElementsByTagName("class").item(0).getTextContent();
				// slistaCPI += ((org.w3c.dom.Element)
				// listaCPI.item(i)).getElementsByTagName("subclass").item(0).getTextContent();
				// slistaCPI += ((org.w3c.dom.Element)
				// listaCPI.item(i)).getElementsByTagName("main-group").item(0).getTextContent();
				// slistaCPI += ((org.w3c.dom.Element)
				// listaCPI.item(i)).getElementsByTagName("subgroup").item(0).getTextContent();
				// slistaCPI += ((org.w3c.dom.Element)
				// listaCPI.item(i)).getElementsByTagName("classification-value").item(0).getTextContent();
			}
			slistaCPI = slistaCPI.replaceAll("\\s+", " ");
			patente.setListaCPI(slistaCPI);
			System.out.println("ListaCPI: " + slistaCPI);

			String inventor = "";
			org.w3c.dom.NodeList listaInventores = doc.getElementsByTagName("inventor");
			for (int i = 0; i < listaInventores.getLength(); i++) {
				if (inventor != "")
					inventor += ", ";
				inventor += ((org.w3c.dom.Element) listaInventores.item(i)).getElementsByTagName("name").item(0)
						.getTextContent();
			}
			patente.setInventor(inventor);

			String solicitante = "";
			org.w3c.dom.NodeList listaSolicitantes = doc.getElementsByTagName("applicant");
			for (int i = 0; i < listaSolicitantes.getLength(); i++) {
				if (solicitante != "")
					solicitante += ", ";
				solicitante += ((org.w3c.dom.Element) listaSolicitantes.item(i)).getElementsByTagName("name").item(0)
						.getTextContent();
				inventor += ", ";
			}
			patente.setSolicitante(solicitante);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String sFecha = ((org.w3c.dom.Element) doc.getElementsByTagName("publication-reference").item(0))
					.getElementsByTagName("date").item(0).getTextContent();
			try {
				patente.setFechaPublicacion(sdf.parse(sFecha));
			} catch (ParseException e) {
				log.error("Error en Fecha de Publicación: " + sFecha);
				e.printStackTrace();
			}

			String resumen = "";
			org.w3c.dom.NodeList listaResumen = doc.getElementsByTagName("abstract");
			for (int i = 0; i < listaResumen.getLength(); i++)
				for (int j = 0; j < ((org.w3c.dom.Element) listaResumen.item(i)).getElementsByTagName("p")
						.getLength(); j++)
					resumen += ((org.w3c.dom.Element) listaResumen.item(i)).getElementsByTagName("p").item(j)
							.getTextContent();
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

	private static String construirParametrosPostBusquedaExpertaLicitaciones(String textoLibre,
			GregorianCalendar fechaDesde, GregorianCalendar fechaHasta, Set<Licitacion_Localizacion> paises,
			String tipoLicitacion, String entidadEmisora, Set<Licitacion_Sector> listaSectores) throws Exception {
		String postParameters;

		URL url;
		String html;
		CookieHandler.setDefault(cookieManager);

		url = new URL("http://ted.europa.eu/TED/");
		conn = (HttpURLConnection) url.openConnection();
		html = verPagina(url, null);
		System.out.println("\n\n  ------------ Página de Selección de Idioma CONSEGUIDA ---------------\n\n");

		url = new URL("http://ted.europa.eu/TED/misc/chooseLanguage.do?lgId=en");
		conn = (HttpURLConnection) url.openConnection();
		postParameters = "action=cl";
		html = verPagina(url, postParameters);
		System.out.println("\n\n  ------------ Idioma Seleccionado ---------------\n\n");

		// 2. Búsqueda
		url = new URL("http://ted.europa.eu/TED/search/expertSearch.do?");
		conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(100000);
		conn.setReadTimeout(100000);
		conn.setRequestProperty("Referer", "http://ted.europa.eu/TED/search/expertSearch.do?");
		postParameters = "action=search";
		postParameters += "&Rs.gp.9719123.pid=home";
		postParameters += "&lgId=en";
		postParameters += "&Rs.gp.9719124.pid=releaseCalendar";
		postParameters += "&quickSearchCriteria=";
		postParameters += "&Rs.gp.9719127.pid=secured";
		postParameters += "&expertSearchCriteria.searchScope=ARCHIVE";

		ArrayList<String> listaCondicionesBusqueda = new ArrayList<String>();

		// Periodo de Publicación
		if ((fechaDesde != null) && (fechaHasta != null)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			listaCondicionesBusqueda
					.add("PD=[" + sdf.format(fechaDesde.getTime()) + " <> " + sdf.format(fechaHasta.getTime()) + "]");
		}
		// Lista de Países
		if (paises.size() > 0) {
			String tmp = "";
			for (Licitacion_Localizacion localizacion : paises)
				tmp += localizacion.getListaCodigos() + ",";

			tmp = tmp.substring(0, tmp.length() - 1);
			listaCondicionesBusqueda.add("CY=[" + tmp.replace(",", " or ") + "]");
		}
		// Lista de Sectores
		if (listaSectores.size() > 0) {
			String tmp = "";
			for (Licitacion_Sector sector : listaSectores)
				tmp += sector.getListaCPV() + ",";
			tmp = tmp.substring(0, tmp.length() - 1);
			listaCondicionesBusqueda.add("PC=[" + tmp.replace(",", " or ") + "]");
		}
		// Tipo de Licitación
		if (tipoLicitacion == null)
			listaCondicionesBusqueda.add("TD=[3 or 7]");
		else if (tipoLicitacion.equals("Contract award"))
			listaCondicionesBusqueda.add("TD=[7]");
		else if (tipoLicitacion.equals("Contract notice"))
			listaCondicionesBusqueda.add("TD=[3]");

		// Entidad Emisora
		if (!"".equals(entidadEmisora))
			listaCondicionesBusqueda.add("AU=[" + entidadEmisora + "]");

		if (!textoLibre.isEmpty()) {
			// textoLibre = URLEncoder.encode(textoLibre, "UTF-8");
			// textoLibre = textoLibre.replace("&",
			// Character.toString((char)0x0026));
			// textoLibre = textoLibre.replace("&", "%26");
			textoLibre = textoLibre.replace("&", " ");
			listaCondicionesBusqueda.add("(" + codificar(textoLibre, "TED") + ")");
		}

		String expertQuery = "";
		if (listaCondicionesBusqueda.size() > 0)

		{
			expertQuery += listaCondicionesBusqueda.get(0);
			for (int i = 1; i < listaCondicionesBusqueda.size(); i++)
				expertQuery += " AND " + listaCondicionesBusqueda.get(i);
		}

		postParameters += "&expertSearchCriteria.query=" + expertQuery;
		postParameters += "&_expertSearchCriteria.statisticsMode=on";

		System.out.println("Parámetros POST: " + postParameters);

		return postParameters;
	}

	private static void cruzarCromosomas(Cromosoma cromosoma1, Cromosoma cromosoma2) {
		// Recombinación en un punto
		// http://es.wikipedia.org/wiki/Recombinaci%C3%B3n_%28computaci%C3%B3n_evolutiva%29
		int puntoCruce = (int) (Math.random() * cromosoma1.getPesos().size());
		// Creamos dos subcromosomas nuevos
		ArrayList<Double> subCromosoma1 = new ArrayList<Double>(); // De pesos
		ArrayList<Double> subCromosoma2 = new ArrayList<Double>(); // De pesos
		for (int i = puntoCruce; i < cromosoma1.getPesos().size(); i++) {
			subCromosoma1.add(cromosoma1.getPesos().get(i));
			subCromosoma2.add(cromosoma2.getPesos().get(i));
			// Y los borramos
			cromosoma1.getPesos().remove(i);
			cromosoma2.getPesos().remove(i);
		}
		for (int i = 0; i < subCromosoma1.size(); i++) {
			cromosoma1.getPesos().add(subCromosoma2.get(i));
			cromosoma2.getPesos().add(subCromosoma1.get(i));
		}
	}

	private static void eliminarRelevantes(List listaObjetosResultado) {
		// Eliminamos los resultados relevantes de la lista de resultados (que
		// ya están en la lista de relevantes)
		Iterator<Object[]> itResultados = listaObjetosResultado.iterator();
		while (itResultados.hasNext()) {
			Object[] row = (Object[]) itResultados.next();
			int idResultado = (Integer) row[0];
			Iterator<Resultado> itResultadosRelevantes = listaResultadosRelevantes.iterator();
			while (itResultadosRelevantes.hasNext())
				if (idResultado == itResultadosRelevantes.next().getFuente().getId()) {
					itResultados.remove();
					break;
				}
		}

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

	private static boolean estanTodosRelevantes(ArrayList<Resultado> resultadosEncontrados) {
		int i = 0;
		while (i < listaResultadosRelevantes.size())
			if (!resultadosEncontrados.contains(listaResultadosRelevantes.get(i++))) {
				log.trace("Falta resultado relevante.");
				return false;
			}
		return true;
	}

	private static ArrayList<Resultado> expandir(List listaObjetosResultado) {
		ArrayList<Resultado> listaResultados = new ArrayList<Resultado>();
		Iterator it = listaObjetosResultado.iterator();
		while (it.hasNext() && (listaResultados.size() < RESULTADOS_UMBRAL)) {
			Object[] row = (Object[]) it.next();
			int idFuente = (Integer) row[0];
			double cobertura = ((BigDecimal) row[1]).doubleValue();
			// double similitud = (double) row[2];
			double similitud = calcularSimilitud(consultaInicial, idFuente);
			Resultado resultado = new Resultado(idFuente, cobertura, similitud);
			listaResultados.add(resultado);
		}
		return listaResultados;
	}

	private static String generarTextoLibreConOrganizaciones(String textoLibre,
			Set<ContrastarCon> listaConstrastarCon) {
		// Generamos la lista de Organizaciones con las que constrastar:
		if (listaConstrastarCon == null)
			return textoLibre;
		if (listaConstrastarCon.size() != 0) {
			String sql = "SELECT nombre, siglas FROM Organizacion";
			String sIdTipoOrganizacion = "";
			for (ContrastarCon cc : listaConstrastarCon)
				if (sIdTipoOrganizacion.length() == 0)
					sIdTipoOrganizacion += cc.getId();
				else
					sIdTipoOrganizacion += "," + cc.getId();
			sql += " WHERE idTipoOrganizacion IN (" + sIdTipoOrganizacion + ")";
			Query query = Delphos.getSession().createSQLQuery(sql);
			List<Object[]> listaOrganizacion = query.list();

			if (listaOrganizacion.size() > 0) {
				// Modificamos el textxo libre para que incluya las
				// Organizaciones
				Iterator<Object[]> it = listaOrganizacion.iterator();
				textoLibre = "(" + textoLibre + ") AND (";
				while (it.hasNext()) {
					Object row[] = (Object[]) it.next();
					if (row[0] != null)
						if (!((String) row[0]).isEmpty())
							textoLibre += "\"" + (String) row[0] + "\"" + " OR ";
					if (row[1] != null)
						if (!((String) row[1]).isEmpty())
							textoLibre += "\"" + (String) row[1] + "\"" + " OR ";
				}
				textoLibre = textoLibre.substring(0, textoLibre.length() - 4) + ")";

			}

		}
		return textoLibre;
	}

	private static Set<Peso> getDescriptoresConsulta(ArrayList<Cromosoma> cromosomas) {
		Set<Peso> descriptoresConsulta = new HashSet<Peso>();

		log.trace("AG-5a: añadimos la consulta inicial");
		// Añadimos la Consulta Inicial al conjunto de descriptoresConsulta
		descriptoresConsulta.addAll(consultaInicial);

		sumarRelevantes(descriptoresConsulta);

		log.trace("Sumamos los cromosomas");
		// sumamos los cromosomas
		for (Cromosoma cromosoma : cromosomas) {
			for (int i = 0; i < cromosoma.getDescriptores().size(); i++) {
				Peso peso = new Peso();
				peso.setDescriptor(cromosoma.getDescriptores().get(i).getTexto());
				peso.setPeso(cromosoma.getPesos().get(i));
				descriptoresConsulta.add(peso);
			}
		}

		return descriptoresConsulta;
	}

	private static ArrayList<String> getListaSites(Set<Localizacion> listaLocalizacion, Set<Sector> listaSector,
			Set<TipoOrganizacion> listaTipoOrganizacion, int offset) {

		final int LIMIT = 50;

		ArrayList<String> sites = new ArrayList<>();

		String select, from, orderBy, limit;
		ArrayList<String> join = new ArrayList<String>();
		ArrayList<String> where = new ArrayList<String>();
		ArrayList<String> order = new ArrayList<String>();

		select = "Host.id, Host.url AS url ";
		from = "Host ";
		// join.add("LEFT JOIN Host_Sector ON Host.id = Host_Sector.idHost");
		// join.add("LEFT JOIN Sector ON Host_Sector.idSector = Sector.id");
		join.add("LEFT JOIN Sector ON Host.idSector = Sector.id");
		join.add("LEFT JOIN TipoOrganizacion ON Host.idTipoOrganizacion = TipoOrganizacion.id");
		join.add("LEFT JOIN Localizacion ON Host.idLocalizacion = Localizacion.id");

		if (listaSector.size() > 0) {
			where.add("((Host.idSector IS NULL) OR (" + verIdJerarEnOR(listaSector, "Sector") + ")) ");
			// where.add("(Host_Sector.idSector IN (" + verIdsSeparadosPorComas(listaSector,
			// Sector.class)
			// + ") OR Host_Sector.idSector IS NULL OR (" + verIdJerarEnOR(listaSector,
			// "Sector") + "))");
			// order.add("Host_Sector.idSector DESC");
			order.add("Host.idSector DESC");
		}
		if (listaTipoOrganizacion.size() > 0) {
			where.add("(Host.idTipoOrganizacion IS NULL OR ("
					+ verIdJerarEnOR(listaTipoOrganizacion, "TipoOrganizacion") + ")) ");
			// where.add("(Host.idTipoOrganizacion IN ("
			// + verIdsSeparadosPorComas(listaTipoOrganizacion, TipoOrganizacion.class)
			// + ") OR Host.idTipoOrganizacion IS NULL OR (" +
			// verIdJerarEnOR(listaTipoOrganizacion, "TipoOrganizacion") + "))");
			order.add("Host.idTipoOrganizacion DESC");
		}
		if (listaLocalizacion.size() > 0) {
			// where.add("(Host.idLocalizacion IS NULL OR Host.idLocalizacion IN ("
			where.add("(Host.idLocalizacion IN (" + verIdsSeparadosPorComas(listaLocalizacion, Localizacion.class)
					+ ") )");

			// where.add("(Host.idLocalizacion IS NULL OR (" +
			// verIdJerarEnOR(listaLocalizacion, "Localizacion") + ")) ");

			// where.add("(Host.idLocalizacion IN (" +
			// verIdsSeparadosPorComas(listaLocalizacion, Localizacion.class)
			// + ") OR Host.idLocalizacion IS NULL OR (" + verIdJerarEnOR(listaLocalizacion,
			// "Localizacion") + "))");
			order.add("Host.idLocalizacion DESC");
		}

		limit = " LIMIT " + LIMIT + " OFFSET " + offset;

		String sql = "SELECT " + select + " FROM " + from + " ";
		for (String sJoin : join)
			sql += sJoin + " ";
		if (where.size() > 0)
			sql += "WHERE " + where.get(0);
		for (int i = 1; i < where.size(); i++)
			sql += " AND " + where.get(i) + " ";

		sql += "ORDER BY Host.id ";
		// sql += " GROUP BY Fuente.id "; // Evitamos duplicados
		// sql += " " + limit;

		System.out.println("Sites, SQL de búsqueda: " + sql);

		// System.out.println("SQL: " + sql);
		Query query = Delphos.getSession().createSQLQuery(sql);
		List lista = query.list();

		Iterator it = lista.iterator();
		ArrayList<DocumentoWeb> listaMalosDocumentos = new ArrayList<>();
		while (it.hasNext()) {
			// Object row[] = ((Object[]) it.next())[1];
			// System.out.println(row[0] + " - " + row[1]);
			sites.add(((Object[]) it.next())[1].toString());
		}

		return sites;
	}

	private static String verIdJerarEnOR(Set<? extends Jerarquia> listaJerarquia, String tabla) {
		Jerarquia[] array = (Jerarquia[]) listaJerarquia.toArray(new Jerarquia[listaJerarquia.size()]);
		StringBuilder resultado = new StringBuilder(tabla + ".idJerar = '" + array[0].getIdJerar() + "' ");
		resultado.append("OR " + tabla + ".idJerar LIKE '" + array[0].getIdJerar() + ".%' ");
		for (int i = 1; i < array.length; i++) {
			resultado.append("OR " + tabla + ".idJerar = '" + array[i].getIdJerar() + "' ");
			resultado.append("OR " + tabla + ".idJerar LIKE '" + array[i].getIdJerar() + ".%' ");
		}

		return resultado.toString();
	}

	public static int getNumResultadosUltimaBusqueda() {
		return numResultadosUltimaBusqueda;
	}

	private static void limitarResultados() {
		// Limita el número de resultados.
		while (listaResultados.size() > Searcher.RESULTADOS_UMBRAL)
			listaResultados.remove(listaResultados.size() - 1);
	}

	public static ArrayList<Resultado> mejorarAG(Set<Jerarquia> sectores, Set<Jerarquia> tiposOrganizacion,
			Set<Jerarquia> localizaciones) {

		log.trace("Iniciando AG");
		// 1. Parsear documentos (eliminar palabras vacías, reducir a la raíz...)
		// Realizado previamente por Parser.

		// 2. Obtenemos descriptores (términos para cada documento)
		// 3. Base documental como matriz.
		// 4. Ponderación de términos.
		// 5. Documento como vectores.
		// Están en la base de datos. Tabla Peso (idFuente, idDescriptor, peso)

		// 6. Consulta trasformada en vector.
		// Ya está en consultaInicial:Set<Peso>, que ha sido parseada y pesada en
		// Searcher.

		// 7. Calculo de similitud entre consulta (Qi) y documentos (Di). Medida del
		// Coseno.
		// 8. Los documentos se devuelven ordenados por orden de similitud (de mayor a
		// menor)
		ArrayList<SimilitudDocumento> similitudes = calcularSimilitudes(consultaInicial);

		// 9. Evaluar el resultados calculando el promedio (Ejemplo Anexo I) de
		// Precisión para 11 grados de Exhaustividad (1, 0'9, 0'8, …0).
		// E = nº de documentos relevantes recuperados / nº de documentos relevantes
		// P = nº de documentos relevantes recuperados / nº de documentos recuperados

		// 10. Identificar los documentos relevantes de prueba entre los primeros 15
		// documentos del ranking de similitud y el primer no relevante.

		// ... AG empieza en 17
		/*
		 * 17. Mejora consulta con AG:
		 * 
		 * a) Tomar 10 documentos: los relevantes obtenidos tras el proceso de
		 * retroalimentción (entre los primeros 15 del ranking) + los primeros no
		 * relevantes que devolvía la última consulta que no mejoró el promedio de
		 * Precisión.
		 * 
		 * 
		 * b) Construir una “ruleta” con estos 10 documentos. Dividir la ruleta en
		 * porciones iguales a la similitud que ofrece cada documento. A modo de
		 * porciones. (Ejemplo de Ruleta en Anexo II)
		 * 
		 * c) “Lanzar” la ruleta 10 veces. Seleccionando cada vez los documentos en los
		 * que pare.
		 * 
		 * d) Obtenemos así 10 documentos (“cromosomas”) padre. 5 parejas.
		 * 
		 * e) Probabilidad de cruce (Pc=0,7) entre parejas. Buscar un número aleatorio
		 * entre 0,0 y 1,0. Si el resultado es < 0,7. Se obtiene un número aleatorio
		 * entre 0 y n, y esa pareja se cruza en ese punto. Si el resultado es > 0,7. No
		 * se cruzan. Repetir para siguiente pareja. f) Probabilidad de mutación (Pm=
		 * 0,05) para pesos de términos (“genes”) que componen los cromosomas
		 * (documentos) Buscar un número aleatorio entre 0,00 y 1,00. Si el resultado es
		 * < 0,05. Se obtiene un número aleatorio entre 0 y el peso máximo de los
		 * descriptores de los 10 documentos padre iniciales y asignarlo al término. Si
		 * el resultado es > 0,05 No se muta. Repetir para siguiente peso (descriptor).
		 * g) Tenemos entonces 10 nuevos vectores. h) Sumar estos 10 vectores y la
		 * última consulta de retroalimentación por relevancia que no obtuvo resultados
		 * relevantes nuevos. Así obtenemos una consulta de prueba (Qp). i) Lanzar Qp al
		 * sistema y calcular promedio Precisión (P) para 11º de Exhaustividad (E). j)
		 * Restar 1er vector a Qp y lanzar esta nueva consulta (Qp') al sistema.
		 * Calcular promedio de Precisión para 11º de Exhaustividad de Qp'.
		 * 
		 * k) Si Qp' obtiene mejor promedio de P que Qp. Eliminar 1er vector. Si no,
		 * recuperar 1er vector. l) Repetir desde j) con los siguientes 9 vectores. m)
		 * Obtenemos finalmente la consulta que mejor promedio de Precisión a obtenido a
		 * partir de AAGG, la consulta Qg. ñ) Observar si mejora promedio de P para 11º
		 * de E. con respecto a Q' Si el resultado es positivo: volver al paso 11. Si el
		 * resultado es negativo: FIN.
		 * 
		 */

		/*
		 * log.trace("Iniciando AG");
		 * 
		 * log.trace("AG-1: Cogemos los resultados relevantes"); // 1. Tomamos los
		 * primeros AG_TAMANO_RULETA resultados de la lista de // relevantes + los
		 * últimos resultados ArrayList<Resultado> resultadosEntrada = new
		 * ArrayList<Resultado>( listaResultadosRelevantes); // copiamos //
		 * listaResultadosRelevantes en // ruleta // Añadimos un porcentaje de la última
		 * listaResultados int numAnadir; if (listaResultadosRelevantes.size() < 5)
		 * numAnadir = 10 - listaResultadosRelevantes.size(); else if
		 * (listaResultadosRelevantes.size() < 10) numAnadir = 15 -
		 * listaResultadosRelevantes.size(); else if (listaResultadosRelevantes.size() <
		 * 15) numAnadir = 20 - listaResultadosRelevantes.size(); else if
		 * (listaResultadosRelevantes.size() < 20) numAnadir = 25 -
		 * listaResultadosRelevantes.size(); else numAnadir = (int)
		 * (listaResultadosRelevantes.size() * AG_PCT_ULTIMOS_RESULTADOS);
		 * 
		 * Iterator<Resultado> it = listaResultados.iterator(); while (it.hasNext() &&
		 * (numAnadir > 0)) { resultadosEntrada.add(it.next()); numAnadir--; }
		 * log.trace("AG-1a: Calculamos sus pesos."); // calculamos el peso de cada
		 * resultado en la ruleta, proporcional a su // similitud ArrayList<Double>
		 * porcionRuletaResultado = new ArrayList<Double>(); int similitudTotal = 0; for
		 * (Resultado ruletaResultado : resultadosEntrada) similitudTotal +=
		 * ruletaResultado.getSimilitud(); for (Resultado ruletaResultado :
		 * resultadosEntrada) porcionRuletaResultado.add(ruletaResultado.getSimilitud()
		 * / similitudTotal);
		 * 
		 * log.trace("AG-1b: Calculamos la probalidad acumulada."); // calculamos la
		 * probabilidad acumulada ArrayList<Double> ruleta = new ArrayList<Double>();
		 * ruleta.add(porcionRuletaResultado.get(0)); for (int i = 1; i <
		 * porcionRuletaResultado.size(); i++) ruleta.add(ruleta.get(i - 1) +
		 * porcionRuletaResultado.get(i));
		 * 
		 * // Ver ruleta // System.out.print("Pesos ruleta: "); // for(Double pr :
		 * ruleta) // System.out.print(pr + " ");
		 * 
		 * ArrayList<Cromosoma> cromosomas; ArrayList<Resultado> resultadosEncontrados;
		 * do { // Los pasos 2 a 6 se repiten hasta que la calidad de los // resultados
		 * obtenidos sea la adecuada log.trace(
		 * "AG-2a: Selección por rueda de ruleta."); // 2. Selección por rueda de ruleta
		 * ArrayList<Resultado> resultadosElegidosParaCruzar = new
		 * ArrayList<Resultado>(); Double tiradaAleatoria; log.trace("Hay " +
		 * resultadosElegidosParaCruzar + " resultadosElegidosParaCruzar.");
		 * log.trace("Elegidos: "); for (int i = 0; i < resultadosEntrada.size(); i++) {
		 * // Seleccionamos // tantos // como // resultados // (relevantes // + %), //
		 * pero // puede // haber // repetidos tiradaAleatoria = Math.random(); Boolean
		 * encontrado = false; int j = 0; while (!encontrado) { if (ruleta.get(j) >
		 * tiradaAleatoria) { resultadosElegidosParaCruzar.add(resultadosEntrada
		 * .get(j)); encontrado = true; // System.out.print(j + " "); } j++; } }
		 * 
		 * log.trace(
		 * "AG-2b: Creamos un cromosoma modelo con todos los descriptores de la consulta."
		 * ); // Creamos los cromosomas de los resultados elegidos para cruzar // Crear
		 * cromosoma modelo a peso 0 con los descriptores de todos los //
		 * resultadosElegidos. Cromosoma cromosomaModelo = new Cromosoma();
		 * ArrayList<String> textosDescriptoresCromosomaModelo = new
		 * ArrayList<String>(); for (Resultado resultadoElegido :
		 * resultadosElegidosParaCruzar) for (Peso peso :
		 * resultadoElegido.getFuente().getPesos()) if
		 * (!textosDescriptoresCromosomaModelo.contains(peso
		 * .getDescriptor().getTexto()))// Evitamos duplicados
		 * textosDescriptoresCromosomaModelo.add(peso .getDescriptor().getTexto());
		 * 
		 * log.trace("AG-2c: Ordenamos el cromosoma modelo alfabéticamente."); //
		 * Ordenar el cromosoma modelo alfabéticamente.
		 * Collections.sort(textosDescriptoresCromosomaModelo);
		 * 
		 * for (String textoDescriptor : textosDescriptoresCromosomaModelo) { Descriptor
		 * descriptorTmp = new Descriptor(); descriptorTmp.setTexto(textoDescriptor);
		 * cromosomaModelo.getDescriptores().add(descriptorTmp);
		 * cromosomaModelo.getPesos().add(0.0); }
		 * 
		 * // verCromosoma(cromosomaModelo);
		 * 
		 * log.trace("AG-2d: Creamos los cromosomas para cada resultado."); // Creamos
		 * la lista de cromosomas (antes del cruce) int i2 = 0; cromosomas = new
		 * ArrayList<Cromosoma>(); for (Resultado resultadoElegido :
		 * resultadosElegidosParaCruzar) { Cromosoma clon = cromosomaModelo.clone();
		 * log.trace( "Creando cromosoma para resultado " + i2++); for (Peso peso :
		 * resultadoElegido.getFuente().getPesos()) { // Buscamos el descriptor y
		 * cambiamos el peso en el clon Boolean encontrado = false; int i = 0; //
		 * System.out.println("-- Buscando descriptor " + //
		 * peso.getDescriptor().getTexto()); while (i < clon.getPesos().size() &&
		 * !encontrado) { // System.out.println("--- comparando con " + //
		 * clon.getDescriptores().get(i).getTexto()); if
		 * (clon.getDescriptores().get(i).getTexto()
		 * .equals(peso.getDescriptor().getTexto())) { clon.getPesos().set(i,
		 * peso.getPeso()); encontrado = true; // System.out.println(
		 * "---- ¡Encontrado!"); } i++; } } // verCromosoma(clon); cromosomas.add(clon);
		 * }
		 * 
		 * log.trace("AG-3: Cruzamos los cromosomas"); // 3. Cruce en un punto Cromosoma
		 * cromosomaPrevio = null; Cromosoma cromosomaPrimero = null; // Primer
		 * cromosoma seleccionado // (para cruzarlo si queda el // último suelto for
		 * (Cromosoma cromosoma : cromosomas) { if (Math.random() < Searcher.AG_PC) { //
		 * Si el elegio para // cruzar if (cromosomaPrimero == null) cromosomaPrimero =
		 * cromosoma.clone(); if (cromosomaPrevio == null) // No hay ninguno previo
		 * cromosomaPrevio = cromosoma; else { // cruzamos cromosoma con cromosomaPrevio
		 * cruzarCromosomas(cromosomaPrevio, cromosoma); cromosomaPrevio = null; // Para
		 * detectar el último // suelto } } } if (cromosomaPrevio != null) { // Se nos
		 * ha quedado el primero que // fue seleccionado
		 * cruzarCromosomas(cromosomaPrevio, cromosomaPrimero);// Cruzamos // con el //
		 * primero }
		 * 
		 * log.trace("AG-4: Mutamos los cromosomas."); // 4. Mutación // Seleccionamos
		 * los pesos máximos y mínimos Criteria crit =
		 * Delphos.getSession().createCriteria(Peso.class);
		 * crit.setProjection(Projections.max("peso")); Double maxPeso = (Double)
		 * crit.uniqueResult();
		 * 
		 * Criteria crit2 = Delphos.getSession().createCriteria(Peso.class);
		 * crit2.add(Restrictions.gt("peso", 0.0));
		 * crit2.setProjection(Projections.min("peso")); Double minPeso = (Double)
		 * crit.uniqueResult();
		 * 
		 * double rangoPeso = maxPeso - minPeso;
		 * 
		 * for (Cromosoma cromosoma : cromosomas) { for (Double peso :
		 * cromosoma.getPesos()) if (Math.random() < Searcher.AG_PM) // Se muta el peso
		 * peso = minPeso + Math.random() * rangoPeso; }
		 * 
		 * log.trace("AG-5: Construimos la nueva consulta."); // 5. Construimos la
		 * consulta sumando consultaInicial + // listaResultadosRelevantes + Cromosomas
		 * (mutados) Set<Peso> descriptoresConsulta =
		 * getDescriptoresConsulta(cromosomas);
		 * 
		 * // Lanzamos la consulta resultadosEncontrados = buscar(descriptoresConsulta,
		 * sectores, tiposOrganizacion, localizaciones);
		 * 
		 * // 6. Comprobamos si están todos los resultadosRelevantes en los // primeros
		 * umbral+Relevantes resultadosEncontrados } while
		 * (!estanTodosRelevantes(resultadosEncontrados)); // Repetimos // pasos 2 a 6
		 * // mientras // falten // resultados
		 * 
		 * // 7. 8. 11. 12. Comprobamos la utilidad de cada cromosoma
		 * ArrayList<Cromosoma> cromosomasUtiles = new ArrayList<Cromosoma>(
		 * cromosomas); for (Cromosoma cromosomaAEliminar : cromosomas) {
		 * cromosomasUtiles.remove(cromosomaAEliminar); Set<Peso>
		 * descriptoresTmpConsulta = getDescriptoresConsulta(cromosomasUtiles);
		 * ArrayList<Resultado> resultadosTmpEncontrados = buscar(
		 * descriptoresTmpConsulta, sectores, tiposOrganizacion, localizaciones); if
		 * (!estanTodosRelevantes(resultadosTmpEncontrados))
		 * cromosomasUtiles.add(cromosomaAEliminar); // 11. Rescatamos el // cromosoma }
		 * 
		 * log.trace("Fin AG"); // Finalmente, lanzamos una RRMax con la consulta
		 * mejorada Set<Peso> descriptoresConsultaUtiles =
		 * getDescriptoresConsulta(cromosomasUtiles); return
		 * buscar(descriptoresConsultaUtiles, sectores, tiposOrganizacion,
		 * localizaciones);
		 */return null;
	}

	public static ArrayList<Resultado> mejorarRRmax(ArrayList<Resultado> listaResultadosRelevantes,
			Resultado resultadoNoRelevante, Set<Jerarquia> sectores, Set<Jerarquia> tiposOrganizacion,
			Set<Jerarquia> localizaciones) {

		Searcher.listaResultadosRelevantes = listaResultadosRelevantes;

		Set<Peso> descriptoresConsulta = new HashSet<Peso>();

		// Añadimos la Consulta Inicial al conjunto de descriptoresConsulta
		descriptoresConsulta.addAll(consultaInicial);

		sumarRelevantes(descriptoresConsulta);
		restarNoRelevante(descriptoresConsulta, resultadoNoRelevante);

		int umbral = Searcher.RESULTADOS_UMBRAL + listaResultadosRelevantes.size();
		List listaObjetosResultado = null;
		listaObjetosResultado = buscar(descriptoresConsulta, sectores, tiposOrganizacion, localizaciones);

		if (listaObjetosResultado == null)
			return null;

		eliminarRelevantes(listaObjetosResultado);
		listaResultados = expandir(listaObjetosResultado);

		return listaResultados;

	}

	public static ArrayList<Resultado> mejorarRRmin(ArrayList<Resultado> listaResultadosRelevantes,
			Resultado resultadoNoRelevante, Set<Jerarquia> sectores, Set<Jerarquia> tiposOrganizacion,
			Set<Jerarquia> localizaciones) {

		Searcher.listaResultadosRelevantes = listaResultadosRelevantes;

		List listaObjetosResultado = null;

		for (int i = 0; i < Searcher.MEJORA_NUM_ITERACIONES; i++) {
			// Creamos una consulta con los descriptores de los resultados
			// marcados
			// como relevantes
			Set<Peso> descriptoresConsulta = new HashSet<Peso>();

			// Sumamos la Consulta Inicial al conjunto de descriptoresConsulta
			descriptoresConsulta.addAll(consultaInicial);

			sumarRelevantes(descriptoresConsulta);

			if (i == 0) // En la primera iteración
				restarNoRelevante(descriptoresConsulta, resultadoNoRelevante);

			listaObjetosResultado = buscar(descriptoresConsulta, sectores, tiposOrganizacion, localizaciones);

			if (i < Searcher.MEJORA_NUM_ITERACIONES - 1) // Eliminamos los
															// resultados por
															// encima del número
															// establecido
				while (listaObjetosResultado.size() > Searcher.MEJORA_NUM_RESULTADOS)
					listaObjetosResultado.remove(Searcher.MEJORA_NUM_RESULTADOS);

		}

		eliminarRelevantes(listaObjetosResultado);
		listaResultados = expandir(listaObjetosResultado);
		// limitarResultados();

		return listaResultados;
	}

	private static ArrayList<Licitacion> obtenerDocumentosTED(String html) throws Exception {
		ArrayList<Licitacion> listaLicitaciones = new ArrayList<Licitacion>();
		Document doc = Jsoup.parse(html);
		Elements nodos = doc.select("table[id=notice] > tbody a");
		System.out.println("Hay " + nodos.size() + " enlaces.");
		Iterator it = nodos.iterator();
		while (it.hasNext()) {
			Element link = (Element) it.next();
			// System.out.println(numDocs++ + ") " + link.text() + " a " +
			// "http://ted.europa.eu" + link.attr("href"));
			URL urlHTML = new URL("http://ted.europa.eu" + link.attr("href") + "&tabId=3"); // Directamente
																							// a
																							// la
																							// pestaña
																							// de
																							// Datos
			System.out.println("URL: " + urlHTML.toString());
			conn = (HttpURLConnection) urlHTML.openConnection();
			String docHTML = verPagina(urlHTML, null);
			// escribirFichero(docHTML);
			Licitacion licitacion = parsear(docHTML);
			licitacion.setUrl(urlHTML);
			listaLicitaciones.add(licitacion);
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
				licitacion.setResumen(nodos.get(0).text());
			System.out.println("\nResumen: " + licitacion.getResumen() + "\n");
		}
		return listaLicitaciones;
	}

	private static String obtenerTokenEPO() {
		String token = null;
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

	private static Licitacion parsear(String docHTML) {
		Licitacion licitacion = new Licitacion();
		Document doc = Jsoup.parse(docHTML);

		// Obtenemos el título:
		Elements nodos = doc.select("th:containsOwn(TI) + td + td");
		String titulo = nodos.get(0).text();
		licitacion.setTitulo(titulo);
		System.out.println("Título: " + titulo);

		// Obtenemos Quien:
		nodos = doc.select("th:containsOwn(AU) + td + td");
		String quien = nodos.get(0).text();
		licitacion.setEntidadEmisora(quien);
		System.out.println("Quién: " + quien);

		// Obtenemos Donde:
		nodos = doc.select("th:containsOwn(TW) + td + td");
		String donde = nodos.get(0).text();
		nodos = doc.select("th:containsOwn(CY) + td + td");
		donde += " - " + nodos.get(0).text();
		System.out.println("Dónde: " + donde);
		licitacion.setLocalizacion(donde);

		// Obtenemos Cuando:
		nodos = doc.select("th:containsOwn(PD) + td + td");
		String cuando = nodos.get(0).text();
		System.out.println("Cuándo: " + cuando);
		licitacion.setFechaPublicacion(cuando);

		// Lista de CPV
		nodos = doc.select("th:containsOwn(PC) + td + td");
		licitacion.setListaCPV(nodos.get(0).text()); // Vienen separados por
														// <br/>

		// Lista de Tipos de Documento
		nodos = doc.select("th:containsOwn(TD) + td + td");
		licitacion.setTipoDocumento(nodos.get(0).text());

		return licitacion;
	}

	private static void quitarImpresentables(String textoConsulta) {
		// Quitamos los resultados en los que no aparezca ninguno de los
		// términos de la consulta en el texto
		Iterator<Resultado> it = listaResultados.iterator();
		while (it.hasNext()) {
			Resultado r = (Resultado) it.next();
			Fuente f = r.getFuente();
			if (Parser.getExtractoFuente(f, textoConsulta) == null)
				it.remove();
		}

	}

	private static void restarNoRelevante(Set<Peso> descriptoresConsulta, Resultado resultadoNoRelevante) {
		if (resultadoNoRelevante == null)
			return;
		// Restamos los pesos del resultado marcado como NoRelevante
		for (Peso descriptorNoRelevante : resultadoNoRelevante.getFuente().getPesos())
			// Si no está, lo añadimos con peso negativo
			if (!descriptoresConsulta.contains(descriptorNoRelevante)) {
				Peso nuevoDescriptor = (Peso) descriptorNoRelevante.clone();
				nuevoDescriptor.setPeso(-1 * nuevoDescriptor.getPeso());
				descriptoresConsulta.add(nuevoDescriptor);
			} else {// Si está, lo buscamos en los descriptores de la Consulta y
					// le restamos el peso del descriptor No Relevante
				Boolean encontrado = false;
				Iterator<Peso> it = descriptoresConsulta.iterator();
				while (it.hasNext() && !encontrado) {
					Peso descriptorConsulta = it.next();
					if (descriptorConsulta.equals(descriptorNoRelevante)) {
						descriptorConsulta.setPeso(descriptorConsulta.getPeso() - descriptorNoRelevante.getPeso()); // Sumamos
																													// su
																													// peso
						encontrado = true;
					}
				}
			}
	}

	private static void sumarRelevantes(Set<Peso> descriptoresConsulta) {
		// Sumamos a descriptoresConsulta los resultados marcados como
		// relevantes
		for (Resultado resultado : Searcher.listaResultados)
			if (resultado.isRelevante())
				for (Peso descriptorListaResultados : resultado.getFuente().getPesos())
					if (!descriptoresConsulta.contains(descriptorListaResultados))
						descriptoresConsulta.add(descriptorListaResultados);
					else {// Si está...lo buscamos...
						Boolean encontrado = false;
						Iterator<Peso> it = descriptoresConsulta.iterator();
						while (it.hasNext() && !encontrado) {
							Peso descriptorConsulta = it.next();
							if (descriptorConsulta.equals(descriptorListaResultados)) {
								descriptorConsulta
										.setPeso(descriptorConsulta.getPeso() + descriptorListaResultados.getPeso()); // Sumamos
																														// su
																														// peso
								encontrado = true;
							}
						}
					}
	}

	private static void verCabeceras() {
		String headerName = null;
		for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++)
			System.out.println("Cabecera: " + conn.getHeaderFieldKey(i) + " = " + conn.getHeaderField(i));
	}

	private static ArrayList<String> verCodigosPatente_Sector(Integer idPatente_Sector) {
		ArrayList<String> resultado = new ArrayList<String>();
		String sql = "SELECT id, nombre, descripcion FROM Patente_Sector WHERE idPadre = " + idPatente_Sector;
		Query query = Delphos.getSession().createSQLQuery(sql);
		List list = query.list();
		Iterator<Object[]> it = list.iterator();
		while (it.hasNext()) {
			Object[] tupla = it.next();
			if (tupla[2] == null) // No tiene descripción
				resultado.addAll(verCodigosPatente_Sector(Integer.valueOf(tupla[0].toString())));
			else
				resultado.add(tupla[1].toString());
		}
		return resultado;
	}

	private static void verCookies() {
		List<HttpCookie> lista = cookieManager.getCookieStore().getCookies();
		for (HttpCookie cookie : lista)
			System.out.println("Cookie: " + cookie);
	}

	private static void verCromosoma(Cromosoma cromosoma) {
		System.out.println("Analizando cromosoma:");
		for (int i = 0; i < cromosoma.getDescriptores().size(); i++) {
			System.out.print(cromosoma.getDescriptores().get(i).getTexto() + " - ");
			System.out.println(cromosoma.getPesos().get(i) + ". ");
		}
		System.out.println("Descriptores: " + cromosoma.getDescriptores().size());
		System.out.println("Pesos: " + cromosoma.getPesos().size());
	}

	private static void verDescriptores(Set<Peso> descriptores) {
		// Muestra por syso el conjunto de descritores y sus pesos
		int i = 1;
		for (Peso peso : descriptores)
			System.out.println(
					i++ + ".- " + peso.getTextoDescriptor() + "(" + peso.getId() + ") peso = " + peso.getPeso());
	}

	private static ArrayList<Integer> verIdSectores(Set<Sector> jerarquia) {
		int tamano = 0;
		ArrayList<Integer> listaIds = new ArrayList<Integer>();

		while (jerarquia.size() > tamano) { // Iteramos hasta que la lista no
											// crece
			tamano = jerarquia.size();
			listaIds = new ArrayList<Integer>();
			Class clase = null;
			for (Jerarquia j : jerarquia) {
				listaIds.add(j.getId());
				clase = j.getClass();
			}
			Criteria crit = Delphos.getSession().createCriteria(clase);
			crit.add(Restrictions.in("padre", jerarquia));
			crit.add(Restrictions.not(Restrictions.in("id", listaIds)));
			jerarquia.addAll(crit.list());
		}

		return listaIds;
	}

	private static String verIdsSeparadosPorComas(Set set, Class clase) {
		// Recibe una lista de ids de jerarquía, la desarrolla y devuelve la
		// lista de ids separados por comas
		int tamano = 0;
		System.out.println("Inicial:" + tamano);
		ArrayList<Integer> listaIds = new ArrayList<Integer>();

		if (clase != Localizacion.class) {
			// Desarrollamos la jerarquía para buscar los hijos
			while (set.size() > tamano) {
				tamano = set.size();
				listaIds = new ArrayList<Integer>();
				for (Object s : set)
					listaIds.add(((Jerarquia) s).getId());
				Criteria crit = Delphos.getSession().createCriteria(clase);
				crit.add(Restrictions.in("padre", set));
				crit.add(Restrictions.not(Restrictions.in("id", listaIds)));
				set.addAll(crit.list());
			}
		} else {
			// Para localización, no añadimos los hijos
			for (Object s : set)
				listaIds.add(((Jerarquia) s).getId());
		}
		String resultado = "";
		if (listaIds.size() > 0) {
			Iterator it = listaIds.iterator();
			resultado = String.valueOf(it.next());
			while (it.hasNext())
				resultado += ", " + String.valueOf(it.next());
		}
		return resultado;
	}

	private static String verIdsSeparadosPorComas(Set<Jerarquia> set) {
		// Recibe una lista de ids de jerarquía, la desarrolla y devuelve la
		// lista de ids separados por comas
		int tamano = 0;
		System.out.println("Inicial:" + tamano);
		ArrayList<Integer> listaIds = new ArrayList<Integer>();
		// Desarrollamos la jerarquía para buscar los hijos
		while (set.size() > tamano) {
			tamano = set.size();
			listaIds = new ArrayList<Integer>();
			for (Jerarquia s : set)
				listaIds.add(s.getId());
			Criteria crit = Delphos.getSession().createCriteria(Sector.class);
			crit.add(Restrictions.in("padre", set));
			crit.add(Restrictions.not(Restrictions.in("id", listaIds)));
			set.addAll(crit.list());
		}
		String resultado = "";
		if (listaIds.size() > 0) {
			Iterator it = listaIds.iterator();
			resultado = String.valueOf(it.next());
			while (it.hasNext())
				resultado += ", " + String.valueOf(it.next());
		}
		return resultado;
	}

	private static int verNumDocs(Calendar fechaInicioPeriodo, Calendar fechaFinIntervalo, Tendencia tendencia) {
		ArrayList<String> urls = new ArrayList<>();

		// TODO:Mejora. Guardar el último resultado obtenido y si la consulta es la
		// misma (porque es para otro periodo, utilizarla)
		// Es decir, montar una caché de urls.

		System.out.println("En Searcher.verNumDocs");

		numTotalDocs = 0;

		// Recorremos la lista de clasificaciones
		for (Documento_Clasificacion dc : tendencia.getListaDocumentoClasificacion()) {
			urls.add(dc.descripcion + "/advanced-search");
		}
		if (urls.isEmpty())
			// Añadimos la url por defecto (todo DSpace)
			urls.add("http://dspace.mit.edu/advanced-search");
		int resultado = 0;

		for (String sUrl : urls) {
			int page = 1;
			int numDocs = 0;
			do {
				try {
					URL url = new URL(sUrl);
					System.out.println("URL: " + url);
					conn = (HttpURLConnection) url.openConnection();
					String params = "num_search_field=3&results_per_page=100&";
					if (url.equals("http://dspace.mit.edu/advanced-search"))
						params += "scope=%2F&";
					params += "field1=ANY";
					params += "&page=" + page;
					params += "&query1=" + tendencia.getTextoLibre().replace(" ", "+");
					params += "&conjunction2=AND&field2=";
					if (tendencia.getDocumentoAutor().isEmpty())
						params += "ANY&query2=";
					else
						params += "author&query2=" + tendencia.getDocumentoAutor().replace(" ", "+");
					params += "&conjunction3=AND&field3=ANY&query3=&rpp=10&sort_by=2&order=DESC&submit=Ir";

					// params =
					// "order=DESC&rpp=100&sort_by=2&page=1&conjunction1=AND&results_per_page=10&etal=0&field1=ANY&num_search_field=3&query1=energy";

					String html = verPagina(url, params);
					escribirFichero(html);

					Document doc = Jsoup.parse(html);
					if (numTotalDocs == 0) {
						try {
							String numTotal = doc.select(".pagination-info").first().text();
							numTotalDocs = Integer.valueOf(numTotal.substring(numTotal.indexOf("de ") + 3));
						} catch (NullPointerException e) {
							numTotalDocs = 0;
							break;
						}
						System.out.println("TOTAL DE DOCUMENTOS: " + numTotalDocs);
					}

					Elements listaLi = doc.select("li.ds-artifact-item");
					for (Element li : listaLi) {
						numDocs++;
						System.out.println("Leyendo documento " + numDocs + " de " + numTotalDocs + " página: " + page);
						Element aTitulo = li.select("div.artifact-title>a").first();
						String docTitulo = aTitulo.text();
						String docUrl = "http://dspace.mit.edu" + aTitulo.attr("href");
						String docAutor = li.select("span.author").text();
						String docEntidad = li.select("span.publisher").text();
						String docFechaPublicacion = li.select("span.date").text();
						String docResumen = li.select(".artifact-abstract").text();
						Document docDetalle = Jsoup.connect(docUrl + "?show=full").get();

						// Si hay rango de fechas
						Date fechaCandidato = adivinarFechaDocAcademico(docFechaPublicacion);
						System.out.println("Fecha Documento: " + fechaCandidato);
						if (fechaCandidato == null) {
							System.out.println("Falla por fechaCandidato nula.");
							continue;
						}

						Date fechaDesde = fechaInicioPeriodo.getTime();
						if (fechaCandidato.compareTo(fechaDesde) < 0) {
							System.out.println("Falla por fecha inferior a periodo: " + fechaDesde);
							continue;
						}

						Date fechaHasta = fechaFinIntervalo.getTime();
						if (fechaCandidato.compareTo(fechaHasta) > 0) {
							System.out.println("Falla por fecha superior a periodo: " + fechaHasta);
							continue;
						}

						// Si hay entidad
						if (!tendencia.getDocumentoEntidad().isEmpty()) {
							if (!docEntidad.toLowerCase().contains(tendencia.getDocumentoEntidad().toLowerCase())) {
								System.out.println("Falla por no coincide la entidad.");
								continue;
							}
						}
						System.out.println("COINCIDE.");
						resultado++;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				page++;
			} while (numDocs < numTotalDocs);
		}

		return resultado;
	}

	public static int verNumLicitacionesModoExperto(Calendar fechaDesde, Calendar fechaHasta, Tendencia tendencia)
			throws Exception {

		URL url = new URL("http://ted.europa.eu/TED/search/search.do?");

		String html = verPagina(url,
				construirParametrosPostBusquedaExpertaLicitaciones(tendencia.getTextoLibre(),
						(GregorianCalendar) fechaDesde, (GregorianCalendar) fechaHasta,
						tendencia.getListaLicitacionLocalizacion(), tendencia.getLicitacionTipo(),
						tendencia.getLicitacionEntidadSolicitante(), tendencia.getListaLicitacionSector()));

		escribirFichero(html);
		return verNumResultados(html);
	}

	private static int verNumPag(String html) {
		int resultado = 0;
		// System.out.println(html);
		Document doc = Jsoup.parse(html);
		Elements nodos = doc.select("a:containsOwn(Last)");
		if (nodos.size() > 0) {
			String texto = ((Element) nodos.get(0)).attr("href");
			resultado = Integer.parseInt(texto.substring(texto.indexOf("=") + 1, texto.length()));
		}
		System.out.println("Hay " + resultado + " páginas de resultado");
		return resultado;
	}

	public static int verNumPatentes(Calendar fechaDesde, Calendar fechaHasta, Tendencia tendencia) throws Exception {
		int numPatentes;

		ArrayList<Patente> listaPatentes = new ArrayList<Patente>();

		// Búsqueda de Documentos en EPO
		String scheme = "https";
		String authority = "ops.epo.org";
		String path = "/3.1/rest-services/published-data/search";

		// String query = "q=";
		ArrayList<String> condiciones = new ArrayList<String>();

		ArrayList<String> alCPI = new ArrayList<String>();
		String queryCPI = null;
		if (tendencia.getListaPatenteSector() != null)
			if (tendencia.getListaPatenteSector().size() != 0) {
				Iterator<Patente_Sector> it = tendencia.getListaPatenteSector().iterator();
				while (it.hasNext()) {
					Patente_Sector ps = it.next();
					if (ps.descripcion == null) // Si es null, es un sector sin
												// código de CPI
						// Cargamos todos sus subcódigos
						alCPI.addAll(verCodigosPatente_Sector(ps.id));
					else
						alCPI.add(ps.getNombre());
				}
				queryCPI = "(ipc=/low ";
				for (String cpi : alCPI)
					queryCPI += cpi + " or ipc=/low ";
				queryCPI = queryCPI.substring(0, queryCPI.length() - " or ipc=/low ".length());
				queryCPI += ")";
			}
		if (alCPI.size() > 10)
			throw new Exception("Demasiados códigos CPI");

		if (queryCPI != null) {
			System.out.println("Añadiendo: " + queryCPI);
			condiciones.add(queryCPI);
		}

		ArrayList<String> alLocalizacion = new ArrayList<String>();
		String queryLocalizacion = null;
		if (tendencia.getListaPatenteLocalizacion() != null)
			if (tendencia.getListaPatenteLocalizacion().size() != 0) {
				Iterator<Patente_Localizacion> it2 = tendencia.getListaPatenteLocalizacion().iterator();
				while (it2.hasNext()) {
					alLocalizacion.add(it2.next().getNombre());
				}
				queryLocalizacion = "(pn= ";
				for (String loc : alLocalizacion)
					queryLocalizacion += loc + " or ";
				queryLocalizacion = queryLocalizacion.substring(0, queryLocalizacion.length() - " or ".length());
				queryLocalizacion += ")";
			}
		if (queryLocalizacion != null) {
			System.out.println("Añadiendo: " + queryLocalizacion);
			condiciones.add(queryLocalizacion);
		}

		if (!tendencia.getTerminoPrincipal().isEmpty()) {
			String textoLibre = tendencia.getTextoLibre();
			if (textoLibre.contains("\""))
				condiciones.add("ta=" + textoLibre); // ta
														// es
														// titleandabstract
			else
				condiciones.add("ta all \"" + tendencia.getTerminoPrincipal() + "\"");
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if ((fechaDesde != null) && (fechaHasta != null)) {
			String condicion = "publicationdate within \"";
			sdf.setCalendar(fechaDesde);
			condicion += sdf.format(fechaDesde.getTime());
			sdf.setCalendar(fechaHasta);
			condicion += " " + sdf.format(fechaHasta.getTime()) + "\"";
			condiciones.add(condicion);
		} else {
			if (fechaDesde != null) {
				sdf.setCalendar(fechaDesde);
				condiciones.add("publicationdate>=" + sdf.format(fechaDesde.getTime()));
			}
			if (fechaHasta != null) {
				sdf.setCalendar(fechaHasta);
				condiciones.add("publicationdate<=" + sdf.format(fechaHasta.getTime()));
			}
		}

		if (tendencia.getPatenteInventor() != null)
			if (!tendencia.getPatenteInventor().isEmpty())
				condiciones.add("inventor=\"" + tendencia.getPatenteInventor() + "\"");
		if (tendencia.getPatenteSolicitante() != null)
			if (!tendencia.getPatenteSolicitante().isEmpty())
				condiciones.add("applicant=\"" + tendencia.getPatenteSolicitante() + "\"");

		String query = "q=";
		for (int i = 0; i < condiciones.size(); i++) {
			if (i == 0)
				query += condiciones.get(i);
			else
				query += " and " + condiciones.get(i);
		}

		log.trace("query: " + query);

		URI uri = new URI(scheme, authority, path, query, null);
		log.info(uri.toString());

		HttpsURLConnection con = (HttpsURLConnection) (uri.toURL().openConnection());

		con.setRequestMethod("GET");
		String token = obtenerTokenEPO();
		con.setRequestProperty("Authorization", "Bearer " + token);
		// String Range = "" + indiceBusquedaEPO + "-" + (indiceBusquedaEPO +
		// 99);
		// System.out.println(Range);
		// con.setRequestProperty("X-OPS-Range", Range);

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

		sdf.setCalendar(fechaDesde);
		System.out.print("numPatentes entre " + sdf.format(fechaDesde.getTime()) + " y ");
		sdf.setCalendar(fechaHasta);
		System.out.print(sdf.format(fechaHasta.getTime()) + " = ");
		numPatentes = Integer.parseInt(doc.getElementsByTagName("ops:biblio-search").item(0).getAttributes()
				.getNamedItem("total-result-count").getTextContent());
		System.out.println(numPatentes);
		if (numPatentes >= 100000) { // Se ha saturado
			System.out.println("SATURADO");
			Thread.sleep(2000);
			Calendar fechaMedia = new GregorianCalendar();
			fechaMedia.setTimeInMillis((fechaDesde.getTimeInMillis() + fechaHasta.getTimeInMillis()) / 2);
			return verNumPatentes(fechaDesde, fechaMedia, tendencia)
					+ verNumPatentes(fechaMedia, fechaHasta, tendencia);
		} else
			return numPatentes;
	}

	private static int verNumResultados(String html) {
		// Buscar <span class="pagebanner">91,050 elements found, displaying 1
		// to 25.</span>
		// Han cambiado: Showing 1 - 25 of 2,391 results. Y en div

		int resultado = 0;
		Document doc = Jsoup.parse(html);
		Elements nodos = doc.select("div.pagebanner");
		if (nodos.size() > 0) {
			if (nodos.get(0).text().contains("One"))
				resultado = 1;
			else {
				String texto = nodos.get(0).text();
				int inicio = texto.indexOf("of ") + 3;
				int fin = texto.indexOf(" result");
				// resultado = Integer.parseInt(nodos.get(0).text().split(" ")[0].replace(",",
				// ""));
				resultado = Integer.parseInt(texto.substring(inicio, fin).replace(",", ""));
			}
		}

		System.out.println("verNumResultados = " + resultado);
		return resultado;
	}

	public static int verNumTotalDocsDSpace() {
		int result = 0;
		try {
			Document doc = Jsoup.connect("http://dspace.mit.edu/browse?type=dateissued").get();
			Element elem = doc.select("p.pagination-info").get(0);
			String[] trozos = elem.text().split(" ");
			result = Integer.parseInt(trozos[trozos.length - 1]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static int verNumTotalPatentes(Calendar fechaDesde, Calendar fechaHasta, Tendencia tendencia)
			throws Exception {
		if (!tendencia.getIndicadorPatentes())
			return -12;

		// Detectamos el tipo de total que hay que calcular
		if (!tendencia.getTerminoPrincipal().isEmpty() && tendencia.getListaPatenteLocalizacion().isEmpty()
				&& tendencia.getListaPatenteSector().isEmpty() && tendencia.getPatenteInventor().isEmpty()
				&& tendencia.getPatenteSolicitante().isEmpty()) {
			// Caso 1. Con texto libre y sin filtros; comparamos con el total de
			// documentos sin filtros, sin término.
			// System.out.println("verNumTotalLicitaciones: Caso 1");
			Tendencia tendenciaClon = (Tendencia) tendencia.clone();
			tendenciaClon.setTerminoPrincipal("");
			return verNumPatentes(fechaDesde, fechaHasta, tendenciaClon);
		}
		if (!tendencia.getTerminoPrincipal().isEmpty()
				&& (!tendencia.getListaPatenteLocalizacion().isEmpty() || !tendencia.getListaPatenteSector().isEmpty()
						|| !tendencia.getPatenteInventor().isEmpty() || !tendencia.getPatenteSolicitante().isEmpty())) {
			// 2) Si el usuario introduce un término y uno o más filtros: El
			// sistema compara los resultados obtenidos por la consulta en la
			// que figuran el término y sus filtros, con los resultados
			// obtenidos solo por los filtros, poniendo a nulo el término.
			Tendencia tendenciaClon = (Tendencia) tendencia.clone();
			tendenciaClon.setTerminoPrincipal("");
			return verNumPatentes(fechaDesde, fechaHasta, tendenciaClon);
		}
		if (tendencia.getTerminoPrincipal().isEmpty()
				&& (!tendencia.getListaPatenteLocalizacion().isEmpty() ^ !tendencia.getListaPatenteSector().isEmpty()
						^ !tendencia.getPatenteInventor().isEmpty() ^ !tendencia.getPatenteSolicitante().isEmpty())) {
			// 1) Si no se introduce término y se selecciona un solo filtro: El
			// sistema compara los resultados obtenidos por ese filtro, con el
			// total de documentos existentes en el período indicado sin filtros
			// para hallar el porcentaje.
			Tendencia tendenciaClon = (Tendencia) tendencia.clone();
			tendenciaClon.setListaPatenteLocalizacion(null);
			tendenciaClon.setListaPatenteSector(null);
			tendenciaClon.setPatenteInventor(null);
			tendenciaClon.setPatenteSolicitante(null);
			return verNumPatentes(fechaDesde, fechaHasta, tendenciaClon);
		}
		// Preguntar filtro principal

		return -3;

	}

	private static String verPagina(URL url, String postParameters) throws Exception {
		System.out.println("\nverPagina: Conectando a : " + url);
		System.out.println("\nverPagina: Paramátros POST : " + postParameters);
		conn.setInstanceFollowRedirects(true);
		HttpURLConnection.setFollowRedirects(true);

		// Establecemos las cabeceras
		conn.setReadTimeout(500000);
		conn.setRequestProperty("Host", "ted.europa.eu");
		conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:33.0) Gecko/20100101 Firefox/33.0");
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
			// conn.setRequestProperty("Content-Length", "" +
			// Integer.toString(postParameters.getBytes().length));
			// conn.setFixedLengthStreamingMode(postParameters.getBytes().length);
			conn.setUseCaches(false);

			System.out.println("Poniendo parámetros de POST: " + postParameters);
			// System.out.println("Long String: " + postParameters.length());
			// System.out.println("Long Bytes: " +
			// Integer.toString(postParameters.getBytes().length));

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			// wr.write(postParameters.getBytes( StandardCharsets.UTF_8 ));
			wr.writeBytes(postParameters);
			wr.flush();
			wr.close();
		}

		boolean redirect = false;

		// Vemos las cabeceras
		// System.out.println("Respuesta: " + conn.getResponseCode());
		// Map<String, List<String>> map = conn.getHeaderFields();
		// for (Map.Entry<String, List<String>> entry : map.entrySet()) {
		// System.out.println("Key : " + entry.getKey() +
		// " ,Value : " + entry.getValue());
		// }

		// System.out.println("Procesando respuesta:");
		int status = conn.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
		}

		if (redirect) {

			// get redirect url from "location" header field
			String newUrl = conn.getHeaderField("Location");

			// get the cookie if need, for login
			if (conn.getHeaderField("Set-Cookie") != null) {
				cookie = conn.getHeaderField("Set-Cookie");
				// System.out.println("Cookie: " + cookie);
			}

			// System.out.println("Redirect to URL : " + newUrl);
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

		// System.out.println("URL Content... \n" + html.toString());

		return html.toString();
	}

}