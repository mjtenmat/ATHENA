package delphos.iu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import delphos.Licitacion;

public class PanelLicitacion extends JPanel {
	protected Licitacion licitacion;
	protected JLabel lblTitulo;
	protected JLabel lblUrl;
	protected JLabel lblCPV;
	protected JLabel lblTipoDoc;
	protected JLabel lblEntidadEmisora;
	protected JLabel lblFechaPublicacion;
	protected JLabel lblLocalizacion;
	protected JLabel lblSector;
	protected JTextArea taResumen;
	
	public PanelLicitacion(Licitacion licitacion){
		this.licitacion = licitacion;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));		
		this.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		
		this.lblTitulo = new JLabel("<html><font color=\"blue\">Título:</font> " + licitacion.getTitulo()+"</html>");
		this.lblTitulo.setHorizontalAlignment(SwingConstants.LEFT);
		this.lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.add(lblTitulo);
		
		this.lblUrl = new JLabel(licitacion.getUrl().toString());
		this.lblUrl.setForeground(Color.GREEN);
		this.lblUrl.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.lblUrl.setHorizontalAlignment(SwingConstants.LEFT);
		this.lblUrl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.add(lblUrl);
		
		this.lblCPV = new JLabel("<html><font color=\"blue\">Código CPV</font>: " + licitacion.getListaCPV()+"</html>");
		this.lblCPV.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.lblCPV.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(lblCPV);
		
		this.lblTipoDoc = new JLabel("<html><font color=\"blue\">Tipo de Documento</font>: " + licitacion.getTipoDocumento()+"</html>");
		this.lblTipoDoc.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.lblTipoDoc.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(lblTipoDoc);
		
		
		this.lblEntidadEmisora = new JLabel("<html><font color=\"blue\">Entidad Emisora</font>: " + licitacion.getEntidadEmisora()+"</html>");
		this.lblEntidadEmisora.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.lblEntidadEmisora.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(lblEntidadEmisora);
		
		//SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YYYY");
		this.lblFechaPublicacion = new JLabel("<html><font color=\"blue\">Fecha Publicación</font>: " + licitacion.getFechaPublicacion()+"</html>");
		this.lblFechaPublicacion.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.lblFechaPublicacion.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(lblFechaPublicacion);
		
		this.lblLocalizacion = new JLabel("<html><font color=\"blue\">Localización</font>: " + licitacion.getLocalizacion().toString()+"</html>");
		this.lblLocalizacion.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.lblLocalizacion.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(lblLocalizacion);
		
		this.taResumen = new JTextArea();
		this.taResumen.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.taResumen.setFont(new Font("Dialog", Font.PLAIN, 10));
		this.taResumen.setEditable(false);
		this.taResumen.setOpaque(false);
		//TODO: Esto habrá que mejorarlo
		this.taResumen.setLineWrap(true);
		this.taResumen.setWrapStyleWord(true);
		//this.taResumen.setMaximumSize(new Dimension(400, 250));
		this.taResumen.setPreferredSize(new Dimension(400, 50));
		//this.taResumen.setColumns(60);
		this.taResumen.setText(licitacion.getResumen());
		
		JScrollPane scrollPane = new JScrollPane(this.taResumen);
		scrollPane.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
		this.add(scrollPane);

		
		//TODO: Esto hay que unificarlo con PanelResultado
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

		
	}
}
