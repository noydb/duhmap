Represents a program element such as a module, package, class, or method. Each element represents a compile-time language-level construct (and not, for example, a runtime construct of the virtual machine).
Elements should be compared using the equals(Object) method. There is no guarantee that any particular element will always be represented by the same object.
To implement operations based on the class of an Element object, either use a visitor or use the result of the getKind method. Using instanceof is not necessarily a reliable idiom for determining the effective class of an object in this modeling hierarchy since an implementation may choose to have a single object implement multiple Element subinterfaces.



// TODO: potentially don't implement provided interface and
// then make methods static?
// could make this configurable?

// TODO
// add a failStrict attribute to annotation. if unrecognized ignore fields, or classes don't have identical fields, etc. 

add null checks. make configurable? 