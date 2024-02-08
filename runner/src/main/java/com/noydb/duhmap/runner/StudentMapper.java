package com.noydb.duhmap.runner;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;

@DuhMap(strictChecks = true, ignoredStrictChecks = {"typeSafe"})
public interface StudentMapper {

    @DuhMapMethod(mapList = true)
    Student mapTo(StudentDTO dto);

    @DuhMapMethod(mapList = true)
    StudentDTO mapFrom(Student student);

}
