package main.java.utils;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;

import com.google.gson.Gson;

import application.context.annotation.Component;
import application.context.annotation.Inject;
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

	public static <T> boolean setResponseBody(HttpServletResponse resp, T payload, ContentType contentType, int responseStatus) {
		try {
			resp.setContentType(contentType.toString());
			resp.setStatus(responseStatus);
			resp.getWriter().append(gson.toJson(payload)).flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static synchronized <T> T parseInputParameter(HttpServletRequest req, String parameterName, String userLocale, Class<T> type) {
		try {
			if(type==String.class) {
				return (T)req.getParameter(parameterName);
			}
			if(type==Integer.class) {
				return (T)Integer.valueOf(req.getParameter(parameterName));
			}
			if(type==UUID.class) {
				return (T)UUID.fromString(req.getParameter(parameterName));
			}
			throw new UnsupportedOperationException(type+" is not supported");
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(localizator.getPropertyByLocale(userLocale, "incorrectParameter"));
		}
	}
	
}
