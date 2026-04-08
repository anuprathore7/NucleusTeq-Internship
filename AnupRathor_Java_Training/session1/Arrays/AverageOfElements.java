//1) Write a program to find the average of elements in an array.
import java.util.Scanner;

public class AverageOfElements {
    public static void main(String[] args) {
        Scanner Sc = new Scanner(System.in);
        System.out.print("Enter the Size of the array: ");
        int size = Sc.nextInt();

        int arr[] = new int[size];
        int sum =0;

        System.out.println("Enter the elements of the array: ");

        for (int i =0; i<size; i++){
            arr[i] = Sc.nextInt();
            sum += arr[i];
        }
         System.out.println("Average of elements of array is: " + (double) sum / arr.length);
        
        }
    }
    

