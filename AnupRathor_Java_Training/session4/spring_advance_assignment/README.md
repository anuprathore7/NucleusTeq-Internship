# Spring Advanced Assignment – TODO Application

##  Project Overview

This project is a **Spring Boot-based REST API** designed to implement a simple **TODO management system** while strictly following **clean architecture and industry-standard backend practices**.

The main goal of this project is not just to perform CRUD operations, but to demonstrate:

* Proper **layered architecture**
* Clean separation of concerns
* Usage of **Spring Boot with JPA (Hibernate)**
* Implementation of **DTO pattern**
* Constructor-based **Dependency Injection**
* Centralized **Exception Handling**
* Request validation using `@Valid`

This project is structured in a way that any developer can easily understand the flow of data and extend it in the future.

---

##  Project Structure

```id="r1d9yz"
spring_advance_assignment/
└── src/main/java/com/anup/spring_advance_assignment/
    ├── controller/         # Handles HTTP requests (API layer)
    ├── service/            # Contains business logic
    ├── repository/         # Handles database interaction via JPA
    ├── entity/             # Represents database tables
    ├── dto/                # Data Transfer Objects (input/output)
    ├── exception/          # Custom and global exception handling
    └── SpringAdvanceAssignmentApplication.java
```

---

##  Complete Application Flow (Step-by-Step)

Understanding the flow is the most important part of this project.

```id="k33wsl"
Client Request (Postman / Frontend)
        ↓
Controller Layer
        ↓
Service Layer
        ↓
Repository Layer
        ↓
Database (In-Memory via JPA/Hibernate)
        ↓
Back to Service → Controller → Client Response
```

---

##  Detailed Flow Explanation

### 1️ Client sends request

Example:

```"
POST /todos
```

With JSON:

```
{
  "title": "Learn Spring Boot",
  "description": "Understand backend",
  "status": "PENDING"
}
```

---

### 2️ Controller Layer

* Receives HTTP request
* Converts JSON → DTO using `@RequestBody`
* Validates input using `@Valid`
* Delegates work to Service layer

 No business logic is written here.

---

### 3️ Service Layer (Core Logic)

This is the **brain of the application**.

Responsibilities:

* Apply business rules
* Handle default values (e.g., status = PENDING)
* Validate status transitions
* Convert DTO → Entity
* Interact with repository
* Handle exceptions

Example logic:

* If status is not provided → set to `PENDING`
* If invalid status transition → throw exception

---

### 4️ Repository Layer

* Acts as a bridge between Service and Database
* Uses `JpaRepository`
* Automatically provides methods like:

  * `save()`
  * `findAll()`
  * `findById()`
  * `deleteById()`

 No SQL queries are written manually.

---

### 5️ Database Layer

* Managed internally by **JPA + Hibernate**
* Data is stored temporarily in memory
* Used only for runtime operations

---

### 6️ Response Flow

* Data fetched from database (Entity)
* Converted to DTO
* Returned as JSON response to client

---

## Core Components Explained

---

###  Entity Layer (`Todo.java`)

Represents the structure of the data stored in the database.

Fields:

* `id` → Unique identifier (auto-generated)
* `title` → Task title
* `description` → Task details
* `status` → Enum (`PENDING`, `COMPLETED`)
* `createdAt` → Timestamp of creation

 Used internally by Hibernate to map Java object → database table.

---

###  DTO Layer (`TodoDTO.java`)

Used for **data transfer between client and server**.

Includes:

* `title` → Required, minimum 3 characters
* `description` → Optional
* `status` → Optional

 Ensures:

* No direct exposure of Entity
* Controlled input/output

---

###  Repository Layer (`TodoRepository.java`)

* Extends `JpaRepository<Todo, Long>`
* Provides built-in database operations
* Eliminates need for manual SQL queries

---

###  Service Layer (`TodoServiceImpl.java`)

Handles:

* Business logic
* Validation rules
* Exception throwing
* DTO ↔ Entity conversion

Key behaviors:

* Automatically sets `createdAt`
* Defaults status to `PENDING`
* Validates allowed status transitions

---

###  Controller Layer (`TodoController.java`)

Handles API endpoints:

| Method | Endpoint    | Description    |
| ------ | ----------- | -------------- |
| POST   | /todos      | Create TODO    |
| GET    | /todos      | Get all TODOs  |
| GET    | /todos/{id} | Get TODO by ID |
| PUT    | /todos/{id} | Update TODO    |
| DELETE | /todos/{id} | Delete TODO    |

 Only handles request and response mapping.

 1. Create TODO
 ![Create Todo](<screenshots\CreateTodo.png>)

 2. Get All TODOS
 ![Get All Todos](<screenshots\GetAllTodo.png>)

 3. Get Todo By Id
 ![Get Todo By Id](<screenshots\GetById.png>)

 4. Update Todo 
 ![Update Todo](<screenshots\Update Todo.png>)
 
 5. Delete Todo 
 ![Delete Todo](<screenshots\Delete Todo.png>)
---

##  Exception Handling (Very Important)

This project uses **centralized exception handling**.

---

###  Custom Exceptions

* `ResourceNotFoundException`

  * Thrown when a TODO is not found

![Todo Not Found](<screenshots\TodoNotFound.png>)

* `InvalidStatusException`

  * Thrown when status transition is invalid

---

###  Global Exception Handler

Implemented using:

```
@RestControllerAdvice
```

Responsibilities:

* Catch exceptions globally
* Return clean, readable error messages
* Prevent default 500 Internal Server Error responses

---

###  Special Case Handling

Invalid enum values (e.g., `"pendin"`) are handled using:

```
HttpMessageNotReadableException
```

 This ensures users get a meaningful error message instead of a generic server error.

---

##  Business Rule: Status Transition

Allowed transitions:

PENDING → COMPLETED
COMPLETED → PENDING
```

Any other transition:
❌ Not allowed → Exception thrown
```

---

##  Validation Rules

* `title`

  * Cannot be null
  * Minimum 3 characters

* `description`

  * Optional

 Validation is triggered using `@Valid` in controller.

---

## Key Concepts Demonstrated

This project clearly demonstrates:

* **Inversion of Control (IoC)**
  Spring manages object creation

* **Dependency Injection (Constructor-based)**
  Dependencies are injected via constructors

* **Layered Architecture**
  Separation of concerns across layers

* **DTO Pattern**
  Clean data transfer without exposing internal models

* **JPA & Hibernate**
  ORM-based database interaction

* **Exception Handling**
  Centralized and clean error responses

---

## Tech Stack

* Java 17
* Spring Boot
* Spring Data JPA
* Hibernate
* Maven

---

##  How to Run the Project

1. Clone the repository
2. Navigate to project directory
3. Run the application:

```mvn spring-boot:run
```
4. Use Postman or any API tool to test endpoints

---

##  Author

**Anup Rathor**

---

