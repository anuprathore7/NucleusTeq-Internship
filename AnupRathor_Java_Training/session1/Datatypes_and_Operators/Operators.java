import java.util.Scanner;

public class Operators {
    public static void main(String[] args){
        Scanner Sc = new Scanner(System.in);

        System.out.println("Enter the first number : ");
        int num1 = Sc.nextInt();
        System.out.println("Enter the second number : ");
        int num2 = Sc.nextInt();

        //Arithmetic Operators
        System.out.println("The sum of the two numbers is : " + (num1 + num2));
        System.out.println("The difference of the two numbers is : " + (num1 - num2));
        System.out.println("The product of the two numbers is : " + (num1 * num2));
        System.out.println("The quotient of the two numbers is : " + (num1 / num2));
        System.out.println("The remainder of the two numbers is : " + (num1 % num2));   
        System.out.println();

        //Relational Operators
        System.out.println("Is num1 greater than num2? " + (num1 > num2));
        System.out.println("Is num1 less than num2? " + (num1 < num2));
        System.out.println("Is num1 equal to num2? " + (num1 == num2));
        System.out.println("Is num1 not equal to num2? " + (num1 != num2));
        System.out.println("Is num1 greater than or equal to num2? " + (num1 >= num2));
        System.out.println("Is num1 less than or equal to num2? " + (num1 <= num2));
        System.out.println();
        

        //Logical Operators
        System.out.println("num1 > 0 AND num2 < 50: " + (num1 > 0 && num2 < 50));
        System.out.println("num1 < 20 OR num2 > 0: " + (num1 < 20 || num2 > 0));
        System.out.println("NOT (num1 > 0): " + !(num1 > 0));

    }
    
}
