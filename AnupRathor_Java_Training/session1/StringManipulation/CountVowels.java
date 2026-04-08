//Implement a function to count the number of vowels in a string. 
import java.util.Scanner;

public class CountVowels {

    public static int countVowels (String str){
        int count = 0;
        str = str.toLowerCase(); // Convert the string to lowercase for case-insensitive comparison

        for (int i =0; i < str.length(); i++){
            char ch = str.charAt(i);
            if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u'){
                count++;
            }
        }
        return count;
    }
    public static void main(String[] args) {
        Scanner Sc = new Scanner(System.in);

        System.out.println("Enter a string to count the number of vowels:");
        String inputString = Sc.nextLine();

        int VowelCount = countVowels(inputString);
        System.out.println("Number of vowels in the string: " + VowelCount);
    }
    
}
