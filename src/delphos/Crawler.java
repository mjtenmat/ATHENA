/**
 * Crawler.java - Busca los documentos asociados a las Fuentes y los guarda en la base de datos
 *
 * @version 0.1
 * @author María José Tena
 */

package delphos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import delphos.iu.Delphos;

public class Crawler {
	private final static Logger log = Logger.getLogger(Crawler.class);
	private static Session session;
	private static String hibernateCfgFile = "hibernate.cfg.xml";

	/*
	 * En MODO_NUEVO (1), recorre todas las Fuentes con Documento nulo y Error
	 * nulo, trayendo su Documento. En MODO_ACTUALIZAR (2), actualiza los
	 * Documentos de todas las Fuentes.
	 */
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure();
		log.info("Arrancando.");

		Fuente fuente = null;

		while (true) {
			try {
				Delphos.setHibernateCfgFile("hibernate.cfg.xml");
				session = Delphos.getSession();

				int indice = 0;
				Query query = session.createSQLQuery("SELECT MAX(id) FROM Fuente");
				int maxId = (int) query.uniqueResult();
				ArrayList<String> tipoFicheroConocido = new ArrayList<String>();
				tipoFicheroConocido.add("html");
				tipoFicheroConocido.add("htm");
				tipoFicheroConocido.add("txt");
				tipoFicheroConocido.add("php");
				tipoFicheroConocido.add("aspx");
				tipoFicheroConocido.add("/");
				tipoFicheroConocido.add("");

				ArrayList<TerminoTecnologiaEmergente> terminosTecnologiasEmergentes = cargarTerminosTecnologiasEmergentes();

				while (indice < maxId) {
					if (args.length > 0)
						fuente = obtenerFuenteParaActualizar(indice++);
					else
						fuente = obtenerFuente(indice++);

					log.info("Procesando Fuente (" + fuente.getId() + "): " + fuente.getUrl().toString());
					// System.exit(0);
					// Evitando pdf, zip
					String tipoFichero = fuente.getUrl().getFile().substring(fuente.getUrl().getFile().lastIndexOf('.') + 1);
					if (tipoFicheroConocido.contains(tipoFichero) || tipoFichero == null || tipoFichero.contains("/")) {
						log.trace("Actualizando Fuente (" + fuente.getId() + "): " + fuente.getUrl().toString());
						if (fuente.actualizar()) {
							if (fuente.getDocumento() != null) {
								for (TerminoTecnologiaEmergente terminoTE : terminosTecnologiasEmergentes) {
									if (fuente.getDocumento().getTexto().toLowerCase().contains(terminoTE.getTermino())) {
										Parser.parsear(fuente);
										AvisoTecnologiasEmergentes aviso = new AvisoTecnologiasEmergentes();
										aviso.setTermino(terminoTE.getTerminoPrincipal());
										aviso.setTipo("Documento Web");
										aviso.setTitulo(fuente.getTitulo());
										aviso.setExtracto(Parser.getExtractoTexto(fuente.getDocumento().getTexto(), terminoTE.getTermino()));
										if (aviso.getExtracto() == null)
											continue;
										aviso.setUrl(fuente.getUrl().toString());
										session.beginTransaction();
										session.save(aviso);
										session.getTransaction().commit();
										System.out.println("Aviso registrado");
									}
								}
							}
						}
					} else {
						String mensaje = "No actualizamos la fuente (" + fuente.getId() + "): " + fuente.getUrl().toString();
						mensaje += " por ser de tipo " + tipoFichero;
						log.trace(mensaje);
						fuente.setError(tipoFichero);
					}

					Host host = new Host(fuente.getUrl().getHost());

					if (host.getId() == null) { // Nuevo host
						session.beginTransaction();
						session.save(host);
						fuente.setHost(host);
						session.save(fuente);
						session.getTransaction().commit();
					} else {
						fuente.setHost(host);
						session.beginTransaction();
						// fuente.setProfundidad(null); // Provocar error aposta
						session.save(fuente);
						session.getTransaction().commit();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Error en el bucle principal al procesar fuente " + fuente.getId() + ": " + e.toString());
				if (session.getTransaction().isActive())
					session.getTransaction().rollback();
				session = Delphos.reconectarSesion();
				String sql = "UPDATE Fuente SET error = 'Crawler: " + e.toString() + "' WHERE id = " + fuente.getId();
				Query query = session.createSQLQuery(sql);
				session.beginTransaction();
				query.executeUpdate();
				session.getTransaction().commit();
			}
		}// while true

		// Código inalcanzable
		// Delphos.cerrarSesion();
		// log.trace("Terminando.");
	}

	public static ArrayList<TerminoTecnologiaEmergente> cargarTerminosTecnologiasEmergentes() {
		ArrayList<TerminoTecnologiaEmergente> resultado = new ArrayList<TerminoTecnologiaEmergente>();

		try (BufferedReader br = new BufferedReader(new FileReader(Delphos.FICHERO_TECNOLOGIAS_EMERGENTES))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				String[] aux = sCurrentLine.split(",");
				for (int i = 0; i < aux.length; i++)
					resultado.add(new TerminoTecnologiaEmergente(aux[0].trim().toLowerCase(), aux[i].trim().toLowerCase()));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultado;
	}

	private static Fuente obtenerFuente(int indice) {
		Criteria crit = session.createCriteria(Fuente.class);
		crit.add(Restrictions.isNull("documento"));
		crit.add(Restrictions.isNull("error"));
		crit.add(Restrictions.eq("noActualizar", false));
		crit.addOrder(Order.asc("id"));
		crit.setMaxResults(1);

		return (Fuente) crit.uniqueResult();
	}

	private static Fuente obtenerFuenteParaActualizar(int indice) {
		Criteria crit = session.createCriteria(Fuente.class);
		// crit.add(Restrictions.isNull("documento"));
		// crit.add(Restrictions.isNull("error"));
		// crit.add(Restrictions.eq("noActualizar", false));
		crit.add(Restrictions.gt("id", indice));
		crit.addOrder(Order.asc("id"));
		crit.setMaxResults(1);

		return (Fuente) crit.uniqueResult();
	}

}