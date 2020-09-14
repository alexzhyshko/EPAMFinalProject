package application.context.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import application.context.annotation.mapping.RequestType;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Mapping {

	public RequestType requestType();
	public String route();
	
}
