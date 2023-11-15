package com.noydb.duhmap.runner;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;

@DuhMap(strictChecks = true)
public interface StudentMapper {

    @DuhMapMethod(ignoredFields = {"age", "firstName"})
    Student mapTo(StudentDTO dto);

    @DuhMapMethod(ignore = true)
    StudentDTO mapFrom(Student student);
}
