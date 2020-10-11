package main.java.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import application.context.annotation.component.Component;
import application.context.scanner.Scanner;

@Component
public class LocalizationService {

	private Map<String, Properties> locales = new HashMap<>();
	
	public LocalizationService() {
		try {
			Properties propertiesEN = new Properties();
			propertiesEN.load(Scanner.class.getClassLoader().getResourceAsStream("translation_EN.properties"));
			Properties propertiesUA = new Properties();
			propertiesUA.load(Scanner.class.getClassLoader().getResourceAsStream("translation_UA.properties"));
			Properties propertiesRU = new Properties();
			propertiesRU.load(Scanner.class.getClassLoader().getResourceAsStream("translation_RU.properties"));
			locales.put("EN", propertiesEN);
			locales.put("UA", propertiesUA);
			locales.put("RU", propertiesRU);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getPropertyByLocale(String locale, String key) {
		return this.locales.get(locale).getProperty(key);
	}
}
