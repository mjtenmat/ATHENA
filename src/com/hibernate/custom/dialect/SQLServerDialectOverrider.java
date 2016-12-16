package com.hibernate.custom.dialect;

import java.sql.Types;

import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.type.StandardBasicTypes;

public class SQLServerDialectOverrider extends SQLServerDialect {
	
	public SQLServerDialectOverrider(){
		super();
		registerHibernateType(Types.NVARCHAR, StandardBasicTypes.STRING.getName());
		registerHibernateType(Types.LONGNVARCHAR, StandardBasicTypes.STRING.getName());
	}
}
