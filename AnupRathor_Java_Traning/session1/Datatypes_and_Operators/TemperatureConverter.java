
import java.util.Scanner;

public class TemperatureConverter {

    public static double toFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    public static double toCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    public static void main(String[] args) {

        Scanner Sc = new Scanner(System.in);

        System.out.println("Choose conversion:");
        System.out.println("1. Celsius to Fahrenheit");
        System.out.println("2. Fahrenheit to Celsius");

        int choice = Sc.nextInt();

        switch (choice) {

            case 1:
                System.out.print("Enter temperature in Celsius: ");
                double c = Sc.nextDouble();
                System.out.println("Fahrenheit = " + toFahrenheit(c));
                break;

            case 2:
                System.out.print("Enter temperature in Fahrenheit: ");
                double f = Sc.nextDouble();
                System.out.println("Celsius = " + toCelsius(f));
                break;

            default:
                System.out.println("Invalid choice");
        }

       
    }
}