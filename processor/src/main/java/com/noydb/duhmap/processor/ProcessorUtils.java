package com.noydb.duhmap.processor;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.error.DuhMapProcessorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ProcessorUtils {

    // 0 = fully qualified package
    // 1 - generated information
    // 2 = class name
    // 3 = original annotated interface
    public static final String CLASS_TEMPLATE = """
            package %s;

            import javax.annotation.processing.Generated;
                        
            %spublic final class %s implements %s {

            """;

    // 0 = return type class
    // 1 = method name
    // 2 = source (parameter) type class
    public static final String METHOD_TEMPLATE = "    public %s %s(final %s source) {";

    // 0 = (capitalized) field name
    // 1 = (capitalized) field name
    public static final String SET_TEMPLATE = "        target.set%s(source.get%s());";

    private ProcessorUtils() {
        // do not instantiate
    }

    // must be a method
    // each method must have one parameter
    // each method's parameter must be a class
    public static void performValidations(final RoundEnvironment roundEnv,
                                          final ProcessingEnvironment processingEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(DuhMap.class)) {
            // Validate that interfaces are the top-level elements
            if (element.getKind() != ElementKind.INTERFACE) {
                throw new DuhMapProcessorException("You may only use DuhMap annotation with an interface");
            }

            if (element.getEnclosedElements().stream().anyMatch(e -> e.getKind() != ElementKind.METHOD)) {
                throw new DuhMapProcessorException("Interfaces in DuhMap must contain methods only");
            }

            for (final var enclosedElement : element.getEnclosedElements()) {
                final var methodEl = (ExecutableElement) enclosedElement;

                List<? extends VariableElement> parameters = methodEl.getParameters();
                if (parameters.size() != 1) {
                    throw new DuhMapProcessorException("You can only map exactly one class to another in a DuhMap method");
                }

                final VariableElement param = parameters.get(0);
                final var parameterType = param.asType();
                final Types types = processingEnv.getTypeUtils();

                // Validate that the parameter is a class
                if (parameterType.getKind() != TypeKind.DECLARED) {
                    throw new DuhMapProcessorException("You can only map classes (to classes) in DuhMap");
                }

                if (types.isAssignable(parameterType, types.getDeclaredType((TypeElement) element))) {
                    throw new DuhMapProcessorException("Interfaces cannot be mapped in DuhMap");
                }
            }

            // TODO: Validate the classes have equal methods or not
        }
    }

    public static String getGeneratedAnnotation() {
        final var template = """
                @Generated(
                     value = "com.noydb.duhmap.processor.DuhMapAnnotationProcessor",
                     date = "%s",
                     comments = "java version: %s"
                )
                        """;

        return String.format(
                template,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                System.getProperty("java.version")
        );
    }

    public static String getPackageName(final TypeElement interfaceEl) {
        final var enclosingElement = interfaceEl.getEnclosingElement();

        if (enclosingElement instanceof PackageElement packageElement) {
            return packageElement.getQualifiedName().toString();
        }

        throw new DuhMapProcessorException("DuhMap failed to determine the package");
    }

    public static TypeElement asTypeElement(final ProcessingEnvironment processingEnv, final TypeMirror tm) {
        return (TypeElement) processingEnv.getTypeUtils().asElement(tm);
    }
}
