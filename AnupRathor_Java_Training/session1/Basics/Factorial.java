//  que : Implement a program to find the factorial of a given number.

import java.util.Scanner;

public class Factorial {

    public static int factorial(int num) {
        int result = 1;
        for (int i = 1; i <= num; i++) {
            result *= i;
        }
        return result;

    }

    public static void main(String[] args) {
        Scanner Sc = new Scanner(System.in);
        System.out.print("Enter a number : ");
        int n = Sc.nextInt();

        System.out.println(factorial(n));
    }
}
