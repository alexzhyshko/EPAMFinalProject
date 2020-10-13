package application.utils;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;

import com.google.gson.Gson;

import application.context.annotation.component.Component;
import application.context.annotation.inject.Inject;
import application.context.cast.Caster;
import application.context.cast.CasterContext;
import main.java.service.LocalizationService;

@Component
public class HttpUtils {

	@Inject
	private static LocalizationService localizator;
	
	private static Gson gson = new Gson();
	
	public static <T> Optional<T> parseBody(HttpServletRequest req, Class<T> parseTo) {
		try {
			String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			T result = gson.fromJson(body, parseTo);
			return Optional.of(result);
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	public static <T> boolean setResponseBody(HttpServletResponse resp, T payload, ContentType contentType, int responseStatus, String charset) {
		try {
			resp.setContentType(contentType.toString()+";charset="+charset);
			resp.setStatus(responseStatus);
			resp.getWriter().append(gson.toJson(payload)).flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static <T> T parseInputParameter(HttpServletRequest req, String parameterName, Class<T> type) {
		String parameter = req.getParameter(parameterName);
		if(parameter == null) {
			throw new IllegalArgumentException("Incorrect parameter");
		}
		T result = null;
		for(Caster caster : CasterContext.getCasters()) {
			try {
				result = (T)caster.cast(parameter, type);
			}catch(ClassCastException e) {}
		}
		if(result == null) {
			throw new UnsupportedOperationException("Unsupported parameter type. Please add your custom caster for this type");
		}
		return result;
	}
	
	public static String parseAuthHeader(String authHeader) {
		return authHeader.substring(7);
	}
	
	
}
