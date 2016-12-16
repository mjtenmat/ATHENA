/**
 * Weigher.java - Recalcula los pesos de los descriptores
 *
 * @version 0.1
 * @author María José Tena
 */

package delphos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import delphos.iu.Delphos;

public class CopyOfWeigher {
	private static String hibernateCfgFile = "hibernate.cfg.xml";
	private static final int NUM_PESOS_CONSULTA = 10000;
	private static Session session;
	private static long N = 0; // Número de Fuentes en la BBDD que han sido
								// parseadas
	private static Logger log;
	private static int contador = 0;
	private static List listaDescriptorCoincidencias;
	private static TreeMap<Integer, BigInteger> rbTree;

	public static void main(String[] args) {
		BasicConfigurator.configure();
		log = Logger.getLogger(CopyOfWeigher.class);
		log.info("Arrancando.");

		Delphos.setHibernateCfgFile(hibernateCfgFile);
		session = Delphos.getSession();

		String sql1 = "SELECT Descriptor.id, count(*) ";
		sql1 += "FROM Descriptor JOIN Peso on Descriptor.id = Peso.idDescriptor ";
		sql1 += "GROUP BY Descriptor.id ";
		sql1 += "ORDER BY Descriptor.id ";
		Query query1 = session.createSQLQuery(sql1);
		listaDescriptorCoincidencias = query1.list();

		File fichTemp = new File("Peso.tmp");
		fichTemp.delete(); // Si existe, lo borramos
		PrintWriter writer;

		// Probamos a recorrerla lista
		// Iterator it = listaDescriptorCoincidencias.iterator();
		// while (it.hasNext()){
		// Object[] row = (Object[])it.next();
		// System.out.println("Identificador: " + row[0]);
		// System.out.println("Texto: " + row[1]);
		// System.out.println("Ocurrencias: " + row[2]);
		// }

		// Construimos el Red-Black Tree - Imprescindible para no tardar una
		// eternidad.
		rbTree = new TreeMap<Integer, BigInteger>();
		Iterator it = listaDescriptorCoincidencias.iterator();
		while (it.hasNext()) {
			Object[] row = (Object[]) it.next();
			rbTree.put((Integer) row[0], (BigInteger) row[1]);
		}

		int indice = 0;
		Integer maxId = 0;
		try {
			Query query = session.createSQLQuery("SELECT MAX(id) FROM Peso");
			maxId = (int) query.uniqueResult();
		} catch (Exception e) {
			log.error("Error en la ejecución de Weigher. La tabla de Peso puede estar vacía");
			e.printStackTrace();
		}
		long inicio = System.currentTimeMillis();

		N = contarFuentes(); // Actualiza el Número total de documentos en la
								// Base de Datos
		List<Peso> listaVectores;
		String textoTemporal;
		while (indice < maxId) {
			listaVectores = obtenerListaPesos(indice);
			if (listaVectores == null)
				break;
			int maxIdLista = 0;
			textoTemporal = "";
			long inicioBloque = System.currentTimeMillis();
			for (Peso peso : listaVectores) {
				peso.setPeso(calcularPeso(peso));
				textoTemporal += peso.toString() + "\n";
				contador++;
				if (contador % NUM_PESOS_CONSULTA == 0) {
					System.out.print("Pesados: " + contador + " descriptores.");
					long ahora = System.currentTimeMillis();
					System.out.print(" Tiempo Bloque: "
							+ (ahora - inicioBloque) + " ms.");
					inicioBloque = System.currentTimeMillis();
				}

				if (maxIdLista < peso.getId())
					maxIdLista = peso.getId();
			}
			
			System.out.print("Escribiendo fichero temporal....");
			writer = abrirFicheroTemporal(fichTemp);
			writer.append(textoTemporal);
			writer.close();
			System.out.println("Hecho.");

			System.out.print("Pesados: " + contador + " descriptores. ");
			System.out.println("T. Bloque: "	+ (System.currentTimeMillis() - inicioBloque) + " ms.");
			
			indice = maxIdLista + 1;
			// Reconectamos la sesión cada NUM_PESOS pesos procesados
			// para evitar OutOfMemory
			//session = Delphos.reconectarSesion(); 
		}
		
		System.out.println("No quedan vectores por pesar");
		System.out.print("Truncando tabla...");
		Delphos.getSession().createSQLQuery("truncate table Peso")
				.executeUpdate();
		System.out.println("Hecho.");
		// Lanzar el comando de systema para SQL LOAD

		// mysqlimport -u root -p --local delphos2 Peso.tmp
		
		String comando = "mysqlimport -u root --password=root --local delphos2 "
				+ System.getProperty("user.dir") + "/Peso.tmp";
		System.out.println(comando);
		System.out.println("Importando tabla...");
		try {
			String s;
			Process p = Runtime.getRuntime().exec(comando);
			System.out.println("Hecho.");
//			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
//					p.getInputStream()));
//
//			BufferedReader stdError = new BufferedReader(new InputStreamReader(
//					p.getErrorStream()));
//
//			// read the output from the command
//			System.out.println("Here is the standard output of the command:\n");
//			while ((s = stdInput.readLine()) != null) {
//				System.out.println(s);
//			}
//
//			// read any errors from the attempted command
//			System.out
//					.println("Here is the standard error of the command (if any):\n");
//			while ((s = stdError.readLine()) != null) {
//				System.out.println(s);
//			}
		} catch (IOException e) {
			log.error("Ha fallado el comando mysqlimport.");
			e.printStackTrace();
		}

		// Código inalcanzable
		Delphos.cerrarSesion();
		log.info("Terminando.");
	}

