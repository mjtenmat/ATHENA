package delphos;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Propiedades {
	private final static String NOMBRE_FICHERO = "delphos.properties";
	private static Properties prop = new Properties();
	private static InputStream input = null;
	private static boolean cargadas = false;

	private static void cargar() {
		try {
			input = new FileInputStream(NOMBRE_FICHERO);

			// load a properties file
			prop.load(input);

			cargadas = true;

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static String get(String nombre) {
		if (!cargadas) cargar();
		return prop.getProperty(nombre);
	}
}
