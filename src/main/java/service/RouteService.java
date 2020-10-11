package main.java.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import application.context.annotation.component.Component;
import application.context.reader.PropertyReader;
import main.java.dto.response.routes.ApiRoute;
import main.java.dto.response.routes.RoutesApiResponse;
import main.java.entity.Coordinates;
import main.java.entity.Route;
import main.java.exception.RouteNotFoundException;

@Component
public class RouteService {

	private PropertyReader propertyReader = new PropertyReader();

	private String apiKey;
	private String queryString;
	private Gson gson = new Gson();

	public RouteService() {
		this.apiKey = propertyReader.getProperty("mapbox.apikey");
		this.queryString = "https://api.mapbox.com/directions/v5/mapbox/driving?access_token=" + apiKey;
		if (this.apiKey == null) {
			throw new NullPointerException("No property for Mapbox api key found");
		}
	}

	public Optional<Route> tryGetRoute(Coordinates departurePoint, Coordinates destinationPoint) {
		HttpEntity apiResponse = queryApi(departurePoint, destinationPoint)
				.orElseThrow(() -> new NullPointerException("Could not query MapBox api"));
		String responseString = parseEntityToString(apiResponse)
				.orElseThrow(() -> new NullPointerException("Could not parse entity to string"));
		RoutesApiResponse responseObject = gson.fromJson(responseString, RoutesApiResponse.class);
		ApiRoute shortestRoute = findShortestApiRoute(responseObject.getRoutes())
				.orElseThrow(() -> new RouteNotFoundException("Could not find shortest route"));
		Route result = buildRoute(departurePoint, destinationPoint, shortestRoute);
		return Optional.of(result);
	}

	private Optional<HttpEntity> queryApi(Coordinates departurePoint, Coordinates destinationPoint) {
		String coordinatesString = buildCoordinatesString(departurePoint, destinationPoint);
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(queryString);
			httpPost.setEntity(getUrlEncodedFormEntity(coordinatesString).orElseThrow(()->new NullPointerException("Could not create url encoded parameter")));
			CloseableHttpResponse response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			return Optional.of(entity);
		} catch (IOException e1) {
			e1.printStackTrace();
			return Optional.empty();
		}
	}

	private String buildCoordinatesString(Coordinates departurePoint, Coordinates destinationPoint) {
		String departureLatitude = departurePoint.getLatitude();
		String departureLongitude = departurePoint.getLongitude();
		String destinationLatitude = destinationPoint.getLatitude();
		String destinationLongitude = destinationPoint.getLongitude();
		StringBuilder queryParamBuilder = new StringBuilder();
		queryParamBuilder.append(departureLongitude).append(",").append(departureLatitude).append(";")
				.append(destinationLongitude).append(",").append(destinationLatitude);
		return queryParamBuilder.toString();
	}

	private Optional<UrlEncodedFormEntity> getUrlEncodedFormEntity(String paramValue) {
		try {
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("coordinates", paramValue));
			return Optional.of(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e) {
			return Optional.empty();
		}
	}

	private Optional<String> parseEntityToString(HttpEntity entity) {
		try {
			return Optional.of(EntityUtils.toString(entity, "UTF-8"));
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	private Optional<ApiRoute> findShortestApiRoute(List<ApiRoute> routes) {
		return routes.stream().sorted((o1, o2) -> Integer.compare((int) o1.getDuration(), (int) o1.getDuration()))
				.findFirst();
	}

	private Route buildRoute(Coordinates departurePoint, Coordinates destinationPoint, ApiRoute route) {
		return Route.builder().distance(route.getDistance() / 1000).time((int) route.getDuration() / 60)
				.departure(departurePoint).destination(destinationPoint).build();
	}

}
