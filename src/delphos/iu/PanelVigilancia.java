package delphos.iu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.hibernate.Query;
import org.jdatepicker.JDateComponentFactory;
import org.jdatepicker.JDatePicker;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import delphos.CPI;
import delphos.ContrastarCon;
import delphos.Documento_Clasificacion;
import delphos.Licitacion_Localizacion;
import delphos.Licitacion_Sector;
import delphos.Localizacion;
import delphos.Patente_Localizacion;
import delphos.Patente_Sector;
import delphos.Sector;
import delphos.TipoOrganizacion;

public class PanelVigilancia extends JPanel implements ConDefaultButton{
	
	public static final String ULTIMAS_24_HORAS = "Últimas 24 horas";
	public static final String ULTIMO_MES = "Último mes";
	public static final String ULTIMA_SEMANA = "Última semana";
	protected DelphosFrame framePrincipal;
	protected PanelVigilanciaController controller;
	
	protected JButton btnEditarListadoTecnologiasEmergentes;
	protected JLabel lblAvisosPendientes;
	protected JButton btnRevisarAvisos;
	
	JCheckBox chckbxLicitaciones;
	JCheckBox chckbxPatentes;
	JCheckBox chckbxDocsWeb;
	JCheckBox chckbxDocs;
	
	protected JTextField tfTextoLibre;
	protected JDatePicker dpFechaDesde;
	protected JDatePicker dpFechaHasta;
	protected JTextField jtfEntidadEmisora;
	protected JComboBox<String> cbTipoLicitacion;
	protected JButton btnSectores;
	protected JLabel lblSectores;
	
	protected JDatePicker dpPatentesFechaDesde;
	protected JDatePicker dpPatentesFechaHasta;
	protected JTextField tfInventor;
	protected JTextField tfSolicitante;
	protected DelphosSelectionTreeDialog<Licitacion_Sector> dstdLicitacionSector;
	protected DelphosSelectionTreeDialog<Licitacion_Localizacion> dstdLicitacionLocalizacion;
	protected DelphosSelectionListDialog<Patente_Localizacion> dsldPatenteLocalizacion;
	protected DelphosSelectionTreeDialog<Patente_Sector> dstdPatenteSector;
	protected JButton btnLicitacionSectores;
	protected JButton btnLicitacionLocalizacion;
	protected JButton btnPatenteLocalizacion;
	protected JButton btnPatenteSectores;
	protected JLabel lblLicitacionSectores;
	protected JLabel lblLicitacionLocalizacion;
	protected JLabel lblPatenteLocalizacion;
	protected JLabel lblPatenteSectores;

	protected JCheckBox cbTesis;
	protected JCheckBox cbArticulos;
	protected JCheckBox cbCongresos;
	protected JDatePicker dpDocsFechaDesde;
	protected JDatePicker dpDocsFechaHasta;
	protected JTextField tfDocAutor;
	protected JTextField tfDocEntidad;
	protected JButton btnDocClasificacion;
	protected JLabel lblDocClasificacion;
	protected DelphosSelectionTreeDialog<Documento_Clasificacion> dstdDocClasificacion;
	
	protected JButton btnBuscar;
	protected JButton btnVerResultadosAnteriores;
	
	protected JPanel panelResultados;
	protected JScrollPane panelScroll;
	
	
	JCheckBox chckbxDocsWebInBody;
	JCheckBox chckbxDocsWebInTitle;
	JCheckBox chckbxDocsWebInKeywords;
	protected JComboBox<String> cbDocsWebActualidad;
	protected JButton btnDocsWebLocalizacion;
	protected DelphosSelectionTreeDialog<Localizacion> dstdDocsWebLocalizacion;
	protected JLabel lblDocsWebLocalizacion;
	protected JButton btnDocsWebSector;
	protected DelphosSelectionTreeDialog<Sector> dstdDocsWebSector;
	protected JLabel lblDocsWebSector;
	protected JButton btnDocsWebTipoOrganizacion;
	protected DelphosSelectionTreeDialog<TipoOrganizacion> dstdDocsWebTipoOrganizacion;
	protected DelphosSelectionTreeDialog<ContrastarCon> dstdContrastarCon;
	protected JLabel lblDocsWebTipoOrganizacion;
	protected JButton btnContrastarCon;
	
