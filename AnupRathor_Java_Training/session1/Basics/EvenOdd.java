
// que : Create a program to check if a number is even or odd.
import java.util.Scanner;

public class EvenOdd {

    public static void main(String[] args) {
        Scanner Sc = new Scanner(System.in);
        System.out.print("Enter a number : ");
        int num = Sc.nextInt();

        if (num % 2 == 0) {
            System.out.println("The number is even.");
        } else {
            System.out.println("The number is odd.");
        }

    }

}
