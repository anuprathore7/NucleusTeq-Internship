// I am Storing student data in array of objects
// Each student has his/her name, marks and attendance

const students = [
  {
    name: "Lalit",
    marks: [
      { subject: "Math", score: 78 },
      { subject: "English", score: 82 },
      { subject: "Science", score: 74 },
      { subject: "History", score: 69 },
      { subject: "Computer", score: 88 }
    ],
    attendance: 82
  },
  {
    name: "Rahul",
    marks: [
      { subject: "Math", score: 90 },
      { subject: "English", score: 85 },
      { subject: "Science", score: 80 },
      { subject: "History", score: 76 },
      { subject: "Computer", score: 92 }
    ],
    attendance: 91
  }
];

console.log(students , "Student data loaded");

// it is function to calculate total marks
function getTotalMarks(student) {
  let total = 0;

  // i have used for each to iterate each student score of every subject.
  student.marks.forEach(mark => {
    total += mark.score;
  });

  return total;
}

// it is going to display total marks
students.forEach(student => {
    // `` this is the template literals inside this we can put our string as well as data 
  console.log(`${student.name} Total Marks: ${getTotalMarks(student)}`);
});

//it is function to calculate average marks
function getAverage(student) {
  const total = getTotalMarks(student);
  return total / student.marks.length;
}

// it displays average marks
students.forEach(student => {
  console.log(`${student.name} Average: ${getAverage(student).toFixed(1)}`);
});


//it is getting all subjects dynamically
const subjects = students[0].marks.map(sub => sub.subject);

console.log("Subject Toppers");

subjects.forEach(subject => {
  let highest = 0;
  let topper = "";

  students.forEach(student => {
    const sub = student.marks.find(s => s.subject === subject);

    if (sub.score > highest) {
      highest = sub.score;
      topper = student.name;
    }
  });

  console.log(`Highest in ${subject}: ${topper} (${highest})`);
});