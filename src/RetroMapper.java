package src;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.SystemUtils;

public class RetroMapper {
	
	private static String baseLocalURL;
	private static void print(String s) {
		System.out.println(s);
	}
	
	public static void main(String[] args) {
		RetroMapper mapper = new RetroMapper();
		
		baseLocalURL = getOSPath(System.getProperty("user.dir"));
		print("Output can be found at:\n" + baseLocalURL);
		try {
			mapper.map();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getJSONFromStartRecord(CSVRecord record) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"key_retro\":\"" + record.get(1) + "\"");
		sb.append(",\"name\":\"" + record.get(2) + "\"");
		sb.append(",\"team\":" + record.get(3));
		sb.append(",\"order\":" + record.get(4));
		sb.append(",\"position\":" + record.get(5));
		sb.append("}");
		return sb.toString();
	}
	private String getJSONFromSubRecord(CSVRecord record) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"sub\":{");
		sb.append("\"key_retro\":\"" + record.get(1) + "\"");
		sb.append(",\"name\":\"" + record.get(2) + "\"");
		sb.append(",\"team\":" + record.get(3));
		sb.append(",\"order\":" + record.get(4));
		sb.append(",\"position\":" + record.get(5));
		sb.append("}}");
		return sb.toString();
	}
	private String getJSONFromPlayRecord(CSVRecord record) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"play\":{");
		sb.append("\"inning\":" + record.get(1));
		sb.append(",\"team\":" + record.get(2));
		sb.append(",\"batter\":\"" + record.get(3) + "\"");
		if (!record.get(4).contains("?") && record.get(4).length() > 0) {
			sb.append(",\"count\":{");
			sb.append("\"balls\":"+record.get(4).charAt(0));
			sb.append(",\"strikes\":"+record.get(4).charAt(1));
			sb.append("}");
		}
		sb.append(",\"pitches\":\"" + record.get(5) + "\"");
		sb.append(",\"event\":\"" + record.get(6) + "\"");
		sb.append("}}");
		return sb.toString();
	}
	private String getJSONFromDataRecord(CSVRecord record) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"type\":\"" + record.get(1) + "\"");
		sb.append(",\"key_retro\":\"" + record.get(2) + "\"");
		sb.append(",\"value\":" + record.get(3));
		sb.append("}");
		return sb.toString();
	}
	
	private void map() throws IOException, InterruptedException {
		String outDir = getOSPath(baseLocalURL + "\\out");
		String dataDir = getOSPath(baseLocalURL + "\\data");
		
		File dataDirFile = new File(dataDir);
		if (!dataDirFile.exists()) {
			dataDirFile = new File(getOSPath(baseLocalURL + "\\..\\data"));
		}
		
		print("Outputting to " + outDir);
		Thread.sleep(2000);
		Collection<File> events = FileUtils.listFiles(dataDirFile, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY);
		for (File file : events) {
			String ext = FilenameUtils.getExtension(file.getName());
			File outFile = new File(outDir + "\\" + file.getName().replace("."+ext, "") + ".json");
			if (ext.equals("ROS")) continue;
			else if (ext.length() == 0) continue;
			else {
				FileReader reader = new FileReader(file);
				CSVParser parser = CSVFormat.EXCEL.parse(reader);
				List<CSVRecord> records = parser.getRecords();
				StringBuilder sb = new StringBuilder();
				sb.append("{\"game\":[");
				Iterator<CSVRecord> it = records.iterator();
				int i = 0;
				if (!it.hasNext()) {
					continue;
				}
				CSVRecord record = it.next();
				while (it.hasNext()) {
					String key = record.get(0);
					if (key.equals("id")) {
						if (i > 0) {
							sb.append(",");
						}
						sb.append("{\"id\":\"" + record.get(1) + "\"");
						it.next();
						sb.append(",\"info\":{");
						int j = 0;
						while ((record=it.next()).get(0).equals("info")) {
							if (j++ > 0) sb.append(",");
							sb.append("\""+record.get(1)+"\":\""+record.get(2).replace('"', '\0')+"\"");
						}
						sb.append("},\"starts\":[");
						j = 0;
						while (record.get(0).equals("start")) {
							if (j++ > 0) sb.append(",");
							sb.append(getJSONFromStartRecord(record));
							record = it.next();
						}
						sb.append("]");
						
						j = 0;
						sb.append(",\"plays\":[");
						while (!record.get(0).equals("data")) {
							if (record.get(0).equals("play")) {
								if (j++ > 0) sb.append(",");
								sb.append(getJSONFromPlayRecord(record));
							}
							record = it.next();
						}
						sb.append("]");
						
						j = 0;
						sb.append(",\"data\":[");
						while(record.get(0).equals("data")) {
							if (j++ > 0) sb.append(",");
							sb.append(getJSONFromDataRecord(record));
							
							if (it.hasNext()) record = it.next();
							else break;
						}
						sb.append("]");
						
						sb.append("}");
					}
					else record = it.next();
					i++;
				}
				sb.append("]}");
				FileUtils.writeStringToFile(outFile, sb.toString());
				print("Finished writing " + outFile.getName());
				sb.setLength(0);
			}
		}
	}

	private static String getOSPath(String path) {
		if (SystemUtils.IS_OS_MAC) return path.replaceAll("\\", "/");
		else return path;
	}
}
