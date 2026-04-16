# Spring and REST Assignment

This project is a simple backend application built using Spring Boot to understand how REST APIs work in a real-world structure. The main focus while building this was not just functionality, but also writing clean code and following proper architecture.

The application manages a small set of users using in-memory data (no database), and exposes APIs to search, add, and delete users.

---

## Overview

The idea behind this project is to learn how backend systems are structured. Instead of putting everything in one place, the project is divided into layers so that each part has a clear responsibility.

* Controller handles incoming requests
* Service contains the actual logic
* Repository manages the data
* Model represents the structure of data

This separation makes the code easier to read and maintain.

---

## User Structure

Each user in the system contains the following fields:

* `id` → Unique identifier (Long)
* `name` → Name of the user (String)
* `age` → Age of the user (Integer)
* `role` → Role of the user (String)

---

## Features

* Search users using different filters like name, age, and role
* Case-insensitive matching for name and role
* Exact match filtering for age
* Add new users using JSON input
* Delete users with a confirmation check to avoid mistakes
* Proper layering (Controller → Service → Repository)
* Basic validation and error handling
* Clean and readable code structure

---

## Tech Stack

* Java 17
* Spring Boot
* Maven
* REST APIs
* IntelliJ IDEA

---

## Project Structure

```
spring_and_rest_assignment/
├── src/
│   ├── main/
│   │   ├── java/com/anup/spring_and_rest_assignment/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   └── SpringAndRestAssignmentApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
└── README.md
```

---

## How to Run

### Prerequisites

* Java 17 installed
* Maven installed

### Steps

1. Clone the repository
2. Open the project in your IDE (IntelliJ recommended)
3. Navigate to the project folder
4. Run the application using:

```
mvn spring-boot:run
```

5. Open browser or Postman and hit:

```
http://localhost:8080/users/search
```

---

## API Endpoints

### 1. Search Users

```
GET /users/search
```

You can pass optional query parameters:

Examples:

```
/users/search
/users/search?name=Anup
/users/search?age=21
/users/search?role=USER
/users/search?age=21&role=USER
```

If no parameters are passed, all users are returned.

---

### 2. Add User

```
POST /submit
```

Sample request:

```
{
  "id": 6,
  "name": "Anup",
  "age": 21,
  "role": "USER"
}
```

* Returns `201 Created` on success
* Returns `400 Bad Request` if input is invalid

---

### 3. Delete User

```
DELETE /users/{id}?confirm=true
```

Important behavior:

* If `confirm=true` is not passed → user will NOT be deleted
* This ensures safe deletion and avoids accidental mistakes

---

## Testing APIs (using curl)

Search all users:

```
curl -X GET http://localhost:8080/users/search
```

Search by name:

```
curl -X GET "http://localhost:8080/users/search?name=Anup"
```

Submit user:

```
curl -X POST http://localhost:8080/submit \
-H "Content-Type: application/json" \
-d '{"name":"John","age":25,"role":"USER"}'
```

Delete without confirmation:

```
curl -X DELETE "http://localhost:8080/users/1"
```

Delete with confirmation:

```
curl -X DELETE "http://localhost:8080/users/1?confirm=true"
```

---

## Concepts Practiced

While building this project, the focus was on understanding:

* How Spring Boot simplifies backend development
* How REST APIs are designed and structured
* Importance of layered architecture
* Constructor-based dependency injection
* Inversion of Control (IoC)
* Exception handling using a global handler
* Handling request data using `@RequestParam` and `@RequestBody`
* Using Java Streams for filtering data

---

## Final Thoughts

This project helped in understanding how a backend application should be structured properly. Instead of writing everything in one place, separating logic into layers makes the application more organized and easier to scale in the future.

The goal here was not complexity, but clarity and correctness.

---
