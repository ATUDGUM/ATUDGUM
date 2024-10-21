package promptmaker;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

public class PromptWithoutTypesMake {
	public static String path = "D:\\desktop\\testset\\";
	public static String oldpath = "\\old";
	public static String newpath = "\\new";
	public static String promptName = "PromptWithoutType.txt";
	public static String jsonFilePath = "D:\\desktop\\gen\\test.json";
	
	public static StringBuilder getInput(String bp, String ap, String bt, String at) throws IOException {
		String filename = "";
		for (int i = bp.length()-1; i >= 0; i--) {
			if (bp.charAt(i) == '\\' || bp.charAt(i) == '/'){
				filename = bp.substring(i+1);
			}
		}
		
		StringBuilder input = new StringBuilder();
		input.append("<<<\r\n");
		
		input.append("[Production Code Changes input]:\r\n" + "\"");
		List<String> original = Files.readAllLines(new File(bp).toPath());
		List<String> revised = Files.readAllLines(new File(ap).toPath());

		Patch<String> patch = DiffUtils.diff(original, revised);
				        
		int contextSize = 10000;
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(filename, filename, original, patch, contextSize);
		
				        
		for (int i =0; i < unifiedDiff.size(); i++) {
			input.append(unifiedDiff.get(i) + "\r\n");
		}	
		input.append("\"\r\n" + "\r\n");
		
		input.append("[Old Test Code input]:\r\n" + "\"");
		List<String> revisedtest = Files.readAllLines(new File(bt).toPath());
		for (int i =0; i < revisedtest.size(); i++) {
			input.append(revisedtest.get(i) + "\r\n");
		}	
		input.append("\"\r\n" + ">>>");
		return input;
	}
	
	public static void main(String[] args) throws IOException, PatchFailedException {
		try {
            FileReader reader = new FileReader(jsonFilePath);

            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(reader);

            JsonArray jsonArray = jsonElement.getAsJsonArray();
            
            for (int i = 1; i <= 520; i++) {
            	JsonElement element = jsonArray.get(i-1);
                JsonObject jsonObject = element.getAsJsonObject();
                
                int lastSlashIndex = 0;
                
                JsonArray focalDbArray = jsonObject.getAsJsonArray("focal_db");
                String old_p_name = focalDbArray.get(5).getAsString();
                String new_p_name = focalDbArray.get(7).getAsString();
                lastSlashIndex = old_p_name.lastIndexOf("/");
                old_p_name = old_p_name.substring(lastSlashIndex + 1);
                lastSlashIndex = new_p_name.lastIndexOf("/");
                new_p_name = new_p_name.substring(lastSlashIndex + 1);
                
                JsonArray testDbArray = jsonObject.getAsJsonArray("test_db");
                String old_t_name = testDbArray.get(5).getAsString();
                String new_t_name = testDbArray.get(7).getAsString();
                lastSlashIndex = old_t_name.lastIndexOf("/");
                old_t_name = old_t_name.substring(lastSlashIndex + 1);
                lastSlashIndex = new_t_name.lastIndexOf("/");
                new_t_name = new_t_name.substring(lastSlashIndex + 1);
                
                String oldfilepath = path + String.valueOf(i) + oldpath;
                String newfilepath = path + String.valueOf(i) + newpath;
                
                String folderPath = path + String.valueOf(i);
             
	            String bp = oldfilepath + "\\" + old_p_name;
	            String ap = newfilepath + "\\" + new_p_name;
	            String bt = oldfilepath + "\\" + old_t_name;
	            String at = newfilepath + "\\" + new_t_name;
	                      
	            StringBuilder prompt = new StringBuilder();
                  	
	            StringBuilder message = new StringBuilder();
	              		
	            message.append("Please revise the old test code provided in <<input>> to accommodate changes in the production code, and output the complete updated test code without any additional text.\r\n\r\n");
//	            message.append("Please revise the old test code provided in <<input>> to accommodate changes in the production code, and output the updated test methods that you changed without any additional text.\r\n\r\n");
        		
	            StringBuilder input = getInput(bp, ap, bt, at);
	              		
	            prompt.append(message.toString());
	              		
	            prompt.append(input.toString());
	              		
	            File promptNoTypeFile = new File(folderPath, promptName);
	            	
	            try { 
	            	FileWriter writer = new FileWriter(promptNoTypeFile);
		            writer.write(prompt.toString());  
		            writer.close();  
		                        
		            System.out.println("The content was successfully written to the file: " + promptNoTypeFile.getAbsolutePath());
		                        
		            } catch (IOException e) {
		            	System.out.println("An error has occurred.");
		                e.printStackTrace();
		             	}
	            } 
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
