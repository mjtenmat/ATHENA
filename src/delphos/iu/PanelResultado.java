package delphos.iu;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import delphos.Fuente;
import delphos.Parser;
import delphos.Resultado;

public class PanelResultado extends JPanel {

	protected JCheckBoxResultadoRelevante relevante;
	protected JLabel lblTitulo;
	protected JLabel lblUrl;
	// protected JLabel lblDescripcion;
	protected JLabel tipoOrganizacion;
	protected JLabel sectores;
	protected JLabel localizacion;

	private Resultado resultado; // Referencia al resultado que se muestra en el
									// panel
	private PanelBusqueda panelBusqueda; // Referencia al panel superior
	private PanelBusquedaController controller;
	private JTextPane tpDescripcion;
	private JTextPane tpExtractoTexto;
	private JPanel panelRestricciones;
	private JLabel lblSector;
	private JLabel lblLocalizacin;
	private JLabel lblTipoDeOrganizacin;
	private JPanel panelSector;
	private JPanel panelLocalizacion;
	private JPanel panelTipoOrganizacion;
	private JPanel panel;

	public PanelResultado(Resultado resultado, PanelBusqueda panelBusqueda) {
		this.resultado = resultado;
		this.panelBusqueda = panelBusqueda;
		this.controller = this.panelBusqueda.controller;

		this.setBackground(Color.WHITE);
		this.addMouseListener(controller);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		panel = new JPanel();
		add(panel);
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);

		relevante = new JCheckBoxResultadoRelevante(this.resultado);
		panel.add(relevante);
		// relevante.setBackground(Color.LIGHT_GRAY);

		lblTitulo = new JLabel("Título del Documento");
		panel.add(lblTitulo);
		lblTitulo.setForeground(Color.BLUE);

		lblUrl = new JLabel("URL de la Fuente");
		add(lblUrl);
		lblUrl.setHorizontalAlignment(SwingConstants.LEFT);
		lblUrl.setForeground(new Color(0, 128, 0));
		lblUrl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		tpDescripcion = new JTextPane();
		tpDescripcion.setBackground(Color.WHITE);
		tpDescripcion.setFont(new Font("Dialog", Font.PLAIN, 10));
		tpDescripcion.setContentType("text/html");
		tpDescripcion.setEditable(false);
		tpDescripcion.setPreferredSize(new Dimension(100, 50));
		add(tpDescripcion);

		tpExtractoTexto = new JTextPane();
		tpExtractoTexto.setBackground(Color.WHITE);
		tpExtractoTexto.setFont(new Font("Dialog", Font.PLAIN, 10));
		tpExtractoTexto.setContentType("text/html");
		tpExtractoTexto.setEditable(false);
		tpExtractoTexto.setPreferredSize(new Dimension(100, 50));
		add(tpExtractoTexto);

		panelRestricciones = new JPanel();
		add(panelRestricciones);
		panelRestricciones.setLayout(new GridLayout(0, 3, 0, 0));
		panelRestricciones.setVisible(true);
		panelRestricciones.setPreferredSize(new Dimension(300, 10));

		panelSector = new JPanel();
		panelRestricciones.add(panelSector);
		panelSector.setLayout(new BoxLayout(panelSector, BoxLayout.X_AXIS));

		lblSector = new JLabel("Sector:");
		panelSector.add(lblSector);

		sectores = new JLabel("Sectores ");
		panelSector.add(sectores);
		sectores.setFont(new Font("Dialog", Font.PLAIN, 12));

		panelLocalizacion = new JPanel();
		panelRestricciones.add(panelLocalizacion);
		panelLocalizacion.setLayout(new BoxLayout(panelLocalizacion,
				BoxLayout.X_AXIS));

		lblLocalizacin = new JLabel("Localización:");
		panelLocalizacion.add(lblLocalizacin);

		localizacion = new JLabel("Localización");
		panelLocalizacion.add(localizacion);
		localizacion.setFont(new Font("Dialog", Font.PLAIN, 12));

		panelTipoOrganizacion = new JPanel();
		panelRestricciones.add(panelTipoOrganizacion);
		panelTipoOrganizacion.setLayout(new BoxLayout(panelTipoOrganizacion,
				BoxLayout.X_AXIS));

		lblTipoDeOrganizacin = new JLabel("Tipo de Organización: ");
		panelTipoOrganizacion.add(lblTipoDeOrganizacin);

		tipoOrganizacion = new JLabel("Tipo de Organización");
		panelTipoOrganizacion.add(tipoOrganizacion);
		tipoOrganizacion.setFont(new Font("Dialog", Font.PLAIN, 12));
		relevante.addActionListener(controller);

