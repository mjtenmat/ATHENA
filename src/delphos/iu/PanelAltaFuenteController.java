package delphos.iu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.hibernate.Session;

import delphos.Fuente;

public class PanelAltaFuenteController implements ActionListener{
	private PanelAltaFuente panelAltaFuente;

	public PanelAltaFuenteController(PanelAltaFuente panelAltaFuente) {
		this.panelAltaFuente = panelAltaFuente;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getSource().getClass().toString()) {
		case "class javax.swing.JButton":
			switch (((JButton) e.getSource()).getName()) {	
			case "btnCancelar":
				System.out.println("Pulsado botón Cancelar");
				panelAltaFuente.framePrincipal.controller.verPanel(panelAltaFuente.framePrincipal.panelBusqueda);
				break;
			case "btnAceptar":
				System.out.println("Pulsado botón Aceptar");
				Fuente fuente = new Fuente();
				try {
					fuente.setUrl(new URL(panelAltaFuente.getUrl()));

					Delphos.setHibernateCfgFile("hibernate.cfg.xml");
					Session session = Delphos.getSession();
					session.beginTransaction();
					session.save(fuente);
					session.getTransaction().commit();
					
					JOptionPane.showMessageDialog(panelAltaFuente, "Fuente registrada correctamente", "Operación Correcta", JOptionPane.OK_OPTION);
					//TODO borrar el campo
				} catch (MalformedURLException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(panelAltaFuente, "El formato de la URL es incorrecto\nDebe ser del estilo http://www.ejemplo.com/directorio/pagina.html", "Error en la URL", JOptionPane.ERROR_MESSAGE);
				}catch(Exception ex){
					ex.printStackTrace();
					String msg = "Se ha producido el siguiente error \"" + ex.getMessage() + "\"";
					msg += "\nIntente repetir la operación o consulte el log.";
					JOptionPane.showMessageDialog(panelAltaFuente, msg, "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
				}
				finally{
					//TODO poner el cursor en el campo.
					//TODO seleccionar el texto del campo (si hay).
				}
			}
		}
	}

}
