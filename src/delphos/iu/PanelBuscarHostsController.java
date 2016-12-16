package delphos.iu;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import delphos.Host;
import delphos.Localizacion;
import delphos.TipoOrganizacion;

public class PanelBuscarHostsController implements ActionListener{
	private PanelBuscarHosts pnBuscarHosts;
	private PanelHost pnHostEditado = null;
	
	public PanelBuscarHostsController(PanelBuscarHosts pnHosts){
		this.pnBuscarHosts = pnHosts;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		pnBuscarHosts.framePrincipal.frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (ae.getSource() == pnBuscarHosts.btnBuscarHosts){
			System.out.println("Pulsado botón buscar Hosts");
			this.buscarPresentar();
		}
		
		if (ae.getSource().getClass() == JButton.class){
			if (((JButton)ae.getSource()).getName() == "btnEditar"){	//Botón de editar host
				this.pnHostEditado = (PanelHost)(((JButton)ae.getSource()).getParent().getParent());
				this.pnHostEditado.editar(true);
			}
			if (((JButton)ae.getSource()).getName() == "btnCancelar"){	//Botón de cancelar editar host
				this.pnHostEditado = (PanelHost)(((JButton)ae.getSource()).getParent().getParent());
				this.buscarPresentar();//Restauramos los valores
				this.pnHostEditado = null;
			}
			if (((JButton)ae.getSource()).getName() == "btnAceptar"){	//Botón de Aceptar editar host
				this.pnHostEditado = (PanelHost)(((JButton)ae.getSource()).getParent().getParent());
				try{
					Delphos.setHibernateCfgFile("hibernate.cfg.xml");
					Session session = Delphos.getSession();
					session.beginTransaction();
					session.save(this.pnHostEditado.getHost());
					session.getTransaction().commit();
					JOptionPane.showMessageDialog(this.pnBuscarHosts, "Host Actualizado Correctamente", "Operación Correcta", JOptionPane.OK_OPTION);
				
					this.pnHostEditado.editar(false);
				}
				catch(Exception ex){
					ex.printStackTrace();
					String msg = "Se ha producido el siguiente error \"" + ex.getMessage() + "\"";
					msg += "\nIntente repetir la operación o consulte el log.";
					JOptionPane.showMessageDialog(this.pnBuscarHosts, msg, "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
				}
			}
			if (((JButton)ae.getSource()).getName() == "btnSectores"){
				System.out.println("Editar Sectores de Host");
				PanelHost pnHost = (PanelHost)(((JButton)ae.getSource()).getParent().getParent().getParent());
				System.out.println(pnHost.getHost().getUrl());
				pnBuscarHosts.treeFrameSector.seleccionar(pnHost.getHost().getSectores());
				pnBuscarHosts.treeFrameSector.setVisible(true);
			}
			if (((JButton)ae.getSource()).getName() == "btnTipoOrganizacion"){
				System.out.println("Editar Tipo de Organización de Host");
				PanelHost pnHost = (PanelHost)(((JButton)ae.getSource()).getParent().getParent().getParent());
				System.out.println(pnHost.getHost().getUrl());
				pnBuscarHosts.treeFrameTipoOrganizacion.seleccionarUnico(pnHost.getHost().getTipoOrganizacion());
				pnBuscarHosts.treeFrameTipoOrganizacion.setVisible(true);
			}
			if (((JButton)ae.getSource()).getName() == "btnLocalizacion"){
				System.out.println("Editar Tipo de Organización de Host");
				PanelHost pnHost = (PanelHost)(((JButton)ae.getSource()).getParent().getParent().getParent());
				System.out.println(pnHost.getHost().getUrl());
				pnBuscarHosts.treeFrameLocalizacion.seleccionarUnico(pnHost.getHost().getLocalizacion());
				pnBuscarHosts.treeFrameLocalizacion.setVisible(true);
			}

			if (((JButton)ae.getSource()).getName() == "btnCancelarDelphosTree"){
				System.out.println("Botón cancelar edición de jerarquía");
				SwingUtilities.getRoot((Component) ae.getSource()).setVisible(false);
			}
			if (((JButton)ae.getSource()).getName() == "btnBorrarDelphosTree"){
				System.out.println("Botón borrar edición de jerarquía");			
				((DelphosTreeDialog) SwingUtilities.getRoot((Component) ae.getSource())).borrarSeleccion();
			}
			if (((JButton)ae.getSource()).getName() == "btnAceptarDelphosTree"){
				System.out.println("Botón aceptar edición de jerarquía");
				DelphosTreeDialog treeDialog = (DelphosTreeDialog) SwingUtilities.getRoot((Component) ae.getSource());
				String restriccion = treeDialog.getClase().getSimpleName();
				if (restriccion.equals("Sector")){
					pnHostEditado.getHost().setSectores(treeDialog.getSeleccion());
					pnHostEditado.lblSectores.setText(pnHostEditado.getHost().getSectores().toString());
				}
				if (restriccion.equals("TipoOrganizacion")){
					try {
						pnHostEditado.getHost().setTipoOrganizacion((TipoOrganizacion)treeDialog.getSeleccionUnico(TipoOrganizacion.class));
						pnHostEditado.lblTipoOrganizacion.setText(pnHostEditado.getHost().getTipoOrganizacion().getNombre());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						JOptionPane.showMessageDialog(pnBuscarHosts, "No es posible asignar múltiples tipos de organización a una fuente", "Error de Edición", JOptionPane.ERROR_MESSAGE);
					}
				}
				if (restriccion.equals("Localizacion")){
					try {
						pnHostEditado.getHost().setLocalizacion((Localizacion)treeDialog.getSeleccionUnico(Localizacion.class));
						pnHostEditado.lblLocalizacion.setText(pnHostEditado.getHost().getLocalizacion().getNombre());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(pnBuscarHosts, "No es posible asignar múltiples localizaciones a una fuente", "Error de Edición", JOptionPane.ERROR_MESSAGE);
					}
				}
				//Cerrar tree
				SwingUtilities.getRoot((Component) ae.getSource()).setVisible(false);
			}
			
		}
		
		pnBuscarHosts.framePrincipal.frame.setCursor(Cursor.getDefaultCursor());
	}
	
	private void buscarPresentar(){
		pnBuscarHosts.pnResultadosHost.removeAll();

		Criteria crit = Delphos.getSession().createCriteria(Host.class, "host");
		if (!pnBuscarHosts.tfHost.getText().equals("")){
			crit.add(Restrictions.like("url", pnBuscarHosts.tfHost.getText(), MatchMode.ANYWHERE));
		}
		crit.addOrder(Order.asc("id"));
		ArrayList<Host> listaHosts = (ArrayList<Host>)crit.list();

		//Presentamos los resultados
		if (listaHosts.size() == 0){
			pnBuscarHosts.pnResultadosHost.add(new JLabel("No se han encontrado resultados"));
		}
		else if (listaHosts.size() == 1)
			pnBuscarHosts.pnResultadosHost.add(new JLabel("Se ha encontrado 1 resultado"));
		else
			pnBuscarHosts.pnResultadosHost.add(new JLabel("Se han encontrado " + listaHosts.size() + " resultados"));

		for (Host host : listaHosts){
			PanelHost pnHost = new PanelHost(host, this);
			pnHost.lblUrl.setText(host.getId() + ".-" + host.getUrl());
			if (host.getSectores() != null)
				pnHost.lblSectores.setText(host.getSectores().toString());
			if (host.getTipoOrganizacion() != null)
				pnHost.lblTipoOrganizacion.setText(host.getTipoOrganizacion().getNombre());
			if (host.getLocalizacion() != null)
				pnHost.lblLocalizacion.setText(host.getLocalizacion().getNombre());
			pnBuscarHosts.pnResultadosHost.add(pnHost);
		}
		pnBuscarHosts.revalidate();
	}
	
	

}
