package application.context.cast.impl;

import application.context.annotation.component.Component;
import application.context.cast.Caster;

@Component
public class IntegerCaster implements Caster{

	@Override
	public Integer cast(String str, Class type) {
		if(type != Integer.class) {
			throw new ClassCastException();
		}
		return Integer.valueOf(str);
	}

}
