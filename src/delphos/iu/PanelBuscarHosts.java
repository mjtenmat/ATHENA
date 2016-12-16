package delphos.iu;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.tree.TreeSelectionModel;

import delphos.Jerarquia;
import delphos.Localizacion;
import delphos.Sector;
import delphos.TipoOrganizacion;

public class PanelBuscarHosts extends JPanel implements ConDefaultButton{
	protected DelphosFrame framePrincipal;
	private PanelBuscarHostsController controlador;
	protected JTextField tfHost;
	protected JButton btnBuscarHosts;
	protected JPanel pnResultadosHost;
	protected JScrollPane panelScroll;
	
	protected DelphosTreeDialog<Sector> treeFrameSector;
	protected DelphosTreeDialog<TipoOrganizacion> treeFrameTipoOrganizacion;
	protected DelphosTreeDialog<Localizacion> treeFrameLocalizacion;
	
	public PanelBuscarHosts(DelphosFrame framePrincipal) {
		this.framePrincipal = framePrincipal;
		this.controlador = new PanelBuscarHostsController(this);
		setLayout(new BorderLayout(0, 0));
		
		//Creamos el panel para el campo de texto y el botón
		JPanel panel = new JPanel();
		add(panel,BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		tfHost = new JTextField();
		panel.add(tfHost);
		tfHost.setColumns(10);
		
		btnBuscarHosts = new JButton("Buscar Hosts");
		btnBuscarHosts.addActionListener(this.controlador);
		panel.add(btnBuscarHosts);
		
		//Creamos el panel de resultados
		pnResultadosHost = new JPanel();
		//add(pnResultadosHost,BorderLayout.CENTER);
		pnResultadosHost.setLayout(new BoxLayout(pnResultadosHost, BoxLayout.Y_AXIS));
		
		//Ponemos el panel de resultados en un panel de scroll
		panelScroll = new JScrollPane(pnResultadosHost);
		panelScroll.setBorder(null);
		this.add(panelScroll);
		
		this.crearArboles();
		
		this.setVisible(false);
	}
	
	private void crearArboles(){
		treeFrameSector = new DelphosTreeDialog<Sector>(Sector.class);	
		treeFrameSector.setController(controlador);
		treeFrameTipoOrganizacion = new DelphosTreeDialog<TipoOrganizacion>(TipoOrganizacion.class);
		treeFrameTipoOrganizacion.setController(controlador);
		treeFrameTipoOrganizacion.setModoSeleccion(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeFrameLocalizacion = new DelphosTreeDialog<Localizacion>(Localizacion.class);
		treeFrameLocalizacion.setController(controlador);
		treeFrameLocalizacion.setModoSeleccion(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	@Override
	public JButton getDefaultButton() {
		return btnBuscarHosts;
	}
}
