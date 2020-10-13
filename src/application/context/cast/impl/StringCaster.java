package application.context.cast.impl;

import application.context.annotation.component.Component;
import application.context.cast.Caster;

@Component
public class StringCaster implements Caster{

	@Override
	public String cast(String str, Class type) {
		if(type != String.class) {
			throw new ClassCastException();
		}
		return str;
	}

}
