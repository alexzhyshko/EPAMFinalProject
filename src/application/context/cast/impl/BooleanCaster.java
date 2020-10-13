package application.context.cast.impl;

import application.context.annotation.component.Component;
import application.context.cast.Caster;

@Component
public class BooleanCaster implements Caster{

	@Override
	public Boolean cast(String str, Class type) {
		if(type != Boolean.class) {
			throw new ClassCastException();
		}
		return Boolean.valueOf(str.equalsIgnoreCase("true"));
	}

}
