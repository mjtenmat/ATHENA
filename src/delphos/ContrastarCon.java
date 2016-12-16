package delphos;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Session;

import delphos.iu.Delphos;

@Entity
public class ContrastarCon extends Jerarquia<ContrastarCon> {
	
	public ContrastarCon(){
		super(ContrastarCon.class);
	}
}
