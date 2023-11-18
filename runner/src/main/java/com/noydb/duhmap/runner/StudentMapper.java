package com.noydb.duhmap.runner;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;

@DuhMap()
public interface StudentMapper {

    @DuhMapMethod(ignoredFields = {"age", "firstName1111"}, mapList = true)
    Student mapTo(StudentDTO dto);

    @DuhMapMethod(mapList = true)
    StudentDTO mapFrom(Student student);

//    List<StudentDTO> mapFrom(final List<Student> sources);
}
