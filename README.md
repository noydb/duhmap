Research, Useful Info, Etc.
- Represents a program element such as a module, package, class, or method. Each element represents a compile-time language-level construct (and not, for example, a runtime construct of the virtual machine).
Elements should be compared using the equals(Object) method. There is no guarantee that any particular element will always be represented by the same object.
To implement operations based on the class of an Element object, either use a visitor or use the result of the getKind method. Using instanceof is not necessarily a reliable idiom for determining the effective class of an object in this modeling hierarchy since an implementation may choose to have a single object implement multiple Element subinterfaces.
- Needed because the java files are on the compiler classpath: See: https://jira.codehaus.org/browse/MCOMPILER-97
- If you control the source code I also recommend to package the processor in the same artifact as the annotations. Like this, whenever you're using one of the annotations, the annotation processor is also picked-up by the compiler.
- https://github.com/mapstruct/mapstruct/blob/main/processor/src/main/java/org/mapstruct/ap/MappingProcessor.java
- https://github.com/pellaton/spring-configuration-validation-processor/blob/master/config-validation-processor-java11/pom.xml
- https://github.com/pellaton/spring-configuration-validation-processor/blob/master/config-validation-processor-java11/pom.xml

TODOs: 
- log full package names in exceptions for better error tracing
- test what happens if you have a field on one class but it's not on the other
- validate fields by name AND type in strictChecks validator.
- Allow class to be constructed as spring bean: https://github.com/mapstruct/mapstruct/blob/main/processor/src/main/java/org/mapstruct/ap/internal/processor/SpringComponentProcessor.java
- potentially don't implement provided interface and then make methods static? Could make this configurable? 
- add null checks. make configurable? 
- build @DuhDTO?
- Add implicitClass (default true) to interface and method annotations. if strictChecks=true, implicitCast cannot be true
- Map Lists/ArrayLists/Collections. Same logic, but just need for loop logic
- Print arrays in the stack trace of the mismatched field lists
- Ability to reference one generated mapper from another generated mapper
