

public class Exceptions {
    public static void main(String[] args) {
        try {
            int arr[] = {1, 2, 3};
            System.out.println(arr[5]); // This will throw ArrayIndexOutOfBoundsException
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("You can not access this index in the array !");
        } finally {
            System.out.println("This block will always execute.");
        }
    }
}
