# duhmap

### Mapper Method Generation

A simple, opinionated, dependency-free Java library for generating configurable mapper classes. This library uses an
annotation processor to analyze applicable interfaces and generate mapping methods. This is done by constructing &
writing java source files - to the `generated-sources` directory - during the compilation phase.

Note: this library will currently ignore the following fields on source and target classes:
- enums
- collections
- 

### 1.

```xml
<project>

    <!-- ... -->

    <dependencies>
        <dependency>
            <groupId>com.noydb</groupId>
            <artifactId>duhmapper</artifactId>
            <version>${duhmap.version}</version>
        </dependency>
    </dependencies>

    <!-- ... -->

    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
            <annotationProcessorPaths>
                <path>
                    <groupId>com.noydb</groupId>
                    <artifactId>duhmapper</artifactId>
                    <version>${duhmap.version}</version>
                </path>
            </annotationProcessorPaths>
        </configuration>
    </plugin>

  <!-- ... -->
  
</project>
```

```gradle
dependencies {
    implementation 'com.noydb.duhmapper:${duhmap.version}'
}
```

### 2.

```Java
package com.helloworld;

import com.noydb.duhmapper.DuhMap;
import com.noydb.duhmapper.DuhMapMethod;

@DuhMap(strictChecks = true)
public interface StudentMapper {

  @DuhMapMethod(mapList = true)
  Student mapTo(StudentDTO dto);

}
```

### 3.

`mvn clean install`

`gradle build`

### Result:

```Java
package com.helloworld;

import javax.annotation.processing.Generated;

@Generated(
        value = "com.noydb.duhmapper.DuhMapAnnotationProcessor",
        date = "2023-11-18T22:20:35.842378",
        comments = "Java version: 17.0.4.1 | duhmap version 0.5.1"
)
public final class DuhStudentMapper implements StudentMapper {

    @Override
    public com.noydb.duhmapper.runner.Student mapTo(final com.noydb.duhmapper.runner.StudentDTO source) {
        if (source == null) return null;

        final com.noydb.duhmapper.runner.Student target = new com.noydb.duhmapper.runner.Student();
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());

        return target;
    }

    public java.util.List<Student> mapTo(final java.util.List<StudentDTO> sources) {
        final java.util.List<Student> targets = new java.util.ArrayList<>();
        for (final StudentDTO source : sources) {
            targets.add(mapTo(source));
        }

        return targets;
    }
}
```

---

## `@DuhMap` Configurations

#### `classType`

Configure the type of java class to generate.

- `DEFAULT`: concrete final class implementing the annotated interface's methods
- `SPRING_BEAN`: Spring Framework based bean, configured through use of `@Component` making the annotated interface available
  for dependency injection in Spring applications
- `STATIC`: un-instantiable final class implementing the interface methods as public static methods

#### `ignoredMethods`

Instruct the processor not to map the specified methods during generation.

**Note**: for `DEFAULT` & `SPRING_BEAN` types, the method will be implemented but immediately return null.

#### `ignoredStrictChecks`

Disable compilation failures by specifying any combination of the string values below (when `strictChecks=true`).

- `ignoredFields`
- `mismatchingFields`
- `ignoredMethods`
- `typeSafe`: by default, the processor will enforce identical types between source and target fields. Disabling this
  rule can allow [widening](https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2), however be careful
  not to map inconvertible types.

#### `strictChecks`

Throw compilation errors if any of the criterion listed below are not met:

- Specified ignored fields do not exist on the source and target classes.
- Specified ignored methods do not exist within the annotated interface.
- The source and target class possess a different number of fields.
- The source and target class possess fields whose names differ.
- The source and target class possess corresponding fields whose types differ.

---

## `@DuhMapMethod` Configurations

#### `ignore`

Instruct the processor not to map the annotated method during generation.

**Note**: for `DEFAULT` & `SPRING_BEAN` types, the method will be implemented but immediately return null.

#### `ignoredFields`

Instruct the processor not to map the specified fields contained within the source (and target) class during generation.

#### `mapList`

