package com.noydb.duhmap.runner;

import java.util.ArrayList;
import java.util.List;

public class LittleTest {

    private static final DuhStudentMapper mapper = new DuhStudentMapper();

    public static void main(String[] args) {
        Student student = new Student("Jim", "Jones", 12);
        Student student1 = new Student("Chris", "Smith", 102);
        Student student2 = new Student("Mike", "Edwards", 18);
        Student student3 = new Student("Will", "Freeddy", 22);

        final List<Student> students = new ArrayList<>();
        students.add(student);
        students.add(student1);
        students.add(student2);
        students.add(student3);

        final var dtos = mapper.mapFrom(students);

        for (StudentDTO dto : dtos) {
            System.out.println(dto + "\n");
        }
    }
}
