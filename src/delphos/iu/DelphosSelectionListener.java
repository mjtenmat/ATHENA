package delphos.iu;

public interface DelphosSelectionListener {
	
	void onAceptar(DelphosSelectionDialog dtd);
	void onCancelar(DelphosSelectionDialog dtd);
	void onBorrar(DelphosSelectionDialog dtd);
	void onCrear(DelphosSelectionDialog dtd);
}