Generate a typesafe java.util.List implementation of the annotated method. It will practically replicate and re-use the
original method, but the parameter & return type signatures will be type-safe java.util.List variables.

**Note**: for `DEFAULT` & `SPRING_BEAN` types, you can utilize this annotation by defining the list method - exactly as the processor will
generate it - **in the annotated interface**. This is so that when you use the class - but do so referencing the
interface - the generated list methods will be detected.

#### `nullSafe`

Generate null checks inside all generated mapper methods (the source class will be checked for null).

---

## 1.0

- `typeSafe`
- validate interface contains nothing other than methods
- disable/handle enums & lists when they are fields on source & target? & other types.....? - document which types of fields are ignored
- Figure out handling mismatchingFields most effectively:
  - do you map as many fields as you can in order, and stop when target doesn't have the field? what if you order them
    and the last two fields aren't the same, but the lengths are?
- Proper unit testing
- make sure mismatching fields still log. think i broke it
- test again fully, make sure everything is working
- sort maven plugin versioning

### Build Issue
need to run processor in separate compile step? -proc:only. see error in intellij:
java: An exception has occurred in the compiler (17.0.4.1). Please file a bug against the Java compiler via the Java bug reporting page (http://bugreport.java.com) after checking the Bug Database (http://bugs.java.com) for duplicates. Include your program, the following diagnostic, and the parameters passed to the Java compiler in your report. Thank you.
java: java.util.ServiceConfigurationError: javax.annotation.processing.Processor: Provider com.noydb.duhmapper.DuhMapAnnotationProcessor not found
java: 	at java.base/java.util.ServiceLoader.fail(ServiceLoader.java:593)
java: 	at java.base/java.util.ServiceLoader$LazyClassPathLookupIterator.nextProviderClass(ServiceLoader.java:1219)

https://stackoverflow.com/a/54281656/8061089

https://maven.apache.org/plugins/maven-assembly-plugin/

https://github.com/mapstruct/mapstruct/blob/main/distribution/src/main/assembly/dist.xml

https://github.com/mapstruct/mapstruct/blob/main/distribution/pom.xml

---

## 2.0

- validate that the mapList signature is valid compared to original method signature
- static method template.
- Ability to reference one generated mapper from another generated mapper (ideally "smartly" detect if a mapper exists
  for a class, use it, else throw. compilation order is important or maybe not?)
- build @DuhDTO
- Logging
- names: rulost, needamap, dpin
- Needed because the java files are on the compiler classpath: See: https://jira.codehaus.org/browse/MCOMPILER-97
- If you control the source code I also recommend to package the processor in the same artifact as the annotations. Like
  this, whenever you're using one of the annotations, the annotation processor is also picked-up by the compiler.
- https://github.com/mapstruct/mapstruct/blob/main/processor/src/main/java/org/mapstruct/ap/MappingProcessor.java
- https://github.com/pellaton/spring-configuration-validation-processor/blob/master/config-validation-processor-java11/pom.xml
- https://github.com/pellaton/spring-configuration-validation-processor/blob/master/config-validation-processor-java11/pom.xml


---

### Research, Useful Info, Etc.

- https://docs.oracle.com/en/java/javase/11/docs/api/java.compiler/javax/lang/model/element/package-summary.html
- https://github.com/chdir/aidl2/
- https://stackoverflow.com/questions/47779403/annotation-processing-roundenvironment-processingover/47782562#47782562
- understand java versioning etc (`@SupportedSourceVersion(SourceVersion.RELEASE_8)`)
- Represents a program element such as a module, package, class, or method. Each element represents a compile-time
  language-level construct (and not, for example, a runtime construct of the virtual machine).
  Elements should be compared using the equals(Object) method. There is no guarantee that any particular element will
  always be represented by the same object.
  To implement operations based on the class of an Element object, either use a visitor or use the result of the getKind
  method. Using instanceof is not necessarily a reliable idiom for determining the effective class of an object in this
  modeling hierarchy since an implementation may choose to have a single object implement multiple Element
  subinterfaces.
  
