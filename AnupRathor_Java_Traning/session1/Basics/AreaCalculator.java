// que : Write a program to calculate the area of a circle, rectangle, or triangle based on user input.

import java.util.Scanner;

public class AreaCalculator {
    public static double AreaOfCircle(double radius) {
        return Math.PI * radius * radius;
    }

    public static double AreaOfRectangle(double length, double width) {
        return length * width;
    }

    public static double AreaOfTriangle(double base, double height) {
        return 0.5 * base * height;
    }

    public static void main(String[] args) {
        Scanner Sc = new Scanner(System.in);

        System.out.print("Choose Shape : circle / rectangle / triangle ->");
        String choice = Sc.nextLine();

        if (choice.equals("circle")) {
            System.out.println("Enter the radius of the circle:");
            double radius = Sc.nextDouble();
            System.out.println("Area of the circle is: " + AreaOfCircle(radius));

        }

        else if (choice.equals("rectangle")) {
            System.out.println("Enter the length of the rectangle:");
            double length = Sc.nextDouble();
            System.out.println("Enter the width of the rectangle:");
            double width = Sc.nextDouble();
            System.out.println("Area of the rectangle is: " + AreaOfRectangle(length, width));

        }

        else if (choice.equals("triangle")) {
            System.out.println("Enter the base of the triangle:");
            double base = Sc.nextDouble();
            System.out.println("Enter the height of the triangle:");
            double height = Sc.nextDouble();
            System.out.println("Area of the triangle is: " + AreaOfTriangle(base, height));

        }

        else {
            System.out.println("Invalid choice. Please choose circle, rectangle, or triangle.");
        }

    }
}