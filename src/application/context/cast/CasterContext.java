package application.context.cast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import application.context.ApplicationContext;

public class CasterContext {

	private static List<Class<Caster>> castingStrategies = new ArrayList<>();
	
	public static void addCaster(Class<Caster> caster) {
		castingStrategies.add(caster);
	}
	
	private static List<Class<Caster>> getCasterClasses(){
		return castingStrategies;
	}
	
	public static List<Caster> getCasters(){
		return getCasterClasses().stream().map(casterClass->(Caster)ApplicationContext.getInstance(casterClass)).collect(Collectors.toList());
	}
	
}
