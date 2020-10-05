package main.java.service;

import java.io.IOException;
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

import application.context.annotation.Component;
import application.context.reader.PropertyReader;
import main.java.dto.response.routes.RoutesApiResponse;
import main.java.entity.Coordinates;
import main.java.entity.Route;

@Component
public class RouteService {

	private PropertyReader propertyReader = new PropertyReader();

	private Gson gson = new Gson();

	private String apiKey;

	public RouteService() {
		this.apiKey = propertyReader.getProperty("mapbox.apikey");
		if (this.apiKey == null) {
			throw new NullPointerException("No property for Mapbox api key found");
		}
	}

	public Optional<Route> tryGetRoute(Coordinates departurePoint, Coordinates destinationPoint) {
		String departureLatitude = departurePoint.getLatitude();
		String departureLongitude = departurePoint.getLongitude();
		String destinationLatitude = destinationPoint.getLatitude();
		String destinationLongitude = destinationPoint.getLongitude();
		String queryString = "https://api.mapbox.com/directions/v5/mapbox/driving?access_token=" + apiKey;
		StringBuilder queryBodyBuilder = new StringBuilder();
		queryBodyBuilder.append(departureLongitude).append(",").append(departureLatitude).append(";")
				.append(destinationLongitude).append(",").append(destinationLatitude);
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(queryString);
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("coordinates", queryBodyBuilder.toString()));
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			CloseableHttpResponse response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			RoutesApiResponse responseObject = gson.fromJson(responseString, RoutesApiResponse.class);
			Route result = new Route();
			responseObject.getRoutes()
					.sort((o1, o2) -> Integer.compare((int) o1.getDuration(), (int) o1.getDuration()));
			result.distance = responseObject.getRoutes().get(0).getDistance() / 1000;
			result.time = (int) responseObject.getRoutes().get(0).getDuration() / 60;
			result.departure = departurePoint;
			result.destination = destinationPoint;
			return Optional.of(result);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return Optional.empty();
	}

}
