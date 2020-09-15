package application.context.scanner;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import application.context.annotation.Component;
import application.context.reader.PropertyReader;

@Component
public class Scanner {

	private static PropertyReader propertyReader = new PropertyReader();
	
	//key - filename
	//value - relative path
	public static Map<String, String> getAllFilesInProject(String path) throws IOException {
		HashMap<String, String> result = new HashMap<>();
		path+="WEB-INF/classes"+propertyReader.getProperty("rootScanDirectory");
		Collection<File> files = FileUtils.listFiles(new File(path), new String[] {"class"}, true);
		for(File file : files) {
			String relativePath = file.getAbsolutePath().split("classes")[1].substring(1).replace("\\", ".");
			result.put(file.getName().split(".class")[0], relativePath.split(".class")[0]);
		}
		return result;
	}

}
