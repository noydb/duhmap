package com.noydb.duhmap.runner;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;
import com.noydb.duhmap.kit.DuhMapStrictRule;

@DuhMap(strictChecks = true, ignoredStrictChecks = {DuhMapStrictRule.MISMATCHED_FIELD_COUNT})
public interface StudentMapper {

    @DuhMapMethod(mapList = true)
    Student mapTo(StudentDTO dto);

    @DuhMapMethod(mapList = true)
    StudentDTO mapFrom(Student student);

}
