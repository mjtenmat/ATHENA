package delphos.iu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import delphos.Localizacion;
import delphos.Sector;
import delphos.TipoOrganizacion;

public class PanelBusqueda extends JPanel implements ConDefaultButton{
	protected DelphosFrame framePrincipal;
	
	protected JPanel panelConsulta;
	protected JTextField jtfConsulta;
	protected JButton btnBuscar;
	protected JPanel panelResultados;
	protected JScrollPane panelScroll;
	
	protected PanelBusquedaController controller;
	protected JButton btnRRmin;
	protected JButton btnRRmax;
	private JPanel panelRestricciones;
	private JButton btnSectores;
	private JButton btnTipoOrganizacion;
	private JButton btnLocalizacion;
	private JLabel lblRestricciones;
	private JPanel panelSector;
	private JLabel lblSectores;
	private JPanel panelTipoOrganizacion;
	private JLabel lblTipoOrganizacion;
	private JPanel panelLocalizacion;
	private JLabel lblLocalizacion;
	private JPanel panelBotonesRestricciones;
	
	protected DialogRelevantes dlgRelevantes;
	protected DelphosTreeDialog<Sector> treeFrameSector;
	protected DelphosTreeDialog<TipoOrganizacion> treeFrameTipoOrganizacion;
	protected DelphosTreeDialog<Localizacion> treeFrameLocalizacion;
	private JButton btnRelevantes;
	

	public PanelBusqueda(DelphosFrame framePrincipal) {
		this.framePrincipal = framePrincipal;
		controller = new PanelBusquedaController(this);
		
		this.setBackground(Color.WHITE);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		crearPanelConsulta();
		crearDialogoRelevantes();
		crearPanelRestricciones();
		crearArboles();
		crearPanelResultados();

		panelScroll = new JScrollPane(panelResultados);
		panelScroll.setBorder(null);
		this.add(panelScroll);
		
	}

	private void crearPanelConsulta(){
		panelConsulta = new JPanel();
		panelConsulta.setBackground(Color.WHITE);
		this.add(panelConsulta);

		jtfConsulta = new JTextField();
		//jtfConsulta.setText("Energía México");
		jtfConsulta.setToolTipText("Introduzca el texto a buscar");
		jtfConsulta.setColumns(25);
		panelConsulta.add(jtfConsulta);

		btnBuscar = new JButton("Buscar");
		btnBuscar.setName("btnBuscar");
		btnBuscar.addActionListener(controller);
		panelConsulta.add(btnBuscar);
		
		btnRRmin = new JButton("RR-min");
		btnRRmin.setName("btnRRmin");
		btnRRmin.addActionListener(controller);
		btnRRmin.setVisible(false);
		panelConsulta.add(btnRRmin);	
		
		btnRRmax = new JButton("RR-max");
		btnRRmax.setName("btnRRmax");
		btnRRmax.addActionListener(controller);
		btnRRmax.setVisible(false);
		panelConsulta.add(btnRRmax);	
		
		btnRelevantes = new JButton("Relevantes");
		btnRelevantes.setName("btnRelevantes");
		btnRelevantes.addActionListener(controller);
		btnRelevantes.setVisible(false);
		panelConsulta.add(btnRelevantes);
	}
	
