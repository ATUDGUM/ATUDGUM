package samplesetmaker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

import analyzer.ChangeTypeAnalyzer;

public class MakeSampleSet {
	public static String path = "D:\\desktop\\sample\\";
	
	public static StringBuilder makeExample(String bp, String ap, String bt, String at) throws IOException {
		try {
			String filename = "";
			for (int i = bp.length()-1; i >= 0; i--) {
				if (bp.charAt(i) == '\\' || bp.charAt(i) == '/'){
					filename = bp.substring(i+1);
				}
			}
			
			StringBuilder example = new StringBuilder();
			example.append("###\r\n" + "\r\n" + "EXAMPLE INPUTS\r\n" + "\r\n");
			
			example.append("[Production Code Changes]:\r\n" + "\"");
			List<String> original = Files.readAllLines(new File(bp).toPath());
			List<String> revised = Files.readAllLines(new File(ap).toPath());
	
			Patch<String> patch = DiffUtils.diff(original, revised);
			        
			int contextSize = 10000;
			List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(filename, filename, original, patch, contextSize);
			        
			for (int i =0; i < unifiedDiff.size(); i++) {
				example.append(unifiedDiff.get(i) + "\r\n");
			}	
			example.append("\"\r\n" + "\r\n");
			
			example.append("[Old Test Code]:" + "\r\n" + "\"");
			List<String> originaltest = Files.readAllLines(new File(bt).toPath());
			for (int i =0; i < originaltest.size(); i++) {
				example.append(originaltest.get(i) + "\r\n");
			}	
			example.append("\"\r\n" + "\r\n");
			
			example.append("[Types of Changes in the Production Code that make the Old Test Code obsolete]:" + "\r\n" + "\"");
	        
	        ChangeTypeAnalyzer anl = new ChangeTypeAnalyzer(bp, ap, bt);
	        List<String> changesReasonsTypes = anl.getChangeTypes();
	        List<String> crt = new ArrayList<>();
	        
	        for (int i = 0; i < changesReasonsTypes.size(); i++) {
	        	crt.add(changesReasonsTypes.get(i));
	        }
	        
	        for (int i = 0; i < crt.size(); i++){
	        	example.append((i+1) + "." + crt.get(i) + "\r\n");
	        }
	        example.append("\"\r\n" + "\r\n");
			
			example.append("###\r\n" + "\r\n" + "EXAMPLE OUTPUTS\r\n" + "\r\n");
			
			example.append("[New Test Code]:\r\n" + "\"");
			List<String> revisedtest = Files.readAllLines(new File(at).toPath());
			for (int i =0; i < revisedtest.size(); i++) {
				example.append(revisedtest.get(i) + "\r\n");
			}	
			example.append("\"\r\n" + "\r\n");
			
			example.append("###\r\n" + "\r\n");
			
			return example;
		}catch(Exception e) {
			System.out.print(e);
			return null;
		}
	}
	
	public static void writeToExampleFile(String filename, String testfilename) throws IOException {
		try {
			String name = filename.substring(0, filename.lastIndexOf("."));
			
	        String bp = path + "old\\" + filename;
	    	String ap = path + "new\\" + filename;
	    	String bt = path + "old\\" + testfilename;
	    	String at = path + "new\\" + testfilename;
	        
	    	StringBuilder sb = makeExample(bp, ap, bt, at);
	    	
	    	ChangeTypeAnalyzer anl = new ChangeTypeAnalyzer(bp, ap, bt);
	        List<String> changesReasonsTypes = anl.getChangeTypes();
	        
	        ArrayList<String> AllTypes = new ArrayList<>();
	        for (int i = 0; i < changesReasonsTypes.size(); i++) {
	        	String type = changesReasonsTypes.get(i).substring(1, changesReasonsTypes.get(i).indexOf("]"));
	        	AllTypes.add(type);
	        }
	        
	        LinkedHashSet<String> set = new LinkedHashSet<>(AllTypes);
	        
	        List<String> Types = new ArrayList<>(set);
	       
	        if (Types.size() == 0){
	        	String filePath = path + "examples\\" + "Nothing" + "\\" + name + ".txt";
	            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
	                writer.write(sb.toString());  
	                System.out.println("The content was successfully written to the file.");
	            } catch (IOException e) {
	                System.err.println("An error occurred while writing to a file: " + e.getMessage());
	            }
	        }
	        if (Types.size() == 1){
	        	 String filePath = path + "examples\\" + Types.get(0) + "\\" + name + ".txt";
	             try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
	                 writer.write(sb.toString()); 
	                 System.out.println("The content was successfully written to the file.");
	             } catch (IOException e) {
	                 System.err.println("An error occurred while writing to a file: " + e.getMessage());
	             }
	        }
	        if (Types.size() > 1) {
	        	String filePath = path + "examples\\" + "MultipleReasons" + "\\" + name + ".txt";
	            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
	                writer.write(sb.toString()); 
	                System.out.println("The content was successfully written to the file.");
	            } catch (IOException e) {
	                System.err.println("An error occurred while writing to a file: " + e.getMessage());
	            }
	        }
		}catch(Exception e) {
			System.out.print(e);
		}
	}
	
	private static boolean checkForFile(String directoryPath, String fileName) {
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            return paths
                    .filter(Files::isRegularFile)  
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .findAny() 
                    .isPresent(); 
        } catch (IOException e) {
            e.printStackTrace();
            return false; 
        }
    }
	
	public static void main(String[] args) throws IOException {
        String folderPath = path + "old";

        File folder = new File(folderPath);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles(); 
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                    	if (!file.getName().endsWith("Test.java")){
                    		String filename = file.getName();
                    		String testfilename = filename.replace(".java", "Test.java");
                    		String examplename = filename.replace(".java", ".txt");
                    		String exampleFolderPath = path + "examples";
                    		boolean exists = checkForFile(exampleFolderPath, examplename);
                            if (exists) {
                                System.out.println("The file " + examplename + " already exists");
                            } else {
                            	writeToExampleFile(filename, testfilename);
                            }
                    	}
                    }
                }
            } else {
                System.err.println("The folder is empty or inaccessible.");
            }
        } else {
            System.err.println("The path is not a folder.");
        }
	}

}
