/**
 * Weigher.java - Recalcula los pesos de los descriptores
 *
 * @version 0.1
 * @author María José Tena
 */

package delphos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

public class Weigher {
	private static String hibernateCfgFile = "hibernate.cfg.xml";
	private static final int NUM_PESOS_CONSULTA = 10000;
	private static Session session;
	private static long N = 0; // Número de Fuentes parseadas de la BBDD
	private static Logger log;
	private static int contador = 0;
	private static List listaDescriptorCoincidencias;
	private static TreeMap<Integer, Integer> rbTree;

	public static void main(String[] args) {

		BasicConfigurator.configure();
		log = Logger.getLogger(Weigher.class);
		log.info("Arrancando.");
		
		//Creando copia de seguridad de la tabla Pesos
		String comando1 = "mysqldump -u root --password=root " + Propiedades.get("dbName") + " Peso";
				
				System.out.println(comando1);
				System.out.println("Haciendo copia de seguridad de la tabla de Pesos...");
				BufferedWriter bw = null;
				try {
			        File file = new File(System.getProperty("user.dir") + "/Peso.bak");
					file.createNewFile();
					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					bw = new BufferedWriter(fw);

					Process p = Runtime.getRuntime().exec(comando1);
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			        String line;
			        while ((line = br.readLine()) != null) 
			        		bw.append(line);
			       
			     }
				catch (IOException e) {
					log.error("Ha fallado el comando mysqldump.");
					e.printStackTrace();
					System.exit(-1);
				}
				finally{
					try {
						bw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

		Delphos.setHibernateCfgFile(hibernateCfgFile);
		session = Delphos.getSession();

		String sql1 = "SELECT Descriptor.id, count(*) ";
		sql1 += "FROM Descriptor JOIN Peso on Descriptor.id = Peso.idDescriptor ";
		sql1 += "GROUP BY Descriptor.id ";
		sql1 += "ORDER BY Descriptor.id ";
		Query query1 = session.createSQLQuery(sql1);
		listaDescriptorCoincidencias = query1.list();

		// Construimos el Red-Black Tree - 
		//Imprescindible para no tardar una eternidad.
		rbTree = new TreeMap<Integer, Integer>();	//Descriptor.id, count(*)
		Iterator it = listaDescriptorCoincidencias.iterator();
		while (it.hasNext()) {
			Object[] row = (Object[]) it.next();
			if (row[1] instanceof BigInteger)
				rbTree.put((Integer) row[0], ((BigInteger)row[1]).intValue());
			else
				rbTree.put((Integer) row[0], (Integer)row[1]);
		}
		
		File fichTemp = new File("Peso.tmp");
		fichTemp.delete(); // Si existe, lo borramos
		PrintWriter writer;

		N = contarFuentesConDocumento(); 
		
		List<Peso> listaVectores;
		String textoTemporal;
		int maxIdLista = 0;
		int indice = 0;

		List listaPesos = obtenerListaPesos(indice); 
		while (listaPesos.size() > 0){
			System.out.println("Procesando pesos de " + indice + " a " + (indice + NUM_PESOS_CONSULTA));
			textoTemporal = "";
			Iterator it2 = listaPesos.iterator();
			while (it2.hasNext()) {
				Object[] row = (Object[]) it2.next();
				double peso = calcularPeso(row);
				textoTemporal += row[0] + "\t" + row[1] + "\t" + row[2] + "\t" + row[3] + "\t" + peso + "\t" + row[4] + "\t" + row[5] + "\n";
				if (maxIdLista < (int)row[0])
					maxIdLista = (int)row[0];
			}
			//System.out.print("Escribiendo fichero temporal....");
			writer = abrirFicheroTemporal(fichTemp);
			writer.append(textoTemporal);
			writer.close();

			indice = maxIdLista; //La búsqueda de pesos es con gt
			listaPesos = obtenerListaPesos(indice);
		}
		
		System.out.println("No quedan vectores por pesar");
		//TODO: Copia de Seguridad de la tabla Peso
//		System.out.print("Truncando tabla...");
//		Delphos.getSession().createSQLQuery("truncate table Peso")
//				.executeUpdate();
		
		String comando = "mysqlimport -u root --password=root --local --delete " + Propiedades.get("dbName") + " "
				+ System.getProperty("user.dir") + "/Peso.tmp";
		System.out.println(comando);
		System.out.println("Importando tabla...");
		try {
			String s;
			Process p = Runtime.getRuntime().exec(comando);
		} catch (IOException e) {
			log.error("Ha fallado el comando mysqlimport.");
			e.printStackTrace();
		}
				
		//Fin 
		Delphos.cerrarSesion();
		System.out.println("Terminando.\n\nPor favor, reinicie la base de datos.\n");
		System.out.println("sudo service mysql restart\n\n");
		
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

	public static Integer contarFuentesPesadas() {
		String sql = "SELECT COUNT(DISTINCT(idFuente)) FROM Peso";
		Query query = Delphos.getSession().createSQLQuery(sql);
		return ((BigInteger)query.uniqueResult()).intValue();
//		Criteria crit = session.createCriteria(Fuente.class);
//		crit.add(Restrictions.eq("pesada", true));
//		return (Long) crit.setProjection(Projections.rowCount()).uniqueResult();
	}
	
	public static long contarFuentesConDocumento() {
		Criteria crit = session.createCriteria(Fuente.class);
		crit.add(Restrictions.and(Restrictions.isNotNull("documento")));
		return (Long) crit.setProjection(Projections.rowCount()).uniqueResult();
	}

	public static List obtenerListaPesos(int id) {
		long t1 = System.currentTimeMillis();
		//List<Peso> resultado;
		List resultado;
		//System.out.print("Obteniendo pesos con id > " + id + "...");
		try {
			String sql = "SELECT id, frecuencia, idFuente, multiplicador, idDescriptor, enBody ";
			sql += " FROM Peso ";
			sql += " WHERE id > " + id;
			//TODO: En SQL SERVER es @@ROWCOUNT
			sql += " LIMIT " + NUM_PESOS_CONSULTA;
			Query query = session.createSQLQuery(sql);
			resultado = query.list();
			
			//Criteria crit = session.createCriteria(Peso.class);
			//crit.add(Restrictions.gt("id", id));
			// Si no se limita la consulta, se revienta la memoria
			//crit.setMaxResults(NUM_PESOS_CONSULTA);
			//resultado = crit.list();
		} catch (Exception e) {
			log.error("Error al obtener lista de pesos: " + e.getMessage());
			e.printStackTrace();
			resultado = null;
		}
		return resultado;
	}
	
	public static double calcularPeso(Object[] row) {
		double valorPeso;
		int nj = 0;
		try{
			nj = rbTree.get(row[4]).intValue();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println((row[4]));
			System.exit(0);
		}
//		BigInteger tmp = (BigInteger) rbTree.get(row[4]); 
//		int nj = tmp.intValue();

		if (nj == 0) {
			log.error("Error al calcular nj");
			return 0;
		}
		valorPeso = (int)row[1] * Math.log(((double) N) / nj)
				/ Math.log(2);

		return valorPeso;
	}

	public static double calcularPeso(Peso peso) {
		double valorPeso;

		if (session == null) {
			Delphos.setHibernateCfgFile(hibernateCfgFile);
			session = Delphos.getSession();
		}

		 Query query = session
		 .createSQLQuery("SELECT COUNT(*) FROM Peso WHERE idDescriptor = :idDescriptor");
		 query.setParameter("idDescriptor", peso.getDescriptor().getId());
		 int nj = ((BigInteger) query.uniqueResult()).intValue();

		 N = contarFuentesPesadas();

		if (nj == 0) {
			System.out.println("Error al calcular nj");
			return 0;
		}
		
		valorPeso = peso.getFrecuencia() * Math.log(((double) N) / nj)
				/ Math.log(2);

		return valorPeso;
	}

}