		this.cargarResultado();
	}

	public void setController() {

	}

	private void cargarResultado() {
		Fuente f = resultado.getFuente();
		this.setTitulo("[" + ((int)resultado.verCobertura()*100) + "% - "+ resultado.verSimilitud() + "] " + f.getTitulo());
		if (resultado.getFuente().getIdioma() != null)
			this.setTitulo(this.getTitulo() + " ("
					+ resultado.getFuente().getIdioma() + ")");
		this.setUrl(f.getUrl().toString());
		this.lblUrl.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					// open the default web browser for the HTML page
					Desktop desktop = Desktop.getDesktop();
					if ((desktop.isSupported(Desktop.Action.BROWSE))) {
						Desktop.getDesktop().browse(
								java.net.URI.create(((JLabel) e.getSource())
										.getText()));
					}
					else{
						ProcessBuilder pb = new ProcessBuilder("/usr/bin/firefox", ((JLabel) e.getSource()).getText());
						Process p = pb.start();
					}
				}
				catch (Exception exc) {
					System.out.println("Error abriendo página web. "
							+ exc.getMessage());
				}

			}
		});
		this.setDescripcion(this.destacarTerminosConsulta(f.getDescripcion()));
		String extractoTexto = Parser.getExtractoFuente(f,
				this.panelBusqueda.getConsulta());
		this.setExtractoTexto("<html>"
				+ this.destacarTerminosConsulta(extractoTexto) + "</html>");
		// this.setExtractoTexto("<html>"+Parser.getExtractoTexto(f,
		// this.panelBusqueda.getConsulta())+"</html>");
		// String extractoTextoDestacado =
		// this.destacarTerminosConsulta(this.getExtractoTexto());
		// if (extractoTextoDestacado.equals(this.getExtractoTexto()))
		// this.setExtractoTexto("<i>No hay coincidencias en el texto del documento</i>");
		// else
		// this.setExtractoTexto(extractoTextoDestacado);
		if (f.getHost().getLocalizacion() != null)
			this.setLocalizacion(f.getHost().getLocalizacion().getNombre());
		else
			this.setLocalizacion("No indicada");
		if (f.getHost().verSectores() != null)
			this.setSectores(f.getHost().verSectores());
		else
			this.setSectores("No indicado");
		if (f.getHost().getTipoOrganizacion() != null)
			this.setTipoOrganizacion(f.getHost().getTipoOrganizacion()
					.getNombre());
		else
			this.setTipoOrganizacion("No indicada");
		this.relevante.setSelected(this.resultado.isRelevante());
	}

	private String destacarTerminosConsulta(String texto) {
		if (texto != null)
			for (String palabra : this.panelBusqueda.getConsulta().split(" "))
				texto = texto.replaceAll("(?i)" + palabra.toLowerCase(), "<b>"
						+ palabra + "</b>");

		return texto;
	}

	public String getTitulo() {
		return this.lblTitulo.getText();
	}

	public void setTitulo(String titulo) {
		this.lblTitulo.setText(titulo);
	}

	public String getUrl() {
		return this.lblUrl.getText();
	}

	public void setUrl(String url) {
		this.lblUrl.setText(url);
	}

	public String getDescripcion() {
		return this.tpDescripcion.getText();
	}

	public void setDescripcion(String descripcion) {
		this.tpDescripcion.setText("<b>Descripción:</b> " + descripcion);
	}

	public String getTipoOrganizacion() {
		return this.tipoOrganizacion.getText();
	}

	public void setTipoOrganizacion(String tipo) {
		this.tipoOrganizacion.setText(tipo);
	}

	public String getSector() {
		return this.sectores.getText();
	}

	public void setSectores(String sectores) {
		this.sectores.setText(sectores);
	}

	public String getLocalizacion() {
		return this.localizacion.getText();
	}

	public void setLocalizacion(String localizacion) {
		this.localizacion.setText(localizacion);
	}

	public void setExtractoTexto(String extractoTexto) {
		this.tpExtractoTexto.setText(extractoTexto);
	}

	public String getExtractoTexto() {
		return this.tpExtractoTexto.getText();
	}

	public Resultado getResultado() {
		return this.resultado;
	}

	public void marcarNoRelevante() {
		// this.setBackground(Color.RED);
		lblTitulo.setForeground(Color.RED);
		// relevante.setSelected(false);
		// relevante.setEnabled(false);
	}
}
