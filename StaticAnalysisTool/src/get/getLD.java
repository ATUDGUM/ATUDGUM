package get;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class getLD {
	public static int getLd(String filePath1, String filePath2) throws IOException {
        String code1 = new String(Files.readAllBytes(Paths.get(filePath1)));
        String code2 = new String(Files.readAllBytes(Paths.get(filePath2)));

        LevenshteinDistance distance = new LevenshteinDistance();
        int result = distance.apply(code1, code2);
        
        return result;
	}
}