	private static PrintWriter abrirFicheroTemporal(File fichero) {
		PrintWriter writer = null;
		
		try {
			writer = new PrintWriter(new FileOutputStream(fichero, true));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return writer;
	}

	public static long contarFuentes() {
		Criteria crit = session.createCriteria(Fuente.class);
		crit.add(Restrictions.and(Restrictions.isNotNull("documento"),
				Restrictions.eq("parseada", true)));
		return (Long) crit.setProjection(Projections.rowCount()).uniqueResult();
	}

	public static List<Peso> obtenerListaPesos(int id) {
		try {
			Criteria crit = session.createCriteria(Peso.class);
			crit.add(Restrictions.gt("id", id));
			crit.setMaxResults(NUM_PESOS_CONSULTA); // Si no se limita la
													// consulta, se revienta la
													// memoria
			return crit.list();
		} catch (Exception e) {
			log.error("Error al obtener lista de pesos: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static double calcularPeso(Peso peso) {
		double valorPeso;

		// if (session == null) {
		// Delphos.setHibernateCfgFile(hibernateCfgFile);
		// session = Delphos.getSession();
		// }

		// Consultamos el número de documentos en la BD que hacen referencia al
		// mismo descriptor (nj)
		// Con una lista simple (tiempo de bloque 172ms)
		// int nj = 0;
		// Iterator it = listaDescriptorCoincidencias.iterator();
		// while (it.hasNext()){
		// Object[] descriptorCoincidencias = (Object[]) it.next();
		// //System.out.println("Comparando " + (int)descriptorCoincidencias[0]
		// + " y " + peso.getDescriptor().getId());
		// if ((int)descriptorCoincidencias[0] == peso.getDescriptor().getId())
		// nj = ((BigInteger)descriptorCoincidencias[1]).intValue();
		// }
		// Con un TreeMap
		// Tiempo de bloque con caché 1ms. Sin caché 56ms
		int nj = ((BigInteger) rbTree.get(peso.getDescriptor().getId()))
				.intValue();
		// Consultando en BD
		// Tiempo de bloque con caché 1ms. Sin caché 56ms
		// Query query = session
		// .createSQLQuery("SELECT COUNT(*) FROM Peso WHERE idDescriptor = :idDescriptor");
		// query.setParameter("idDescriptor", peso.getDescriptor().getId());
		// int nj = ((BigInteger) query.uniqueResult()).intValue();

		// N = contarFuentes();

		if (nj == 0) {
			log.error("Error al calcular nj");
			return 0;
		}
		valorPeso = peso.getFrecuencia() * Math.log(((double) N) / nj)
				/ Math.log(2);

		return valorPeso;
	}

}
