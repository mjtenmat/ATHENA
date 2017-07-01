package delphos.iu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import delphos.DocumentoWeb;

public class PanelDocumentoWeb extends JPanel {
	protected final DocumentoWeb docWeb;
	protected JLabel lblTitulo;
	protected JLabel lblUrl;
	protected JTextArea taExtracto;
	private JLabel lblOtrosDatos;
	private PanelVigilanciaController controlador;

	public PanelDocumentoWeb(final DocumentoWeb docWeb, PanelVigilanciaController controlador) {
		this.docWeb = docWeb;
		this.controlador = controlador;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		this.lblTitulo = new JLabel("<html><font color=\"blue\">Título:</font> " + docWeb.getTitulo() + "</html>");
		this.lblTitulo.setHorizontalAlignment(SwingConstants.LEFT);
		this.lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.add(lblTitulo);

		this.lblUrl = new JLabel(docWeb.getDisplayUrl().toString());
		this.lblUrl.setForeground(Color.GREEN);
		this.lblUrl.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.lblUrl.setHorizontalAlignment(SwingConstants.LEFT);
		this.lblUrl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.add(lblUrl);

		lblOtrosDatos = new JLabel(docWeb.getLocalizacion() + " - " + docWeb.getSector() + " - " + docWeb.getTipoOrgnizacion());
		add(lblOtrosDatos);

		if (docWeb.getExtracto() != null) {
			this.taExtracto = new JTextArea();
			this.taExtracto.setAlignmentX(Component.LEFT_ALIGNMENT);
			this.taExtracto.setFont(new Font("Dialog", Font.PLAIN, 10));
			this.taExtracto.setEditable(false);
			this.taExtracto.setOpaque(false);
			// TODO: Esto habrá que mejorarlo
			this.taExtracto.setLineWrap(true);
			this.taExtracto.setWrapStyleWord(true);
			// this.taResumen.setMaximumSize(new Dimension(400, 250));
			this.taExtracto.setPreferredSize(new Dimension(400, 50));
			// this.taResumen.setColumns(60);
			this.taExtracto.setText(docWeb.getExtracto());
			this.taExtracto.addMouseListener(this.controlador);

			JScrollPane scrollPane = new JScrollPane(this.taExtracto);
			scrollPane.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
			this.add(scrollPane);
		}

		// TODO: Esto hay que unificarlo con PanelResultado
		this.lblUrl.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					// open the default web browser for the HTML page
					Desktop desktop = Desktop.getDesktop();
					if ((desktop.isSupported(Desktop.Action.BROWSE))) {
						Desktop.getDesktop().browse(java.net.URI.create(((JLabel) e.getSource()).getText()));
					} else {
						//ProcessBuilder pb = new ProcessBuilder("/usr/bin/firefox", ((JLabel) e.getSource()).getText());
						ProcessBuilder pb = new ProcessBuilder("/usr/bin/firefox", docWeb.getUrl().toString());
						Process p = pb.start();
					}
				} catch (Exception exc) {
					System.out.println("Error abriendo página web. " + exc.getMessage());
				}

			}
		});

	}
}
