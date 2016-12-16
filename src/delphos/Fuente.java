/**
 * Fuente.java
 * Fuente de documentos para el Crawler.
 *
 * @version 0.1
 * @author María José Tena
 */
package delphos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.management.Query;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import delphos.iu.Delphos;


@Entity
public class Fuente {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id = 0;
	private URL url;
	private Date fechaActualizacion;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn(name = "idHost", referencedColumnName = "id")
	private Host host;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn(name = "idPadre", referencedColumnName = "id")
	private Fuente fuentePadre;
	@OneToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "idDocumento", referencedColumnName = "id")
	private Documento documento;
	private String titulo;
	private String descripcion;
	private String idioma;
	private String error;
	private Integer profundidad = 0;
	private boolean parseada;
	@OneToMany(mappedBy = "fuente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<Peso> pesos = new HashSet<Peso>();
	private boolean noActualizar = false;
	@Transient
	private final static Logger log = Logger.getLogger(Fuente.class);

	public boolean actualizar() throws Exception {
		String oldDoc = "";
		if (this.getDocumento() != null)
			oldDoc = this.getDocumento().getTexto();
		
		HttpURLConnection httpCon = (HttpURLConnection) this.url.openConnection();

		// Establecemos las cabeceras
		httpCon.setReadTimeout(5000);
		httpCon.setConnectTimeout(5000);
		httpCon.setRequestProperty("User-Agent", "Mozilla");
		httpCon.setRequestProperty("Accept-Language", "es, en");

		Date fechaActualizacionServidor = new Date(httpCon.getLastModified());

		// this.idioma = httpCon.getHeaderField("Content-language"); //Quitado, llegan datos sin formato específico.

		// log.trace(httpCon.getResponseCode());

		if (this.fechaActualizacion != null)
			if (!fechaActualizacionServidor.after(this.fechaActualizacion) && fechaActualizacionServidor.getTime() != 0)
				return false; // No es necesario actualizar el documento

		// Actualizamos el documento
		log.info("Actualizando Documento de Fuente (" + this.id + ")");
		InputStream is;
		InputStreamReader isr;
		BufferedReader br;
		try {
			is = httpCon.getInputStream();
			isr = new InputStreamReader(is,"UTF-8");
			br = new BufferedReader(isr);

			String linea;
			if (this.documento == null)
				this.documento = new Documento();
			this.documento.setTexto("");
			while ((linea = br.readLine()) != null)
				this.documento.setTexto(this.documento.getTexto() + asegurarUTF8(linea));
			
			if (this.documento.getTexto().equals(oldDoc))	//No ha cambiado el documento
				return false;
			
			// Cerramos
			//this.fechaActualizacion = fechaActualizacionServidor;
			this.fechaActualizacion = new Date();
			this.parseada = false;	//Hacemos que se vuelva a parsear
			this.error = null;
			br.close();
			isr.close();
			return true;
		} catch (IOException e) {
			String mensaje;
			mensaje = "Error en la Fuente " + this.id;
			mensaje += " = " + this.url.toString();
			mensaje += "\nDescripción de la Excepción: " + e.toString();

			int errorCode;
			if (e.getClass().getName() == "java.net.UnknownHostException")
				errorCode = 404;
			else {
				try {
					errorCode = httpCon.getResponseCode();
				} catch (Exception e1) {
					errorCode = 99;
				}
			}

			mensaje += "\nCódigo del Error HTTP: " + errorCode;
			this.error = Integer.toString(errorCode);
			// is = httpCon.getErrorStream(); //Si queremos leer la página de
			// error

			log.trace(mensaje);
			return false;
		}
	}
	public void anadirDescriptor(String textoDescriptor, double multiplicador, boolean enBody) {
		// Vemos si el descriptor está ya en el vector
		log.trace("Añadiendo Descriptor: " + textoDescriptor);
		for (Peso peso : this.pesos) {
			if (peso.getTextoDescriptor().equals(textoDescriptor)) {
				log.trace("El Descriptor " + textoDescriptor + " ya está registrado. Vamos al siguiente.");
				peso.setFrecuencia(peso.getFrecuencia() + 1);
				peso.setMultiplicador(peso.getMultiplicador() * multiplicador);
				peso.setEnBody(enBody);
				return;
			}
		}

		// No está, lo añadimos
		log.trace("El Descriptor " + textoDescriptor + " no está registrado. Lo incluimos.");
		Peso nuevoDescriptor = new Peso();
		nuevoDescriptor.setFuente(this);
		nuevoDescriptor.setDescriptor(textoDescriptor);
		nuevoDescriptor.setFrecuencia(1);
		nuevoDescriptor.setMultiplicador(multiplicador);
		this.pesos.add(nuevoDescriptor);
	}
	public void borrarVector() {
		this.pesos.removeAll(this.pesos);
	}

	// Getters y Setters
	
	public Integer getId() {
		return id;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean getParseada() {
		return parseada;
	}

	public void setParseada(boolean parseada) {
		this.parseada = parseada;
	}

	public Fuente getFuentePadre() {
		return fuentePadre;
	}

	public void setFuentePadre(Fuente fuentePadre) {
		this.fuentePadre = fuentePadre;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public int getProfundidad() {
		return profundidad;
	}

	public void setProfundidad(Integer profundidad) {
		this.profundidad = profundidad;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		if (descripcion.length() > 1024)
			this.descripcion = descripcion.substring(0, 1023);
		else
			this.descripcion = descripcion;
	}

	public String getIdioma() {
		return idioma;
	}

	public void setIdioma(String idioma) {
		this.idioma = idioma;
	}

	public Set<Peso> getPesos() {
		return pesos;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public boolean isNoActualizar() {
		return noActualizar;
	}
	public void setNoActualizar(boolean noActualizar) {
		this.noActualizar = noActualizar;
	}

	public static String asegurarUTF8(String textoPeligroso){
		//System.out.println(textoPeligroso);
		
		//ISO-8859-1
//		CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();
//		CharsetDecoder decoder = Charset.forName("ISO-8859-1").newDecoder();
//		
//		try{
//			ByteBuffer tmp = encoder.encode(CharBuffer.wrap(textoPeligroso));
//			//System.out.println(decoder.decode(tmp).toString());
//		}
//		catch(Exception ex){
//			ex.printStackTrace();
//		}
		
		//ByteBuffer bb = new ByteBuffer(textoPeligroso.getBytes());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < textoPeligroso.length(); i++) {
		    char ch = textoPeligroso.charAt(i);
		    if (!Character.isHighSurrogate(ch) && !Character.isLowSurrogate(ch)) {
		        sb.append(ch);
		    }
		}
		return sb.toString();
	}
	/**
	 * Guarda la Frecuencia de los Descriptores de la Fuente en la tabla Fuente_Frecuencia_Descriptor
	 */
	public void guardarFrecuencia() {
		String sql = "INSERT INTO Fuente_Descriptor_Frecuencia "
				+ "(idFuente, idDescriptor, frecuencia, fecha) "
				+ "SELECT Peso.idFuente, Peso.idDescriptor, frecuencia, CURDATE() "
				+ "FROM Peso WHERE idFuente = " + this.getId();
		Delphos.getSession().createSQLQuery(sql).executeUpdate();
	}
}
