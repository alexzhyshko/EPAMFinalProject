package application.context.cast.impl;

import java.util.UUID;

import application.context.annotation.component.Component;
import application.context.cast.Caster;


@Component
public class UUIDCaster implements Caster{

	@Override
	public UUID cast(String str, Class type) {
		if(type != UUID.class) {
			throw new ClassCastException();
		}
		return UUID.fromString(str);
	}

}
