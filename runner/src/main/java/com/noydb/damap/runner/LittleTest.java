package com.noydb.damap.runner;

public class LittleTest {

    private static final com.noydb.damap.runner.DuhStudentMapper mapper = new com.noydb.damap.runner.DuhStudentMapper();

    public static void main(String[] args) {
        Student student = new Student("Jim", "Jones", 12);

//        StudentDTO dto = mapper.mapFrom(student);

//        System.out.println(dto);
    }
}
