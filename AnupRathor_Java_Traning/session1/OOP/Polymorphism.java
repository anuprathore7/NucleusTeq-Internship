// IT is the example of polymorphism in which we have two methods with same name but different parameters. This is called method overloading.

class MathOperation {

    public int add(int a, int b) {
        return a + b;
    }

    public double add(double a, double b) {
        return a + b;
    }
}

public class Polymorphism {
    public static void main(String[] args) {
        MathOperation math = new MathOperation();
        System.out.println(math.add(5, 10));
        System.out.println(math.add(5.5, 10.5));
    }
}
