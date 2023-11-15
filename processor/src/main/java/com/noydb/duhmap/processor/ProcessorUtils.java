package com.noydb.duhmap.processor;

import com.noydb.duhmap.error.DuhMapException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ProcessorUtils {

    // 0 = fully qualified package
    // 1 = generated information
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

    public static String getPackageName(final TypeElement interfaceOrClassEl) {
        final var enclosingElement = interfaceOrClassEl.getEnclosingElement();

        if (enclosingElement instanceof PackageElement packageElement) {
            return packageElement.getQualifiedName().toString();
        }

        throw new DuhMapException("DuhMap failed to determine the package");
    }

    public static TypeElement asTypeElement(final ProcessingEnvironment processingEnv, final TypeMirror tm) {
        return (TypeElement) processingEnv.getTypeUtils().asElement(tm);
    }

    public static String getName(final Element element) {
        return element.getSimpleName().toString();
    }

    public static List<String> getFields(final Element classEl) {
        final var sourceFields = new ArrayList<String>();
        for (final var field : classEl.getEnclosedElements()) {
            if (field.getKind().isField()) {
                sourceFields.add(getName(field));
            }
        }

        return sourceFields;
    }

}
