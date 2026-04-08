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


class GraduateStudent extends Student {
    private int age ;
    private String course;

    public GraduateStudent(String name , int rollNumber , double marks , int age , String course){
        super(name , rollNumber , marks); // calling the constructor of parent class to initialize common attributes
        this.age = age;
        this.course = course;
    }

    public void DisplayGraduateDetails(){
        DisplayDetails(); // calling the method of parent class to display common details
        System.out.println("Age: " + age);
        System.out.println("Course: " + course);
    }
    
}


public class Inheritance {
    public static void main(String[] args) {

        GraduateStudent gradStudent = new GraduateStudent("Anup", 1167, 90.0, 21 , "Devops");
        gradStudent.DisplayGraduateDetails();
       
    }
}