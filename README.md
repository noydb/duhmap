# DuhMap
A simple, opinionated, dependency-free, Java library for generating configurable mapper classes. This library uses an annotation processor to analyze applicable interfaces and generate mapping methods. This is done by constructing & writing java source files - to the `generated-sources` directory - during the compilation phase.

#### Interface Configurations
- `strictChecks`
- `ignoredMethods`
- `beanType`
- TODO `ignoredStrictChecks`

#### Method Configurations
- `ignore`
- `ignoreFields`
- `nullSafe`
- `mapList`
- TODO `implicitCast`

---

### TODOs: 
- log mismatching fields (when non strict) and log basically "mapped what was possible" 
  - test what happens if you have a field on one class but it's not on the other
- disable/handle enums & lists when they are fields on source & target? & other types.....?
- maybe allow mapAll with spring bean, by adding list to interface and using mapAll with it. explain in readme

---

### 2.0
- static method template.
- Ability to reference one generated mapper from another generated mapper (ideally "smartly" detect if a mapper exists for a class, use it, else throw. compilation order is important or maybe not?)
- Add implicitClass (default true) to interface and method annotations. if strictChecks=true, implicitCast cannot be true
- build @DuhDTO
- add ability to disable any one of the strict rules

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
