import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// Implement a simple file I/O operation to read data from a text file. 

public class FileRead {
    public static void main(String[] args) {
        String filePath = "AnupRathor_Java_Training/session1/AdvancedTopics/dummy.txt"; // Specifies the file path

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }
}