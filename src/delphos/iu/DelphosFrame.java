package delphos.iu;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import org.apache.log4j.BasicConfigurator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delphos.Tendencia;

public class DelphosFrame {
	private static Session session;
	private static SessionFactory sessionFactory;
	// private final static Logger log =
	// LogManager.getLogger(DelphosFrame.class);
	private static Logger log;

	protected JFrame frame; // El frame principal
	protected PanelBusqueda panelBusqueda;
	protected PanelVigilancia panelVigilancia;
	protected PanelVigilanciaResultados panelVigilanciaResultados;
	protected PanelBuscarHosts panelHosts;
	protected PanelAltaFuente panelAltaFuente;
	protected PanelEdicionTecnologiasEmergentes panelEdicionTecnologiasEmergentes;
	protected PanelAvisosTecnologia panelAvisosTecnologia;
	protected PanelAvisosTendencia panelAvisosTendencia;
	protected PanelTendencias panelTendencias;
	protected JMenuBar menuBar;
	JMenuItem miBuscador;
	JMenuItem miVigilancia;
	JMenuItem miTendencias;
	JMenuItem miTecnologiasEmergentes;
	protected JMenuItem miFuentesAlta;
	protected JMenuItem miHosts;

	protected DelphosFrameController controller;

	public DelphosFrame() {
		BasicConfigurator.configure();
		log = LoggerFactory.getLogger(DelphosFrame.class);
		log.trace("Arrancando.");

		session = Delphos.getSession();
		
		controller = new DelphosFrameController(this);
		crearPaneles();
		crearMenuPrincipal();

		frame.getRootPane().setDefaultButton(panelBusqueda.getDefaultButton());

		frame.toFront();
	}

	protected void crearPaneles() {
		frame = new JFrame();

		panelBusqueda = new PanelBusqueda(this);
		frame.add(panelBusqueda);

		panelVigilancia = new PanelVigilancia(this);
		panelVigilancia.setVisible(false);
		frame.add(panelVigilancia);
		
		panelVigilanciaResultados = new PanelVigilanciaResultados(this);
		panelVigilanciaResultados.setVisible(false);
		frame.add(panelVigilanciaResultados);

		panelAltaFuente = new PanelAltaFuente(this);
		panelAltaFuente.setVisible(false);
		frame.add(panelAltaFuente);

		panelHosts = new PanelBuscarHosts(this);
		panelHosts.setVisible(false);
		frame.add(panelHosts);

		panelEdicionTecnologiasEmergentes = new PanelEdicionTecnologiasEmergentes(this);
		panelEdicionTecnologiasEmergentes.setVisible(false);
		frame.add(panelEdicionTecnologiasEmergentes);
		
		panelAvisosTecnologia = new PanelAvisosTecnologia(this);
		panelAvisosTecnologia.setVisible(false);
		frame.add(panelAvisosTecnologia);
		
		panelAvisosTendencia = new PanelAvisosTendencia(this);
		panelAvisosTendencia.setVisible(false);
		frame.add(panelAvisosTendencia);
		
		panelTendencias = new PanelTendencias(this);
		panelTendencias.setVisible(false);
		frame.add(panelTendencias);
		
		frame.setTitle("Buscador Delphos");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				exitProcedure();
			}
		});
		frame.setLayout(new CardLayout());

		// frame.add(panelLoquesea.getPanel());
		frame.pack();
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);

		controller.verPanel(panelBusqueda);
	}

	protected void crearMenuPrincipal() {
		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		// Menú principal - Delphos
		JMenu mnDelphos = new JMenu("Delphos");
		mnDelphos.setMnemonic(KeyEvent.VK_D);
		mnDelphos.getAccessibleContext().setAccessibleDescription(
				"Operaciones principales del programa Delphos");
		menuBar.add(mnDelphos);

		this.miBuscador = new JMenuItem("Buscador", KeyEvent.VK_B);
		this.miBuscador.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				ActionEvent.ALT_MASK));
		this.miBuscador.getAccessibleContext().setAccessibleDescription(
				"Buscador de Fuentes (ALT+B)");
		this.miBuscador.addActionListener(controller);
		mnDelphos.add(this.miBuscador);

		this.miVigilancia = new JMenuItem("Vigilancia", KeyEvent.VK_V);
		this.miVigilancia.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.ALT_MASK));
		this.miVigilancia.getAccessibleContext().setAccessibleDescription(
				"Buscador de Licitaciones (ALT+L)");
		this.miVigilancia.addActionListener(controller);
		mnDelphos.add(this.miVigilancia);

		this.miTendencias = new JMenuItem("Tendencias", KeyEvent.VK_T);
		this.miTendencias.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				ActionEvent.ALT_MASK));
		this.miTendencias.getAccessibleContext().setAccessibleDescription(
				"Análisis de Tendencias (ALT+T)");
		this.miTendencias.addActionListener(controller);
		mnDelphos.add(this.miTendencias);