	public PanelVigilancia(DelphosFrame framePrincipal) {
		this.framePrincipal = framePrincipal;
		this.controller = new PanelVigilanciaController(this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		//Lo metemos todo en un panel de Scroll
		JPanel panelVigilancia = new JPanel();
		panelVigilancia.setAlignmentX(LEFT_ALIGNMENT);
		panelVigilancia.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		panelVigilancia.setLayout(new BoxLayout(panelVigilancia, BoxLayout.Y_AXIS));
		JScrollPane panelScroll = new JScrollPane(panelVigilancia);
		add(panelScroll);

		//Tecnologías Emergentes
		JPanel panelTecnologiasEmergentes = new JPanel();
		panelTecnologiasEmergentes.setAlignmentX(LEFT_ALIGNMENT);
		panelTecnologiasEmergentes.setBorder(BorderFactory.createMatteBorder(5,5,5,5,Color.ORANGE));
		panelVigilancia.add(panelTecnologiasEmergentes);
		panelTecnologiasEmergentes.setLayout(new BoxLayout(panelTecnologiasEmergentes, BoxLayout.Y_AXIS));
		
		JPanel subPanel1 = new JPanel();
		panelTecnologiasEmergentes.add(subPanel1);
		subPanel1.setAlignmentX(LEFT_ALIGNMENT);
		subPanel1.setBackground(Color.ORANGE);
		JLabel lblTecnologiasEmergentes = new JLabel("Tecnologías emergentes");
		lblTecnologiasEmergentes.setHorizontalAlignment(SwingConstants.CENTER);
		subPanel1.add(lblTecnologiasEmergentes);
		
		JPanel subPanel4 = new JPanel();
		panelTecnologiasEmergentes.add(subPanel4);
		subPanel4.setAlignmentX(LEFT_ALIGNMENT);
		subPanel4.setLayout(new BoxLayout(subPanel4, BoxLayout.X_AXIS));
		subPanel4.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		btnEditarListadoTecnologiasEmergentes = new JButton("Editar Listado");
		btnEditarListadoTecnologiasEmergentes.addActionListener(this.controller);
		subPanel4.add(btnEditarListadoTecnologiasEmergentes);
		
		JPanel subPanel2 = new JPanel();
		panelTecnologiasEmergentes.add(subPanel2);
		subPanel2.setAlignmentX(LEFT_ALIGNMENT);
		subPanel2.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		subPanel2.setLayout(new BoxLayout(subPanel2, BoxLayout.X_AXIS));
		
		lblAvisosPendientes = new JLabel();
		actualizarNumAvisosPendientes();
		subPanel2.add(lblAvisosPendientes);
		subPanel2.add(Box.createRigidArea(new Dimension(50, 1)));
		
		btnRevisarAvisos = new JButton("Revisar ahora");
		btnRevisarAvisos.addActionListener(this.controller);
//		if (numAvisos == 0)
//			btnRevisarAvisos.setEnabled(false);
		subPanel2.add(btnRevisarAvisos);

		
		JPanel panelBuscador = new JPanel();
		panelBuscador.setAlignmentX(LEFT_ALIGNMENT);
		panelBuscador.setBorder(BorderFactory.createMatteBorder(5,10,5,10,Color.GRAY));
		panelVigilancia.add(panelBuscador);
		panelBuscador.setLayout(new BoxLayout(panelBuscador, BoxLayout.Y_AXIS));
		
		JPanel subPanel3 = new JPanel();
		panelBuscador.add(subPanel3);
		subPanel3.setAlignmentX(LEFT_ALIGNMENT);
		subPanel3.setBackground(Color.GRAY);
		JLabel lblBuscador = new JLabel("Buscador");
		lblBuscador.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		lblBuscador.setHorizontalAlignment(SwingConstants.CENTER);
		subPanel3.add(lblBuscador);

		
		//Texto Libre - Busca en título y resumen
		JPanel panel1 = new JPanel();
		panel1.setAlignmentX(LEFT_ALIGNMENT);
		panel1.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		panelBuscador.add(panel1);
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
		panel1.add(new JLabel("Texto Libre: "));	
		this.tfTextoLibre = new JTextField();
		//this.tfTextoLibre.setMaximumSize( new Dimension(Integer.MAX_VALUE, this.tfTextoLibre.getPreferredSize().height) );
		this.tfTextoLibre.setMaximumSize( new Dimension(Integer.MAX_VALUE, 24) );
		panel1.add(this.tfTextoLibre);
		
		this.btnContrastarCon = new JButton("Contrastar con:");
		this.btnContrastarCon.addActionListener(this.controller);
		panel1.add(this.btnContrastarCon);
		
		//LICITACIONES
		panelBuscador.add(new JSeparator(JSeparator.HORIZONTAL));
		JPanel panelLicitaciones = new JPanel();
		panelLicitaciones.setLayout(new BoxLayout(panelLicitaciones, BoxLayout.Y_AXIS));
		panelLicitaciones.setAlignmentX(LEFT_ALIGNMENT);
		//panelPatentes.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		panelLicitaciones.setBorder(BorderFactory.createMatteBorder(5,10,5,10,Color.BLUE));
		panelBuscador.add(panelLicitaciones);
		
		chckbxLicitaciones = new JCheckBox("LICITACIONES:");
		panelLicitaciones.add(chckbxLicitaciones);
		
		//Fechas de Publicación
		JPanel panelFechas = new JPanel();
		panelFechas.setAlignmentX(LEFT_ALIGNMENT);
		panelFechas.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		panelLicitaciones.add(panelFechas);
		panelFechas.setLayout(new BoxLayout(panelFechas, BoxLayout.X_AXIS));
		panelFechas.add(new JLabel("Publicado entre: "));
		this.dpFechaDesde = new JDateComponentFactory().createJDatePicker();
		this.dpFechaDesde.setTextEditable(true);
		this.dpFechaDesde.setShowYearButtons(true);
		this.dpFechaDesde.getModel().setValue(null);
	    panelFechas.add((JComponent) this.dpFechaDesde);
	    panelFechas.add(Box.createRigidArea(new Dimension(50, 1)));
		panelFechas.add(new JLabel(" y: "));
		this.dpFechaHasta = new JDateComponentFactory().createJDatePicker();
		this.dpFechaHasta.setTextEditable(true);
		this.dpFechaHasta.setShowYearButtons(true);
		this.dpFechaHasta.getModel().setValue(null);
	    panelFechas.add((JComponent) this.dpFechaHasta);
		
	    //Localizacion, Tipo de Licitación y Entidad Emisora
	    JPanel panelTipoLicitacion = new JPanel();
	    panelTipoLicitacion.setAlignmentX(LEFT_ALIGNMENT);
	    panelTipoLicitacion.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	    panelLicitaciones.add(panelTipoLicitacion);
		panelTipoLicitacion.setLayout(new BoxLayout(panelTipoLicitacion, BoxLayout.X_AXIS));
		
		panelTipoLicitacion.add(new JLabel("Tipo de Licitación: "));
		this.cbTipoLicitacion = new JComboBox(this.controller.getListaTipoLicitacion());
		AutoCompleteDecorator.decorate(this.cbTipoLicitacion);
		//this.cbTipoLicitacion.setMaximumSize( new Dimension(Integer.MAX_VALUE, this.cbTipoLicitacion.getPreferredSize().height) );
		this.cbTipoLicitacion.setMaximumSize( new Dimension(Integer.MAX_VALUE, 24) );
		panelTipoLicitacion.add(this.cbTipoLicitacion);
		panelTipoLicitacion.add(new JLabel("Entidad Emisora: "));
		this.jtfEntidadEmisora = new JTextField();
	    this.jtfEntidadEmisora.setMaximumSize( new Dimension(Integer.MAX_VALUE, this.jtfEntidadEmisora.getPreferredSize().height) );
		panelTipoLicitacion.add(this.jtfEntidadEmisora);

		JPanel panelLicitacionesSectores = new JPanel();
		panelLicitacionesSectores.setLayout(new BoxLayout(panelLicitacionesSectores, BoxLayout.X_AXIS));
		panelLicitacionesSectores.setAlignmentX(LEFT_ALIGNMENT);
		panelLicitacionesSectores.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

		this.btnLicitacionSectores = new JButton("Sectores");
		this.btnLicitacionSectores.addActionListener(this.controller);
		panelLicitacionesSectores.add(this.btnLicitacionSectores);
		dstdLicitacionSector = new DelphosSelectionTreeDialog<Licitacion_Sector>(Licitacion_Sector.class, this.controller);
		panelLicitacionesSectores.add(Box.createRigidArea(new Dimension(20, 1)));
		this.lblLicitacionSectores = new JLabel("Todos");
		this.lblLicitacionSectores.setFont(new Font("Dialog", Font.PLAIN, 12));
		this.lblLicitacionSectores.setBackground(Color.WHITE);
		this.lblLicitacionSectores.setOpaque(true);
		this.lblLicitacionSectores.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(3, 5, 3, 5)));
		panelLicitacionesSectores.add(this.lblLicitacionSectores);

