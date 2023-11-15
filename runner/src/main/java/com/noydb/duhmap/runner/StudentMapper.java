package com.noydb.duhmap.runner;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;

@DuhMap(ignoredMethods = {"mapFrom"})
public interface StudentMapper {

    @DuhMapMethod(ignoredFields = {"age", "firstName"})
    Student mapTo(StudentDTO dto);

    StudentDTO mapFrom(Student student);
}