	private void crearPanelRestricciones(){
		panelRestricciones = new JPanel();
		panelRestricciones.setLayout(new BoxLayout(panelRestricciones, BoxLayout.Y_AXIS));
		this.add(panelRestricciones);
		
		lblRestricciones = new JLabel("Restricciones:");
		panelRestricciones.add(lblRestricciones);
		
		panelBotonesRestricciones = new JPanel();
		panelRestricciones.add(panelBotonesRestricciones);
		
		panelSector = new JPanel();
		panelBotonesRestricciones.add(panelSector);
		panelSector.setLayout(new BoxLayout(panelSector, BoxLayout.Y_AXIS));
		
		btnSectores = new JButton("Sectores");
		panelSector.add(btnSectores);
		btnSectores.addActionListener(controller);
		btnSectores.setName("btnSectores");
		
		lblSectores = new JLabel("Todos.");
		lblSectores.setFont(new Font("Dialog", Font.PLAIN, 12));
		panelSector.add(lblSectores);
		
		panelTipoOrganizacion = new JPanel();
		panelBotonesRestricciones.add(panelTipoOrganizacion);
		panelTipoOrganizacion.setLayout(new BoxLayout(panelTipoOrganizacion, BoxLayout.Y_AXIS));
		
		btnTipoOrganizacion = new JButton("Tipo de Organización");
		btnTipoOrganizacion.setName("btnTipoOrganizacion");
		btnTipoOrganizacion.addActionListener(controller);
		panelTipoOrganizacion.add(btnTipoOrganizacion);
		
		lblTipoOrganizacion = new JLabel("Todos.");
		lblTipoOrganizacion.setFont(new Font("Dialog", Font.PLAIN, 12));
		panelTipoOrganizacion.add(lblTipoOrganizacion);
		
		panelLocalizacion = new JPanel();
		panelBotonesRestricciones.add(panelLocalizacion);
		panelLocalizacion.setLayout(new BoxLayout(panelLocalizacion, BoxLayout.Y_AXIS));
		
		btnLocalizacion = new JButton("Localización");
		btnLocalizacion.setName("btnLocalizacion");
		btnLocalizacion.addActionListener(controller);
		panelLocalizacion.add(btnLocalizacion);
		
		lblLocalizacion = new JLabel("Todas.");
		lblLocalizacion.setFont(new Font("Dialog", Font.PLAIN, 12));
		panelLocalizacion.add(lblLocalizacion);
	
	}
	
	private void crearPanelResultados(){
		panelResultados = new JPanel();
		panelResultados.setBackground(Color.WHITE);
		panelResultados.setMinimumSize(new Dimension(this.getWidth(),this.getHeight()));
		
		panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));
	}
	
	private void crearDialogoRelevantes(){
		dlgRelevantes = new DialogRelevantes();
		dlgRelevantes.setController(controller);
	}
	
	private void crearArboles(){
		treeFrameSector = new DelphosTreeDialog<Sector>(Sector.class);	
		treeFrameSector.setController(controller);
		treeFrameTipoOrganizacion = new DelphosTreeDialog<TipoOrganizacion>(TipoOrganizacion.class);
		treeFrameTipoOrganizacion.setController(controller);
		treeFrameLocalizacion = new DelphosTreeDialog<Localizacion>(Localizacion.class);
		treeFrameLocalizacion.setController(controller);
	}
	
	public JPanel getPanelResultados() {
        return panelResultados;
    }

	public JTextField getJtfConsulta() {
		return jtfConsulta;
	}

	public JButton getBtnBuscar() {
		return btnBuscar;
	}

	public String getConsulta(){
		return jtfConsulta.getText();
	}
	
	@Override
	public JButton getDefaultButton(){
		return btnBuscar;
	}

	public JButton getBtnRRmin() {
		return btnRRmin;
	}

	public JButton getBtnRRmax() {
		return btnRRmax;
	}

	public JButton getBtnRelevantes() {
		return btnRelevantes;
	}
	
	public DelphosTreeDialog getTreeFrameSector(){
		return this.treeFrameSector;
	}
	
	public DelphosTreeDialog getTreeFrameTipoOrganizacion(){
		return this.treeFrameTipoOrganizacion;
	}
	
	public DelphosTreeDialog getTreeFrameLocalizacion(){
		return this.treeFrameLocalizacion;
	}

	public void setTextoRestriccion(String restriccion, String texto){
		if (restriccion.equals("Sector"))
			lblSectores.setText(texto);
		if (restriccion.equals("TipoOrganizacion"))
			lblTipoOrganizacion.setText(texto);
		if (restriccion.equals("Localizacion"))
			lblLocalizacion.setText(texto);
	}
}
