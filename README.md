# DuhMap
### One Liner Mapper Method Generation

A simple, opinionated, dependency-free, Java library for generating configurable mapper classes. This library uses an annotation processor to analyze applicable interfaces and generate mapping methods. This is done by constructing & writing java source files - to the `generated-sources` directory - during the compilation phase.

### Step One
```xml
<dependencies>
    <dependency>
        <groupId>com.noydb.duhmap</groupId>
        <artifactId>processor</artifactId>
        <version>${duhmap.version}</version>
    </dependency>
</dependencies>
```

### Step Two
```Java
package com.noydb.duhmap.runner;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;

@DuhMap(strictChecks = true)
public interface StudentMapper {

    @DuhMapMethod(mapList = true)
    Student mapTo(StudentDTO dto);

}
```

### Result
```Java
package com.noydb.duhmap.runner;

import javax.annotation.processing.Generated;

@Generated(
     value = "com.noydb.duhmap.processor.DuhMapAnnotationProcessor",
     date = "2023-11-18T22:20:35.842378",
     comments = "java version: 17.0.4.1"
)
public final class DuhStudentMapper implements StudentMapper {

    @Override
    public com.noydb.duhmap.runner.Student mapTo(final com.noydb.duhmap.runner.StudentDTO source) {
        if (source == null) return null;

        final com.noydb.duhmap.runner.Student target = new com.noydb.duhmap.runner.Student();
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());

        return target;
    }

    public java.util.List<Student> mapTo(final java.util.List<StudentDTO> sources) {
        final java.util.List<Student> targets = new java.util.ArrayList<>();
        for (final StudentDTO source: sources) {
            targets.add(mapTo(source));
        }

        return targets;
    }
}
```

---

## `@DuhMap` Configurations
- `strictChecks`: throw compilation errors if certain pre-defined rules fail during generation. Rules:
  - if specified ignored fields do not exist on the source and target, fail.
  - if the source and target classes do not possess identical fields. Equality is determined by number of fields, names, and types.
  - `implicitCasting` must be disabled (set to false)

- `ignoredMethods`: tell DuhMap to skip any methods contained within the annotated interface  

- `beanType`: configure the type of java bean to generate.
  - `DEFAULT`: concrete final class implementing the annotated interface's methods
  - `SPRING`: Spring Framework based bean, configured through use of `@Component` making the annotated interface available for dependency injection in Spring applications 
  - `STATIC`: un-instantiable final class implementing the interface methods as public static methods

- TODO `ignoredStrictChecks`: disable compilation failure for any combination of the rules run when `strictChecks=true`

---

## `@DuhMapMethod` Configurations 
- `ignore`: tell DuhMap to not map the annotated method (it will return null for `SPRING` & `DEFAULT` bean types)

- `ignoreFields`: tell DuhMap to skip any fields contained on the source (and target) class during generation

- `nullSafe`: generate null checks inside all generated mapper methods (the source class is checked for null)

- `mapList`: generate a list implementation of the annotated method. It will replicate the original method, but the parameter & return type signatures will be type-safe `java.util.List` variables
  - **Note**: with Spring beans, you can utilize this annotation by setting it to true _and_ defining the list method **in the annotated interface**. This is so that when you inject the bean - but do so referencing the interface - the generated list methods will be detected) 

- TODO `implicitCast`: automatically widen/promote a value for a source field, if the target field type permits. (`byte` -> `short` -> `char` -> `int` -> `long` -> `float` -> `double`)
  - **Note**: you cannot use this annotation when `strictChecks=true`

---

## TODOs
- test what happens if you have a field on one class but it's not on the other
- disable/handle enums & lists when they are fields on source & target? & other types.....?
- maybe allow mapAll with spring bean, by adding list to interface and using mapAll with it. explain in readme
- validate ignoredMethods exist

---

## 2.0
- static method template.
- Ability to reference one generated mapper from another generated mapper (ideally "smartly" detect if a mapper exists for a class, use it, else throw. compilation order is important or maybe not?)
- Add implicitClass (default true) to interface and method annotations. if strictChecks=true, implicitCast cannot be true
- build @DuhDTO
- add ability to disable any one of the strict rules
- Logging
- Proper unit testing

---

### Research, Useful Info, Etc.
- sort maven plugin versioning
- understand java versioning etc (`@SupportedSourceVersion(SourceVersion.RELEASE_8)`)
- Represents a program element such as a module, package, class, or method. Each element represents a compile-time language-level construct (and not, for example, a runtime construct of the virtual machine).
  Elements should be compared using the equals(Object) method. There is no guarantee that any particular element will always be represented by the same object.
  To implement operations based on the class of an Element object, either use a visitor or use the result of the getKind method. Using instanceof is not necessarily a reliable idiom for determining the effective class of an object in this modeling hierarchy since an implementation may choose to have a single object implement multiple Element subinterfaces.
- Needed because the java files are on the compiler classpath: See: https://jira.codehaus.org/browse/MCOMPILER-97
- If you control the source code I also recommend to package the processor in the same artifact as the annotations. Like this, whenever you're using one of the annotations, the annotation processor is also picked-up by the compiler.
- https://github.com/mapstruct/mapstruct/blob/main/processor/src/main/java/org/mapstruct/ap/MappingProcessor.java
- https://github.com/pellaton/spring-configuration-validation-processor/blob/master/config-validation-processor-java11/pom.xml
- https://github.com/pellaton/spring-configuration-validation-processor/blob/master/config-validation-processor-java11/pom.xml
