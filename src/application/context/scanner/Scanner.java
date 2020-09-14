package application.context.scanner;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class Scanner {

	
	//key - filename
	//value - relative path
	public static Map<String, String> getAllFilesInProject(String path) throws IOException {
		HashMap<String, String> result = new HashMap<>();
		Properties properties = new Properties();
		properties.load(Scanner.class.getClassLoader().getResourceAsStream("configuration.properties"));
		path+="WEB-INF/classes"+properties.getProperty("rootScanDirectory");
		Collection<File> files = FileUtils.listFiles(new File(path), new String[] {"class"}, true);
		for(File file : files) {
			String relativePath = file.getAbsolutePath().split("classes")[1].substring(1).replace("\\", ".");
			result.put(file.getName().split(".class")[0], relativePath.split(".class")[0]);
		}
		return result;
	}

}
