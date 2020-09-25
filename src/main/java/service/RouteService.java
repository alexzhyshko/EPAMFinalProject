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
		String departureLatitude = departurePoint.latitude;
		String departureLongitude = departurePoint.longitude;
		String destinationLatitude = destinationPoint.latitude;
		String destinationLongitude = destinationPoint.longitude;
		String queryString = "https://api.mapbox.com/directions/v5/mapbox/driving?access_token=" + apiKey;
		StringBuilder queryBodyBuilder = new StringBuilder();
		queryBodyBuilder.append(departureLongitude).append(",").append(departureLatitude).append(";")
				.append(destinationLongitude).append(",").append(destinationLatitude);
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(queryString);
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("coordinates", queryBodyBuilder.toString()));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			CloseableHttpResponse response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			RoutesApiResponse responseObject = gson.fromJson(responseString, RoutesApiResponse.class);
			Route result = new Route();
			responseObject.routes.sort((o1, o2)->Integer.compare((int)o1.duration, (int)o1.duration));
			result.distance = responseObject.routes.get(0).distance/1000;
			result.time = (int)responseObject.routes.get(0).duration/60;
			result.departure = departurePoint;
			result.destination = destinationPoint;
			client.close();
			return Optional.of(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

}