		panelLicitacionesSectores.add(Box.createRigidArea(new Dimension(20, 1)));
		this.btnLicitacionLocalizacion = new JButton("Países");
		this.btnLicitacionLocalizacion.addActionListener(this.controller);
		panelLicitacionesSectores.add(this.btnLicitacionLocalizacion);
		dstdLicitacionLocalizacion = new DelphosSelectionTreeDialog<Licitacion_Localizacion>(Licitacion_Localizacion.class, this.controller);
		panelLicitacionesSectores.add(Box.createRigidArea(new Dimension(20, 1)));
		this.lblLicitacionLocalizacion = new JLabel("Todos");
		this.lblLicitacionLocalizacion.setFont(new Font("Dialog", Font.PLAIN, 12));
		this.lblLicitacionLocalizacion.setBackground(Color.WHITE);
		this.lblLicitacionLocalizacion.setOpaque(true);
		this.lblLicitacionLocalizacion.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(3, 5, 3, 5)));
		panelLicitacionesSectores.add(this.lblLicitacionLocalizacion);
		
		panelLicitaciones.add(panelLicitacionesSectores);
		
		//PATENTES
		panelBuscador.add(new JSeparator(JSeparator.HORIZONTAL));
		JPanel panelPatentes = new JPanel();
		panelPatentes.setLayout(new BoxLayout(panelPatentes, BoxLayout.Y_AXIS));
		panelPatentes.setAlignmentX(LEFT_ALIGNMENT);
		//panelPatentes.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		panelPatentes.setBorder(BorderFactory.createMatteBorder(5,10,5,10,Color.GREEN));
		panelBuscador.add(panelPatentes);
		
		chckbxPatentes = new JCheckBox("PATENTES:");
		panelPatentes.add(chckbxPatentes);

		//Fechas de Publicación
		JPanel panelPatentesFechas = new JPanel();
		panelPatentesFechas.setAlignmentX(LEFT_ALIGNMENT);
		panelPatentesFechas.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		panelPatentes.add(panelPatentesFechas);
		panelPatentesFechas.setLayout(new BoxLayout(panelPatentesFechas, BoxLayout.X_AXIS));
		panelPatentesFechas.add(new JLabel("Publicado entre: "));
		this.dpPatentesFechaDesde = new JDateComponentFactory().createJDatePicker();
		this.dpPatentesFechaDesde.setTextEditable(true);
		this.dpPatentesFechaDesde.setShowYearButtons(true);
		this.dpPatentesFechaDesde.getModel().setValue(null);
	    panelPatentesFechas.add((JComponent) this.dpPatentesFechaDesde);
	    panelPatentesFechas.add(Box.createRigidArea(new Dimension(50, 1)));
		panelPatentesFechas.add(new JLabel(" y: "));
		this.dpPatentesFechaHasta = new JDateComponentFactory().createJDatePicker();
		this.dpPatentesFechaHasta.setTextEditable(true);
		this.dpPatentesFechaHasta.setShowYearButtons(true);
		this.dpPatentesFechaHasta.getModel().setValue(null);
	    panelPatentesFechas.add((JComponent) this.dpPatentesFechaHasta);
		
	    JPanel panelInventorSolicitante = new JPanel();
	    panelInventorSolicitante.setLayout(new BoxLayout(panelInventorSolicitante, BoxLayout.X_AXIS));
	    panelInventorSolicitante.setAlignmentX(LEFT_ALIGNMENT);
	    panelInventorSolicitante.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	    panelInventorSolicitante.add(new JLabel("Inventor: "));
	    this.tfInventor = new JTextField();
	    this.tfInventor.setMaximumSize( new Dimension(Integer.MAX_VALUE, this.tfInventor.getPreferredSize().height) );
	    panelInventorSolicitante.add(this.tfInventor);
	    panelInventorSolicitante.add(Box.createRigidArea(new Dimension(20, 1)));
	    panelInventorSolicitante.add(new JLabel("Solicitante: "));
	    this.tfSolicitante = new JTextField();
	    this.tfSolicitante.setMaximumSize( new Dimension(Integer.MAX_VALUE, this.tfSolicitante.getPreferredSize().height) );
	    panelInventorSolicitante.add(this.tfSolicitante);
	    panelPatentes.add(panelInventorSolicitante);
	    
		JPanel panelPatentesSectores = new JPanel();
		panelPatentesSectores.setLayout(new BoxLayout(panelPatentesSectores, BoxLayout.X_AXIS));
		panelPatentesSectores.setAlignmentX(LEFT_ALIGNMENT);
		panelPatentesSectores.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

		this.btnPatenteSectores = new JButton("Sectores");
		this.btnPatenteSectores.addActionListener(this.controller);
		panelPatentesSectores.add(this.btnPatenteSectores);
		dstdPatenteSector = new DelphosSelectionTreeDialog<Patente_Sector>(Patente_Sector.class, this.controller);
		panelPatentesSectores.add(Box.createRigidArea(new Dimension(20, 1)));
		this.lblPatenteSectores = new JLabel("Todos");
		this.lblPatenteSectores.setFont(new Font("Dialog", Font.PLAIN, 12));
		this.lblPatenteSectores.setBackground(Color.WHITE);
		this.lblPatenteSectores.setOpaque(true);
		this.lblPatenteSectores.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(3, 5, 3, 5)));
		panelPatentesSectores.add(lblPatenteSectores);

		panelPatentesSectores.add(Box.createRigidArea(new Dimension(20, 1)));
		this.btnPatenteLocalizacion = new JButton("Países");
		this.btnPatenteLocalizacion.addActionListener(this.controller);
		panelPatentesSectores.add(this.btnPatenteLocalizacion);
		dsldPatenteLocalizacion = new DelphosSelectionListDialog<Patente_Localizacion>(Patente_Localizacion.class, this.controller);
		panelPatentesSectores.add(Box.createRigidArea(new Dimension(20, 1)));
		this.lblPatenteLocalizacion = new JLabel("Todos");
		this.lblPatenteLocalizacion.setFont(new Font("Dialog", Font.PLAIN, 12));
		this.lblPatenteLocalizacion.setBackground(Color.WHITE);
		this.lblPatenteLocalizacion.setOpaque(true);
		this.lblPatenteLocalizacion.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(3, 5, 3, 5)));
		panelPatentesSectores.add(this.lblPatenteLocalizacion);
		
		panelPatentes.add(panelPatentesSectores);

		//DOCUMENTOS WEB
		panelBuscador.add(new JSeparator(JSeparator.HORIZONTAL));
		JPanel panelDocsWeb = new JPanel();
		panelDocsWeb.setLayout(new BoxLayout(panelDocsWeb, BoxLayout.Y_AXIS));
		panelDocsWeb.setAlignmentX(LEFT_ALIGNMENT);
	//	panelDocsWeb.setLayout(new BorderLayout());
		panelDocsWeb.setBorder(BorderFactory.createMatteBorder(5,10,5,10,Color.YELLOW));
		panelBuscador.add(panelDocsWeb);
				
		chckbxDocsWeb = new JCheckBox("DOCUMENTOS WEB:");
		panelDocsWeb.add(chckbxDocsWeb);
		panelDocsWeb.add(Box.createHorizontalGlue());
		
		//Subpanel Bing
		JPanel panelBing = new JPanel();
		panelBing.setLayout(new BoxLayout(panelBing, BoxLayout.X_AXIS));
		panelBing.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		panelBing.setAlignmentX(LEFT_ALIGNMENT);
		panelBing.add(new JLabel("Buscar en:   "));
		this.chckbxDocsWebInBody = new JCheckBox("el cuerpo del documento   ");
		panelBing.add(chckbxDocsWebInBody);
		this.chckbxDocsWebInTitle = new JCheckBox("el título   ");
		panelBing.add(chckbxDocsWebInTitle);
		this.chckbxDocsWebInKeywords = new JCheckBox("las palabras clave");
		panelBing.add(chckbxDocsWebInKeywords);
		panelBing.add(Box.createHorizontalGlue());
		panelBing.add(new JLabel("Actualidad:"));
		this.cbDocsWebActualidad = new JComboBox<>(new String[] {"", ULTIMAS_24_HORAS, ULTIMA_SEMANA, ULTIMO_MES});
		this.cbDocsWebActualidad.setMaximumSize( new Dimension(300, 24) );
		panelBing.add(cbDocsWebActualidad);
		panelDocsWeb.add(panelBing);
				
		//Criterios
		JPanel panelCriteriosDocsWeb = new JPanel();
		panelCriteriosDocsWeb.setLayout(new BoxLayout(panelCriteriosDocsWeb, BoxLayout.X_AXIS));
		panelCriteriosDocsWeb.setAlignmentX(LEFT_ALIGNMENT);
		panelCriteriosDocsWeb.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

		this.btnDocsWebLocalizacion = new JButton("Localizacion");
		this.btnDocsWebLocalizacion.addActionListener(this.controller);
		panelCriteriosDocsWeb.add(this.btnDocsWebLocalizacion);
		dstdDocsWebLocalizacion = new DelphosSelectionTreeDialog<Localizacion>(Localizacion.class, this.controller);
		panelCriteriosDocsWeb.add(Box.createRigidArea(new Dimension(20, 1)));
		this.lblDocsWebLocalizacion = new JLabel("Todos");
		this.lblDocsWebLocalizacion.setFont(new Font("Dialog", Font.PLAIN, 12));
		this.lblDocsWebLocalizacion.setBackground(Color.WHITE);
		this.lblDocsWebLocalizacion.setOpaque(true);
		this.lblDocsWebLocalizacion.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(3, 5, 3, 5)));
		panelCriteriosDocsWeb.add(lblDocsWebLocalizacion);
		panelCriteriosDocsWeb.add(Box.createRigidArea(new Dimension(20, 1)));
		
		this.btnDocsWebSector = new JButton("Sector");
		this.btnDocsWebSector.addActionListener(this.controller);
		panelCriteriosDocsWeb.add(this.btnDocsWebSector);
		dstdDocsWebSector = new DelphosSelectionTreeDialog<Sector>(Sector.class, this.controller);
		panelCriteriosDocsWeb.add(Box.createRigidArea(new Dimension(20, 1)));
		this.lblDocsWebSector = new JLabel("Todos");
		this.lblDocsWebSector.setFont(new Font("Dialog", Font.PLAIN, 12));
		this.lblDocsWebSector.setBackground(Color.WHITE);
		this.lblDocsWebSector.setOpaque(true);
		this.lblDocsWebSector.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(3, 5, 3, 5)));
		panelCriteriosDocsWeb.add(lblDocsWebSector);
		panelCriteriosDocsWeb.add(Box.createRigidArea(new Dimension(20, 1)));
		
		this.btnDocsWebTipoOrganizacion = new JButton("Tipo de Organización");
		this.btnDocsWebTipoOrganizacion.addActionListener(this.controller);
		panelCriteriosDocsWeb.add(this.btnDocsWebTipoOrganizacion);
		dstdDocsWebTipoOrganizacion = new DelphosSelectionTreeDialog<TipoOrganizacion>(TipoOrganizacion.class, this.controller);
		dstdContrastarCon = new DelphosSelectionTreeDialog<ContrastarCon>(ContrastarCon.class, this.controller);
		panelCriteriosDocsWeb.add(Box.createRigidArea(new Dimension(20, 1)));
		this.lblDocsWebTipoOrganizacion = new JLabel("Todos");
		this.lblDocsWebTipoOrganizacion.setFont(new Font("Dialog", Font.PLAIN, 12));
		this.lblDocsWebTipoOrganizacion.setBackground(Color.WHITE);
		this.lblDocsWebTipoOrganizacion.setOpaque(true);
		this.lblDocsWebTipoOrganizacion.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(3, 5, 3, 5)));
		panelCriteriosDocsWeb.add(lblDocsWebTipoOrganizacion);
		panelCriteriosDocsWeb.add(Box.createRigidArea(new Dimension(20, 1)));
		
		panelDocsWeb.add(panelCriteriosDocsWeb);
		
	    
		//DOCUMENTOS ACADÉMICOS
		panelBuscador.add(new JSeparator(JSeparator.HORIZONTAL));
		JPanel panelDocs = new JPanel();
		panelDocs.setLayout(new BoxLayout(panelDocs, BoxLayout.Y_AXIS));
		panelDocs.setAlignmentX(LEFT_ALIGNMENT);
		//panelPatentes.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		panelDocs.setBorder(BorderFactory.createMatteBorder(5,10,5,10,Color.RED));
		panelBuscador.add(panelDocs);
		
		chckbxDocs = new JCheckBox("DOCUMENTOS ACADÉMICOS:");
		panelDocs.add(chckbxDocs);
		
		//Tipos de Documentos
		JPanel panelDocsTipos = new JPanel();
		panelDocsTipos.setLayout(new BoxLayout(panelDocsTipos, BoxLayout.X_AXIS));
		panelDocsTipos.setAlignmentX(LEFT_ALIGNMENT);
		cbTesis = new JCheckBox("Tesis");
		cbArticulos = new JCheckBox("Artículos");
		cbCongresos = new JCheckBox("Congresos");
		panelDocsTipos.add(cbTesis);
		panelDocsTipos.add(cbArticulos);
		panelDocsTipos.add(cbCongresos);
		panelDocs.add(panelDocsTipos);
		
		
		//Fechas de Publicación
		JPanel panelDocsFechas = new JPanel();
		panelDocsFechas.setAlignmentX(LEFT_ALIGNMENT);
		panelDocsFechas.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		panelDocs.add(panelDocsFechas);
		panelDocsFechas.setLayout(new BoxLayout(panelDocsFechas, BoxLayout.X_AXIS));
		panelDocsFechas.add(new JLabel("Publicado entre: "));
		this.dpDocsFechaDesde = new JDateComponentFactory().createJDatePicker();
		this.dpDocsFechaDesde.setTextEditable(true);
		this.dpDocsFechaDesde.setShowYearButtons(true);
		this.dpDocsFechaDesde.getModel().setDate(1970,0,1);
	    panelDocsFechas.add((JComponent) this.dpDocsFechaDesde);
	    panelDocsFechas.add(Box.createRigidArea(new Dimension(50, 1)));
		panelDocsFechas.add(new JLabel(" y: "));
		this.dpDocsFechaHasta = new JDateComponentFactory().createJDatePicker();
		this.dpDocsFechaHasta.setTextEditable(true);
		this.dpDocsFechaHasta.setShowYearButtons(true);
		this.dpDocsFechaHasta.getModel().setDate(2017,3,1);
	    panelDocsFechas.add((JComponent) this.dpDocsFechaHasta);
	    
	    JPanel panelAutorEntidad = new JPanel();
	    panelAutorEntidad.setLayout(new BoxLayout(panelAutorEntidad, BoxLayout.X_AXIS));
	    panelAutorEntidad.setAlignmentX(LEFT_ALIGNMENT);
	    panelAutorEntidad.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	    panelAutorEntidad.add(new JLabel("Autor: "));
	    this.tfDocAutor = new JTextField();
	    this.tfDocAutor.setMaximumSize( new Dimension(Integer.MAX_VALUE, this.tfDocAutor.getPreferredSize().height) );
	    panelAutorEntidad.add(this.tfDocAutor);
	    panelAutorEntidad.add(Box.createRigidArea(new Dimension(20, 1)));
	    panelAutorEntidad.add(new JLabel("Entidad/Publicación: "));
	    this.tfDocEntidad = new JTextField();
	    this.tfDocEntidad.setMaximumSize( new Dimension(Integer.MAX_VALUE, this.tfDocEntidad.getPreferredSize().height) );
	    panelAutorEntidad.add(this.tfDocEntidad);
	    panelDocs.add(panelAutorEntidad);

		JPanel panelDocClasificacion = new JPanel();
		panelDocClasificacion.setLayout(new BoxLayout(panelDocClasificacion, BoxLayout.X_AXIS));
		panelDocClasificacion.setAlignmentX(LEFT_ALIGNMENT);
		panelDocClasificacion.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		this.btnDocClasificacion = new JButton("Clasificación");
		this.btnDocClasificacion.addActionListener(this.controller);
		panelDocClasificacion.add(this.btnDocClasificacion);
		//TODO Cambiar para que salga de la tabla de 
		dstdDocClasificacion = new DelphosSelectionTreeDialog<Documento_Clasificacion>(Documento_Clasificacion.class, this.controller);
		panelDocClasificacion.add(Box.createRigidArea(new Dimension(20, 1)));
		this.lblDocClasificacion = new JLabel("Todos");
		this.lblDocClasificacion.setFont(new Font("Dialog", Font.PLAIN, 12));
		this.lblDocClasificacion.setBackground(Color.WHITE);
		this.lblDocClasificacion.setOpaque(true);
		this.lblDocClasificacion.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
				BorderFactory.createEmptyBorder(3, 5, 3, 5)));
		panelDocClasificacion.add(lblDocClasificacion);
		panelDocs.add(panelDocClasificacion);

		
		//Filler 
	    Component glue = Box.createVerticalGlue();
	    panelBuscador.add(glue);		
	    panelBuscador.add(new JSeparator(JSeparator.HORIZONTAL));
		
		//Botones
		JPanel panelBotones = new JPanel();
		panelBotones.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.X_AXIS));
		panelBotones.setAlignmentX(LEFT_ALIGNMENT);
		panelBotones.add(Box.createHorizontalGlue());
		this.btnVerResultadosAnteriores = new JButton("VerResultadosAnteriores");
		panelBotones.add(this.btnVerResultadosAnteriores);
		this.btnVerResultadosAnteriores.addActionListener(this.controller);
		this.btnBuscar = new JButton("Buscar");
		panelBotones.add(this.btnBuscar);
		this.btnBuscar.addActionListener(this.controller);
		panelBuscador.add(panelBotones);
		
		/*
		
		//Panel de Resultados con Scroll
		panelResultados = new JPanel();
		panelResultados.setBackground(Color.WHITE);
		panelResultados.setMinimumSize(new Dimension(this.getWidth(),this.getHeight()));		
		panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));
		panelScroll = new JScrollPane(panelResultados);
		panelScroll.setBorder(null);
		add(panelScroll);
		*/
	}

	public void actualizarNumAvisosPendientes() {
		int numAvisos;
		String sql = "SELECT COUNT(*) FROM AvisoTecnologiasEmergentes WHERE revisado = 0";
		Query query = Delphos.getSession().createSQLQuery(sql);
		numAvisos = ((BigInteger) query.uniqueResult()).intValue();
		
		if (numAvisos != 0)
			lblAvisosPendientes.setText("Tiene " + numAvisos + " avisos pendientes de revisión.");
		else
			lblAvisosPendientes.setText("No tiene avisos pendientes");
		this.revalidate();
	}

	@Override
	public JButton getDefaultButton() {
		return btnBuscar;
	}
}
