package promptmaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.text.similarity.LevenshteinDistance;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import analyzer.ChangeTypeAnalyzer;
import get.getLD;

public class PromptWithoutContextsMake {
	public static String datasetPath = "D:\\desktop\\testset\\";
	public static String oldpath = "\\old";
	public static String newpath = "\\new";
	
	public static String dataPath = "D:\\desktop\\sample\\";
	public static String examplesPath = "D:\\desktop\\sample\\examples\\";
	
	public static String jsonFilePath = "D:\\desktop\\gen\\test.json";
	
	public static List<String> extractChanges(String filePath) {
        StringBuilder changes = new StringBuilder();
        boolean capture = false;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("[Types of Changes in the Production Code that make the Old Test Code obsolete]:")) {
                    capture = true;
                }
                
                if (capture) {
                    if (line.contains("###")) {
                        break;
                    }
                    changes.append(line).append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        ArrayList<String> AllTypes = new ArrayList<>();
        String[] strArr = changes.toString().split("\n");
        
        for (int i = 1; i < strArr.length - 2; i++) {
        	String type = strArr[i].substring(strArr[i].indexOf("[")+1, strArr[i].indexOf("]"));
        	AllTypes.add(type);
        }
        
        LinkedHashSet<String> set = new LinkedHashSet<>(AllTypes);
        List<String> Types = new ArrayList<>(set);
        Collections.sort(Types);
        
        return Types;
    }
	
	public static String getBestExample(String filePath, String folderPath) throws IOException {
		File folder = new File(folderPath);
		int minLD = Integer.MAX_VALUE;
		StringBuilder result = new StringBuilder();
        if (folder.isDirectory()) {
        	File[] allFiles = folder.listFiles();
        	
        	List<File> filteredFiles = new ArrayList<>();
            if (allFiles != null) {
                for (File file : allFiles) {
                    if (file.isFile() && file.getName().endsWith(".txt")) {
                        filteredFiles.add(file);
                    }
                }
            } 
            allFiles = filteredFiles.toArray(new File[0]); 
            
            if (allFiles != null) {
                List<File> filesList = new ArrayList<>(Arrays.asList(allFiles));
                Collections.shuffle(filesList); 
                List<File> selectedFiles = new ArrayList<>();
                
                for (File file : filesList) {
                    if (file.isFile() && file.length() < 20480) {
                        selectedFiles.add(file);
                    }
                }
                
                selectedFiles = selectedFiles.subList(0, Math.min(selectedFiles.size(), 100));
   
	            if (selectedFiles != null) {
	                for (File file : selectedFiles) {
	                    if (file.isFile()) {
	                    	String javafilename = file.getName().replace(".txt", ".java");
	                    	String javafilepath = dataPath + "old\\" + javafilename;
	                    	if (!filePath.equals(javafilepath)){
	                    		int LD = getLD.getLd(filePath, javafilepath);
	                    		if (LD < minLD) {
	                    			minLD = LD;
	                    			result.delete(0, result.length());
	                    			result.append(file.getName());
	                    		}
	                    	}
	                    }
	                }
	            } 
	            else {
	                System.err.println("The folder is empty or inaccessible.");
	            }
            }
        } 
        else {
            System.err.println("The path is not a folder.");
        }
        return folderPath + "\\" + result.toString();
	}
	
