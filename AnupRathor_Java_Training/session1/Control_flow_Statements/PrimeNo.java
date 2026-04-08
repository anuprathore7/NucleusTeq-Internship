
//Write a program to check if a given number is prime using an if-else statement.
import java.util.Scanner;

public class PrimeNo {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter a number to check whether it is prime or not:");

        int num = sc.nextInt();
        int count = 0;
        for (int i = 2; i <= num; i++) {
            if (num % i == 0) {
                count++;

            }

        }

        if (count == 1) {
            System.out.println(num + " is a prime number.");
        } else {
            System.out.println(num + " is not a prime number.");
        }

    }
}
