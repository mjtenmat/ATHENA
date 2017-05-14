package delphos.iu;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Timer;

import javax.swing.SwingUtilities;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import delphos.Propiedades;
import delphos.Searcher;
import delphos.VigilanteTecnologico;
import delphos.VigilanteTendencias;

public class Delphos implements Runnable {
	private static String hibernateCfgFile = "hibernate.cfg.xml";
	public final static String FICHERO_TECNOLOGIAS_EMERGENTES = "terminos_tecnologias_emergentes.txt";
	//private static String hibernateCfgFile = "hibernate_test.cfg.xml";
	private static Configuration configuration;
	private static StandardServiceRegistryBuilder ssrb;
	private static Session session = null; 
	private static SessionFactory sessionFactory;
	private static Timer timer;

	@Override
	public void run() {
		//Lanzamos el vigilante tecnológico
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 30);
		timer = new Timer();
		timer.schedule(new VigilanteTecnologico(), cal.getTime(), 24*60*60*1000);
		
		//Lanzamos el vigilante tecnológico
		timer.schedule(new VigilanteTendencias(), cal.getTime(), 24*60*60*1000);
		
		//Lanzamos el interfaz gráfico
		new DelphosFrame();
	}

	public static void main(String[] args) throws ParseException{
//		String textoFecha = "June 2012";
//		SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.US);
//		System.out.println("Sale " + sdf.parse(textoFecha));
		SwingUtilities.invokeLater(new Delphos());
	}

	public static String getHibernateCfgFile() {
		return hibernateCfgFile;
	}

	public static void setHibernateCfgFile(String hibernateCfgFile) {
		Delphos.hibernateCfgFile = hibernateCfgFile;
	}

	public static Session getSession() {
		if (session == null)
			return crearSesion();
		if (!session.isOpen())
			return crearSesion();
		
		return session;
	}
	
	public static void cerrarSesion(){
		//session.flush();
		//session.clear();
		session.close();
		sessionFactory.close();
	}
	
	public static Session reconectarSesion(){
		cerrarSesion();
		return crearSesion();
	}
	
	private static Session crearSesion(){
		configuration = new Configuration();
		configuration.configure(hibernateCfgFile);
		String params = "?useUnicode=true&amp;characterEncoding=utf-8";
		configuration.setProperty("hibernate.connection.url", "jdbc:mysql://" + Propiedades.get("dbHost") + "/" + Propiedades.get("dbName") + params);
		configuration.setProperty("hibernate.connection.username", Propiedades.get("dbUser"));
		configuration.setProperty("hibernate.connection.password", Propiedades.get("dbPass") );
		configuration.setProperty("hibernate.connection.CharSet", "utf8");
		configuration.setProperty("hibernate.connection.characterEncoding", "utf8");
		configuration.setProperty("hibernate.connection.useUnicode", "true");
		//configuration.setProperty("hibernate.connection.autocommit", "true");
		ssrb = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
		sessionFactory = configuration.buildSessionFactory(ssrb.build());
		session = sessionFactory.openSession();
		return session;
	}
	
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
}
