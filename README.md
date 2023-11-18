# DuhMap
### One Liner Mapper Method Generation

A simple, opinionated, dependency-free, Java library for generating configurable mapper classes. This library uses an annotation processor to analyze applicable interfaces and generate mapping methods. This is done by constructing & writing java source files - to the `generated-sources` directory - during the compilation phase.

### 1.
```xml
<dependencies>
    <dependency>
        <groupId>com.noydb.duhmap</groupId>
        <artifactId>processor</artifactId>
        <version>${duhmap.version}</version>
    </dependency>
</dependencies>
```

### 2.
```Java
package ...;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;

@DuhMap(strictChecks = true)
public interface StudentMapper {

    @DuhMapMethod(mapList = true)
    Student mapTo(StudentDTO dto);

}
```

### 3.
`mvn clean install`

### Done.
```Java
package ...;

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

#### `beanType`
Configure the type of java bean to generate.

- `DEFAULT`: concrete final class implementing the annotated interface's methods
- `SPRING`: Spring Framework based bean, configured through use of `@Component` making the annotated interface available for dependency injection in Spring applications 
- `STATIC`: un-instantiable final class implementing the interface methods as public static methods

#### `ignoredMethods` TODO
Instruct the processor not to map the specified methods during generation. 

Note: for `SPRING` & `DEFAULT` bean types, the method will be implemented but immediately return null.

#### `ignoredStrictChecks` TODO
Disable compilation failure by specifiying any combination of the string values below (when `strictChecks=true`)

- `ignoreFields`
- `mismatchingFields`
- `ignoredMethods`
- `implicitCasting`
- `implicitCasting`: By default, the processor will [widen](https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2) source variables, if the target field type permits doing so. 

#### `strictChecks`

Throw compilation errors if any of the criteria listed below are not met:
  - Specified ignored fields do not exist on the source and target.
  - The source and target classes do not possess identical fields. Equality is determined by number of fields, names, and types.

---

## `@DuhMapMethod` Configurations 

#### `ignore`

Instruct the processor not to map the annotated method during generation. 

Note: for `SPRING` & `DEFAULT` bean types, the method will be implemented but immediately return null.   

#### `ignoredFields`

Instruct the processor not to map the specified fields contained within the source (and target) class during generation.

#### `mapList`

Generate a list implementation of the annotated method. It will replicate the original method, but the parameter & return type signatures will be type-safe `java.util.List` variables

**Note**: for Spring beans, you can utilize this annotation by defining the list method - replicating the one DuhMap will generate - **in the annotated interface**. This is so that when you inject the bean - but do so referencing the interface - the generated list methods will be detected) 

#### `nullSafe`
Generate null checks inside all generated mapper methods (the source class will be checked for null)

---

## TODOs
- test what happens if you have a field on one class but it's not on the other
- disable/handle enums & lists when they are fields on source & target? & other types.....?
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
- ignored methods
- ignoredStrictChecks

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
