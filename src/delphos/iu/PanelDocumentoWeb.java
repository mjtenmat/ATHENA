package delphos.iu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import delphos.DocumentoWeb;

public class PanelDocumentoWeb extends JPanel {

	private static final long serialVersionUID = 1L;
	protected final DocumentoWeb docWeb;
	protected JLabel lblTitulo;
	protected JLabel lblUrl;
	protected JTextArea taExtracto;
	private JLabel lblOtrosDatos;
	private JCheckBox cbRelevante;
	private PanelVigilanciaResultadosController controlador;

	public PanelDocumentoWeb(final DocumentoWeb docWeb, final PanelVigilanciaResultadosController controlador, boolean relevante) {
		this.docWeb = docWeb;
		this.controlador = controlador;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		JPanel panelHorizontal = new JPanel();
		panelHorizontal.setLayout(new BoxLayout(panelHorizontal, BoxLayout.X_AXIS));
		this.cbRelevante = new JCheckBox();
		// this.cbRelevante.setHorizontalAlignment(SwingConstants.LEFT);
		this.cbRelevante.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.cbRelevante.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (((JCheckBox) e.getSource()).isSelected())
					controlador.addRelevante(docWeb);
				else
					controlador.removeRelevante(docWeb);
				System.out.println("Añadido/Quitado resultado relevante.");
			}
		});
		this.cbRelevante.setSelected(relevante);
		this.cbRelevante.setEnabled(!relevante);

		panelHorizontal.add(cbRelevante);
		this.lblTitulo = new JLabel("<html> -<font color=\"blue\"> " + docWeb.getTitulo() + "</font></html>");
		// this.lblTitulo.setHorizontalAlignment(SwingConstants.LEFT);
		this.lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
		panelHorizontal.add(lblTitulo);
		panelHorizontal.setAlignmentX(LEFT_ALIGNMENT);
		this.add(panelHorizontal);

		this.lblUrl = new JLabel(docWeb.getDisplayUrl().toString());
		this.lblUrl.setForeground(Color.GREEN);
		this.lblUrl.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.lblUrl.setHorizontalAlignment(SwingConstants.LEFT);
		this.lblUrl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.add(lblUrl);

		lblOtrosDatos = new JLabel(
				docWeb.getLocalizacion() + " - " + docWeb.getSector() + " - " + docWeb.getTipoOrgnizacion());
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
			this.taExtracto.addMouseListener(this.controlador.panelVigilanciaResultados.controllerVigilancia);

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
						// ProcessBuilder pb = new ProcessBuilder("/usr/bin/firefox", ((JLabel)
						// e.getSource()).getText());
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
