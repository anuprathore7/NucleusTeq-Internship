class Student {
    private String name;
    private int rollNumber;
    private double marks;

    public Student (String name , int rollNumber , double marks){
        this.name = name;
        this.rollNumber = rollNumber;
        this.marks = marks;
    }

    public String getName () {
        return name;
    }

    public int getRollNumber() {
        return rollNumber;
    }

    public double getMarks(){
        return marks;
    }
    
    public void DisplayDetails(){
        System.out.println("Name: " + name);
        System.out.println("Roll Number: " + rollNumber);
        System.out.println("Marks: " + marks);
    }

}

public class StudentDetail {
    public static void main(String[] args) {
        Student student1 = new Student("Anup" , 101 , 85.5);
        student1.DisplayDetails();

        System.out.println();

        Student student2 = new Student ("Anshika" , 102 , 90);
        student2.DisplayDetails();

        // we can also fetch individual details of particular student using getter methods 

        System.out.println("\nFetching individual name of student1: " + student1.getName());
    }
}