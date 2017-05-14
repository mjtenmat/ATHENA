package delphos.iu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import delphos.DocumentoAcademico;
import delphos.DocumentoWeb;

public class PanelDocumentoAcademico extends JPanel {
	protected DocumentoAcademico docAcademico;
	protected JLabel lblTitulo;
	protected JLabel lblUrl;
	protected JLabel lblFecha;
	protected JLabel lblAutor;
	protected JLabel lblEntidad;
	protected JTextArea taResumen;
	private PanelVigilanciaController controlador;

	public PanelDocumentoAcademico(DocumentoAcademico docAcademico, PanelVigilanciaController controlador) {
		this.docAcademico = docAcademico;
		this.controlador = controlador;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		this.lblTitulo = new JLabel("<html><font color=\"blue\">Título:</font> " + docAcademico.getTitulo() + "</html>");
		this.lblTitulo.setHorizontalAlignment(SwingConstants.LEFT);
		this.lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.add(lblTitulo);

		this.lblUrl = new JLabel(docAcademico.getHref());
		this.lblUrl.setForeground(Color.GREEN);
		this.lblUrl.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.lblUrl.setHorizontalAlignment(SwingConstants.LEFT);
		this.lblUrl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.add(lblUrl);

		this.lblFecha = new JLabel("<html><font color=\"blue\">Fecha Publicación:</font> " + docAcademico.getFechaDisponibilidad()+ "</html>");
		add(lblFecha);
		this.lblAutor = new JLabel("<html><font color=\"blue\">Autor/es:</font> " + docAcademico.getAutor()+ "</html>");
		add(lblAutor);
		this.lblEntidad = new JLabel("<html><font color=\"blue\">Entidad:</font> " + docAcademico.getEntidad()+ "</html>");
		add(lblEntidad);

		if (docAcademico.getResumen() != null) {
			this.taResumen = new JTextArea();
			this.taResumen.setAlignmentX(Component.LEFT_ALIGNMENT);
			this.taResumen.setFont(new Font("Dialog", Font.PLAIN, 10));
			this.taResumen.setEditable(false);
			this.taResumen.setOpaque(false);
			// TODO: Esto habrá que mejorarlo
			this.taResumen.setLineWrap(true);
			this.taResumen.setWrapStyleWord(true);
			// this.taResumen.setMaximumSize(new Dimension(400, 250));
			this.taResumen.setPreferredSize(new Dimension(400, 50));
			// this.taResumen.setColumns(60);
			this.taResumen.setText(docAcademico.getResumen());
			this.taResumen.addMouseListener(this.controlador);

			JScrollPane scrollPane = new JScrollPane(this.taResumen);
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
						ProcessBuilder pb = new ProcessBuilder("/usr/bin/firefox", ((JLabel) e.getSource()).getText());
						Process p = pb.start();
					}
				} catch (Exception exc) {
					System.out.println("Error abriendo página web. " + exc.getMessage());
				}

			}
		});

	}
}
