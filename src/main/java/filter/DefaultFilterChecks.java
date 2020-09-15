package main.java.filter;

import java.util.Arrays;
import java.util.List;

public class DefaultFilterChecks {

	private DefaultFilterChecks() {}
	
	private static List<String> exceptions = Arrays.asList("/login", "/register", "/refreshToken");
	
	private static String adminPaths = "/admin";
	
	/**
	 * Checks is the path given matches filter exceptions
	 */
	public static boolean checkFilterExceptions(String path) {
		return exceptions.contains(path);
	}
	
	/**
	 * Checks if input path matches the only-admin-accessible resources path
	 */
	public static boolean checkOnlyAdminResourcesPath(String path) {
		return path.startsWith(adminPaths);
	}
	
}
