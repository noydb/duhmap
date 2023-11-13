package com.noydb.damap.runner;

public final class DuhStudentMapper implements StudentMapper {

    public Student mapTo(StudentDTO source) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setAge(source.getAge());
    }
    public StudentDTO mapFrom(Student source) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setAge(source.getAge());
    }

}