	public static String getBestExample(String filePath, String folderPath, List<String> Types) throws IOException {
		File folder = new File(folderPath);
		int minLD = Integer.MAX_VALUE;
		StringBuilder result = new StringBuilder();
        if (folder.isDirectory()) {
        	File[] allFiles = folder.listFiles();
        	
        	List<File> filteredFiles = new ArrayList<>();
            if (allFiles != null) {
                for (File file : allFiles) {
                    if (file.isFile() && file.getName().endsWith(".txt")) {
                        filteredFiles.add(file); 
                    }
                }
            } 
            allFiles = filteredFiles.toArray(new File[0]); 
        	
            if (allFiles != null) {
                List<File> filesList = new ArrayList<>(Arrays.asList(allFiles));
                Collections.shuffle(filesList); 
                List<File> selectedFiles = new ArrayList<>();
                
                for (File file : filesList) {
                    if (file.isFile() && file.length() < 20480) {
                        selectedFiles.add(file);
                    }
                }
                
                selectedFiles = selectedFiles.subList(0, Math.min(selectedFiles.size(), 100));
                
	            if (selectedFiles != null) {
	                for (File file : selectedFiles) {
	                    if (file.isFile()) {
	                    	String javafilename = file.getName().replace(".txt", ".java");
	                    	String javafilepath = dataPath + "old\\" + javafilename;
	                    	
	                    	if (filePath.equals(javafilepath)) {
	                    		continue;
	                    	}
	                    	
	                    	List<String> t = extractChanges(folderPath + "\\" + file.getName());
	                    	if (t.equals(Types)) {
	                    		result.delete(0, result.length());
                    			result.append(file.getName());
                    			break;
	                    	}
	                    	else {
	                    		LevenshteinDistance distance = new LevenshteinDistance();
	                            int LD = distance.apply(t.toString(), Types.toString());
	                    		if (LD < minLD) {
	                    			minLD = LD;
	                    			result.delete(0, result.length());
	                    			result.append(file.getName());
	                    		}
	                    	}
	                    
	                    }
	                }
	            } 
	            else {
	                System.err.println("The folder is empty or inaccessible.");
	            }
            }
        } 
        else {
            System.err.println("The path is not a folder.");
        }
        return folderPath + "\\" + result.toString();
	}
	
