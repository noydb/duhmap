package com.noydb.damap.runner;

import com.noydb.damap.annotation.DuhMap;

@DuhMap
public interface StudentMapper {

    Student mapTo(StudentDTO dto);

    StudentDTO mapFrom(Student student);
}
