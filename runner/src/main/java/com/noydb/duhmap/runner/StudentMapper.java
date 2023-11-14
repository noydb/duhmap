package com.noydb.duhmap.runner;

import com.noydb.duhmap.annotation.DuhMap;

@DuhMap
public interface StudentMapper {

    Student mapTo(StudentDTO dto);

    StudentDTO mapFrom(Student student);
}
