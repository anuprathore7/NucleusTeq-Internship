
import java.util.Scanner;
public class LinearSearch {
    public static void main(String[] args) {
        Scanner Sc = new Scanner(System.in);
        System.out.print("Enter the size of the array: ");
        int size = Sc.nextInt();

        int arr[] = new int[size];

        System.out.println("Enter the elements of the array: ");

        for (int i =0; i < size; i++){
            arr[i] = Sc.nextInt();
        }
        System.out.println("Enter the target to search: ");
        int target = Sc.nextInt();

        for (int i =0; i < arr.length;i++){
            if(arr[i] == target){
                System.out.println("Element found at index: " + i);
                return;
            }
        }
        System.out.println("Element not found in the array.");

        
    }
}