//		
//		JMenuItem miTecnologiasEmergentes = new JMenuItem("Tecnologías Emergente", KeyEvent.VK_E);
//		miTecnologiasEmergentes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
//				ActionEvent.ALT_MASK));
//		miTecnologiasEmergentes.getAccessibleContext().setAccessibleDescription(
//				"Editor de Tecnologías Emergentes (ALT+E)");
//		mnDelphos.add(mTendencias);
		
		mnDelphos.add(new JSeparator());

		JMenuItem miSalir = new JMenuItem("Salir", KeyEvent.VK_Q);
		miSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				ActionEvent.ALT_MASK));
		miSalir.getAccessibleContext().setAccessibleDescription(
				"Finalizar la Aplicación (ALT+Q)");
		mnDelphos.add(miSalir);

		// Menú de Gestión
		JMenu mnGestion = new JMenu("Gestión");
		mnGestion.setMnemonic(KeyEvent.VK_G);
		mnGestion.getAccessibleContext().setAccessibleDescription(
				"Operaciones de Gestión");
		menuBar.add(mnGestion);

		JMenu mnFuentes = new JMenu("Fuentes");
		mnFuentes.setMnemonic(KeyEvent.VK_F);
		mnFuentes.getAccessibleContext().setAccessibleDescription(
				"Gestión de Fuentes (ALT+F)");
		mnFuentes.setEnabled(true);
		miFuentesAlta = new JMenuItem("Alta", KeyEvent.VK_F);
		miFuentesAlta.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.ALT_MASK));
		miFuentesAlta.getAccessibleContext().setAccessibleDescription(
				"Alta de Fuente (ALT+A)");
		miFuentesAlta.addActionListener(controller);
		miFuentesAlta.setEnabled(true);
		mnFuentes.add(miFuentesAlta);
		mnGestion.add(mnFuentes);

		miHosts = new JMenuItem("Hosts", KeyEvent.VK_H);
		miHosts.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
				ActionEvent.ALT_MASK));
		miHosts.getAccessibleContext().setAccessibleDescription(
				"Gestión de Hosts (ALT+H)");
		miHosts.addActionListener(controller);
		// miHosts.setEnabled(false);
		mnGestion.add(miHosts);

		mnGestion.add(new JSeparator());

		JMenuItem miLocalizaciones = new JMenuItem("Localizaciones",
				KeyEvent.VK_L);
		miLocalizaciones.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.ALT_MASK));
		miLocalizaciones.getAccessibleContext().setAccessibleDescription(
				"Gestión de Localizaciones (ALT+L)");
		miLocalizaciones.setEnabled(false);
		mnGestion.add(miLocalizaciones);

		JMenuItem miSectores = new JMenuItem("Sectores", KeyEvent.VK_S);
		miSectores.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.ALT_MASK));
		miSectores.getAccessibleContext().setAccessibleDescription(
				"Gestión de Sectores (ALT+S)");
		miSectores.setEnabled(false);
		mnGestion.add(miSectores);

		JMenuItem miTiposOrganizacion = new JMenuItem("Tipos de Organización",
				KeyEvent.VK_T);
		miTiposOrganizacion.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_T, ActionEvent.ALT_MASK));
		miTiposOrganizacion.getAccessibleContext().setAccessibleDescription(
				"Gestión de Tipos de Organización (ALT+T)");
		miTiposOrganizacion.setEnabled(false);
		mnGestion.add(miTiposOrganizacion);

		mnGestion.add(new JSeparator());

		JMenuItem miErrores = new JMenuItem("Errores", KeyEvent.VK_E);
		miErrores.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				ActionEvent.ALT_MASK));
		miErrores.getAccessibleContext().setAccessibleDescription(
				"Gestión de Errores del Programa (ALT+E)");
		miErrores.setEnabled(false);
		mnGestion.add(miErrores);
	}

	public void exitProcedure() {
		frame.dispose();
		Delphos.cerrarSesion();
		System.exit(0);
	}

	public JFrame getFrame() {
		return frame;
	}

}