	public static StringBuilder findExample(String bp, String ap, String bt) throws IOException {
		ChangeTypeAnalyzer anl = new ChangeTypeAnalyzer(bp, ap, bt);
		List<String> changesReasonsTypes = anl.getChangeTypes();
		
		ArrayList<String> AllTypes = new ArrayList<>();
        for (int i = 0; i < changesReasonsTypes.size(); i++) {
        	String type = changesReasonsTypes.get(i).substring(1, changesReasonsTypes.get(i).indexOf("]"));
        	AllTypes.add(type);
        }
        
        LinkedHashSet<String> set = new LinkedHashSet<>(AllTypes);
        
        List<String> Types = new ArrayList<>(set);
        Collections.sort(Types);
        
        StringBuilder targetfile = new StringBuilder();
        
        if (Types.size() == 0){
        	String examplePath = examplesPath + "Nothing";
        	targetfile.append(getBestExample(bp, examplePath));
        }
        else if (Types.size() == 1){
        	String examplePath = examplesPath + Types.get(0);
        	targetfile.append(getBestExample(bp, examplePath));
        }
        else if (Types.size() > 1){
        	String examplePath = examplesPath + "MultipleReasons";
        	targetfile.append(getBestExample(bp, examplePath, Types));
		}
        
        String examplePath = targetfile.toString();
		StringBuilder example = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(examplePath))) {
            String line;
            while ((line = br.readLine()) != null) {
            	example.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("An error occurred while reading the file: " + e.getMessage());
        }
		
		return example;
	}
	
	public static StringBuilder getInput(String bp, String ap, String bt) throws IOException {//获取Input
		String filename = "";
		for (int i = bp.length()-1; i >= 0; i--) {
			if (bp.charAt(i) == '\\' || bp.charAt(i) == '/'){
				filename = bp.substring(i+1);
			}
		}
		
		StringBuilder input = new StringBuilder();
		input.append("<<<\r\n");
		
		input.append("[Production Code Changes input]:\r\n" + "\"");
		List<String> original = Files.readAllLines(new File(bp).toPath(), StandardCharsets.UTF_8);
		List<String> revised = Files.readAllLines(new File(ap).toPath(), StandardCharsets.UTF_8);

		Patch<String> patch = DiffUtils.diff(original, revised);
				        
		int contextSize = 50;
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
		input.append("\"\r\n" + "\r\n");
				
		input.append("[Types of Changes in the Production Code that make the Old Test Code obsolete input]:" + "\r\n" + "\"");
		        
		ChangeTypeAnalyzer anl = new ChangeTypeAnalyzer(bp, ap, bt);
		List<String> changesReasonsTypes = anl.getChangeTypes();
		List<String> crt = new ArrayList<>();
		        
		for (int i = 0; i < changesReasonsTypes.size(); i++) {
		     crt.add(changesReasonsTypes.get(i));
		}
		        
		for (int i = 0; i < crt.size(); i++){
			input.append((i+1) + "." + crt.get(i) + "\r\n");
		}
		input.append("\"\r\n" + ">>>");
		
		return input;
	}
	
	public static void main(String[] args) throws IOException {
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
	                
	                String oldfilepath = datasetPath + String.valueOf(i) + oldpath;
	                String newfilepath = datasetPath + String.valueOf(i) + newpath;
	                
	                File oldDirectory = new File(oldfilepath);
	                File newDirectory = new File(newfilepath);
	                
	                File[] oldFiles = oldDirectory.listFiles();
	                File[] newFiles = newDirectory.listFiles();
	                
	                if (oldFiles.length != 2 || newFiles.length != 2) {
	                	System.out.println("folder" + i + "is empty");
	                	continue;
	                }
	                
	                String bp = oldfilepath + "\\" + old_p_name;
	                String ap = newfilepath + "\\" + new_p_name;
	                String bt = oldfilepath + "\\" + old_t_name;

	                StringBuilder prompt = new StringBuilder();
	            	
	        		StringBuilder message = new StringBuilder();
	        		message.append("Please revise the old test code provided in <<<input>>> to accommodate changes in the production code, and output the complete updated test case without any additional text.\r\n\r\n");
	        		
	     		
	        		
//make input------------------------------------------------------------------------------------------------------------------------
	        		
	        		StringBuilder input = new StringBuilder();
	        		
	                JsonObject jsonObject1 = element.getAsJsonObject();
	        		
	                String JsonString_focal_src = jsonObject1.get("focal_src").getAsString();
	                String JsonString_focal_tgt = jsonObject1.get("focal_tgt").getAsString();
	                String JsonString_test_src = jsonObject1.get("test_src").getAsString();
	                
	                JsonString_focal_src = JsonString_focal_src.replace("{", "{\n").replace(";", ";\n");
	                JsonString_focal_tgt = JsonString_focal_tgt.replace("{", "{\n").replace(";", ";\n");
	                JsonString_test_src = JsonString_test_src.replace("{", "{\n").replace(";", ";\n");
	                
	                List<String> oldCodeLines = Arrays.asList(JsonString_focal_src.split("\n"));
	                List<String> newCodeLines = Arrays.asList(JsonString_focal_tgt.split("\n"));
	                List<String> testCodeLines = Arrays.asList(JsonString_test_src.split("\n"));
	                
	                
	                Patch<String> patch = DiffUtils.diff(oldCodeLines, newCodeLines);

	                List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff("OldCode.java", "NewCode.java", oldCodeLines, patch, 100);
 
	                input.append("<<<\r\n");
	        		
	        		input.append("[Production Code Changes input]:\r\n" + "\"");
	                
	                for (int j =0; j < unifiedDiff.size(); j++) {
	        			input.append(unifiedDiff.get(j) + "\r\n");
	        		}	
	        		input.append("\"\r\n" + "\r\n");
	                
	        		input.append("[Old Test Code input]:\r\n" + "\"");
	        		for (int j =0; j < testCodeLines.size(); j++) {
	        			input.append(testCodeLines.get(j) + "\r\n");
	        			
	        		}	
	        		input.append("\"\r\n" + "\r\n");
	        		
	        		input.append("[Types of Changes in the Production Code that make the Old Test Code obsolete input]:" + "\r\n" + "\"");
	        		        
	        		ChangeTypeAnalyzer anl = new ChangeTypeAnalyzer(bp, ap, bt);
	        		List<String> changesReasonsTypes = anl.getChangeTypes();
	        		List<String> crt = new ArrayList<>();
	        		        
	        		for (int j = 0; j < changesReasonsTypes.size(); j++) {
	        			crt.add(changesReasonsTypes.get(j));
	        		}
	        		        
	        		for (int j = 0; j < crt.size(); j++){
	        			input.append((j+1) + "." + crt.get(j) + "\r\n");
	        		}
	        		input.append("\"\r\n" + ">>>");
	        		
	        		StringBuilder example = findExample(bp, ap, bt);
           
	        		prompt.append(message.toString());
	        		prompt.append(example.toString());
	        		prompt.append(input.toString());
	        		    		
	        		String filePath = datasetPath + String.valueOf(i) + "\\" + "PromptWithoutContext.txt";
	        		
	        		try (FileWriter writer = new FileWriter(filePath)) {
	                    writer.write(prompt.toString());
	                    System.out.println("The content was successfully written to the file: " + filePath);
	                } catch (IOException e) {
	                    System.out.println("An error occurred while writing to the file.");
	                    e.printStackTrace();
	                }
	               
	            }
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}

}
