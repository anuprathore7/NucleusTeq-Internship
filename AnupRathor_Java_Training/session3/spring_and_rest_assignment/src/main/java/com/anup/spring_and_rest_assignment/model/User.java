package com.anup.spring_and_rest_assignment.model;

public class User {

    private Long id;
    private String name;
    private Integer age;
    private String role;

    public User(Long id, String name, Integer age, String role) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.role = role;
    }

    public User(int id2, String name2, int age2, String role2) {
        //TODO Auto-generated constructor stub
    }

    public Long getId() {
        return id;

    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public String getRole() {
        return role;
    }

}
