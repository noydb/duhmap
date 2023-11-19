package com.noydb.duhmapper.kit;

import com.noydb.duhmapper.DuhMapAnnotationProcessor;
import com.noydb.duhmapper.error.DuhMapException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public final class DuhMapProcessorUtils {

    public static final String DUH_MAP_VERSION = DuhMapAnnotationProcessor.class.getPackage().getImplementationVersion();

    private DuhMapProcessorUtils() {
        // do not instantiate
    }

    public static String getName(final Element element) {
        return element.getSimpleName().toString();
    }

    public static String getFullyQualifiedName(final TypeElement element) {
        return String.format(
                "%s.%s",
                element.getEnclosingElement().toString(),
                getName(element)
        );
    }

    public static TypeElement asTypeElement(final ProcessingEnvironment processingEnv, final TypeMirror tm) {
        return (TypeElement) processingEnv.getTypeUtils().asElement(tm);
    }

    public static List<VariableElement> getFields(final Element classEl) {
        final var sourceFields = new ArrayList<VariableElement>();
        for (final var field : classEl.getEnclosedElements()) {
            final var kind = field.getKind();
            if (!kind.isField()
                    || kind.equals(ElementKind.CLASS)
                    || kind.equals(ElementKind.INTERFACE)
            ) continue;

            sourceFields.add((VariableElement) field);
        }

        return sourceFields;
    }

    public static String getPackageName(final TypeElement interfaceOrClassEl) {
        final var enclosingElement = interfaceOrClassEl.getEnclosingElement();

        if (enclosingElement instanceof PackageElement packageElement) {
            return packageElement.getQualifiedName().toString();
        }

        throw new DuhMapException("duhmap failed to determine the package");
    }

    public static String getMismatchingFieldsLog(
            final List<VariableElement> sourceFieldEls,
            final List<VariableElement> targetFieldEls,
            final String targetClassName,
            final String sourceClassName
    ) {
        return String.format(
                "source: %s\n%s target: %s\n%s",
                sourceClassName,
                getFieldsPretty(sourceFieldEls),
                targetClassName,
                getFieldsPretty(targetFieldEls)
        );
    }

    private static String getFieldsPretty(final List<VariableElement> els) {
        final var builder = new StringBuilder();
        for (final var el : els) {
            var elType = el.asType().toString();
            // Remove annotations from the type string.
            // when printing a field that is annotated,
            // the annotations are printed before the type
            // causing the log to be less readable
            for (final var annotation : el.getAnnotationMirrors()) {
                String annotationString = annotation.toString();
                elType = elType.replace(annotationString, "").trim();
                // annotations leave a comma behind
                if (elType.contains(",")) {
                    elType = elType.substring(elType.lastIndexOf(",") + 1);
                }
            }

            builder
                    .append("\t")
                    .append(elType)
                    .append(" ")
                    .append(getName(el))
                    .append("\n");
        }

        return builder.toString();
    }
}
