package delphos.iu;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import delphos.Host;
import delphos.Localizacion;
import delphos.Sector;
import delphos.TipoOrganizacion;

public class PanelHost extends JPanel {
	protected Host host;
	protected PanelBuscarHostsController controlador;
	protected JLabel lblUrl;
	protected JButton btnEditar;
	protected JButton btnAceptar;
	protected JButton btnCancelar;
	protected JLabel lblSectores;
	protected JButton btnSectores;
	protected JLabel lblTipoOrganizacion;
	protected JButton btnTipoOrganizacion;
	protected JLabel lblLocalizacion;
	protected JButton btnLocalizacion;
	
	public PanelHost(Host host, PanelBuscarHostsController controlador) {
		this.host = host;
		this.controlador = controlador;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel panel_1 = new JPanel();
		add(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		btnEditar = new JButton("Editar");
		btnEditar.setName("btnEditar");
		btnEditar.addActionListener(controlador);
		panel_1.add(btnEditar);
		
		btnAceptar = new JButton("Aceptar");
		btnAceptar.setName("btnAceptar");
		btnAceptar.addActionListener(controlador);
		btnAceptar.setVisible(false);
		panel_1.add(btnAceptar);
		
		btnCancelar = new JButton("Cancelar");
		btnCancelar.setName("btnCancelar");
		btnCancelar.addActionListener(controlador);
		btnCancelar.setVisible(false);
		panel_1.add(btnCancelar);
		
		JLabel label = new JLabel("URL del Host: ");
		panel_1.add(label);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		lblUrl = new JLabel();
		panel_1.add(lblUrl);
		
		JPanel panel = new JPanel();
		add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JPanel pnSectores = new JPanel();
		panel.add(pnSectores);
		pnSectores.setLayout(new BoxLayout(pnSectores, BoxLayout.X_AXIS));
		pnSectores.add(new JLabel("Sectores: "));
		lblSectores = new JLabel();
		pnSectores.add(lblSectores);
		btnSectores = new JButton("Sectores");
		btnSectores.addActionListener(controlador);
		btnSectores.setName("btnSectores");
		btnSectores.setVisible(false);
		pnSectores.add(btnSectores);
		
		JPanel pnTipoOrganizacion = new JPanel();
		panel.add(pnTipoOrganizacion);
		pnTipoOrganizacion.setLayout(new BoxLayout(pnTipoOrganizacion, BoxLayout.X_AXIS));
		pnTipoOrganizacion.add(new JLabel("Tipo de Organización: "));
		lblTipoOrganizacion = new JLabel();
		pnTipoOrganizacion.add(lblTipoOrganizacion);
		btnTipoOrganizacion = new JButton("Tipo de Organización");
		btnTipoOrganizacion.addActionListener(controlador);
		btnTipoOrganizacion.setName("btnTipoOrganizacion");
		btnTipoOrganizacion.setVisible(false);
		pnTipoOrganizacion.add(btnTipoOrganizacion);

		JPanel pnLocalizacion = new JPanel();
		panel.add(pnLocalizacion);
		pnLocalizacion.setLayout(new BoxLayout(pnLocalizacion, BoxLayout.X_AXIS));
		pnLocalizacion.add(new JLabel("Localización: "));
		lblLocalizacion = new JLabel();
		pnLocalizacion.add(lblLocalizacion);
		btnLocalizacion = new JButton("Localización");
		btnLocalizacion.addActionListener(controlador);
		btnLocalizacion.setName("btnLocalizacion");
		btnLocalizacion.setVisible(false);
		pnLocalizacion.add(btnLocalizacion);

	}
	
	public Host getHost(){
		return this.host;
	}
	public void setHost(Host host){
		this.host = host;
	}
	
	public void editar(boolean modo){
		if (modo)
			this.setBorder(BorderFactory.createLineBorder(Color.red));
		else
			this.setBorder(null);
		
		btnEditar.setVisible(!modo);
		btnAceptar.setVisible(modo);
		btnCancelar.setVisible(modo);
		btnSectores.setVisible(modo);
		btnTipoOrganizacion.setVisible(modo);
		btnLocalizacion.setVisible(modo);
	}
}
