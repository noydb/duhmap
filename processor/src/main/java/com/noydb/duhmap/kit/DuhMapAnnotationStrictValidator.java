package com.noydb.duhmap.kit;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;
import com.noydb.duhmap.error.StrictDuhMapException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static com.noydb.duhmap.kit.DuhMapProcessorUtils.asTypeElement;
import static com.noydb.duhmap.kit.DuhMapProcessorUtils.getFullyQualifiedName;

public final class DuhMapAnnotationStrictValidator {

    private DuhMapAnnotationStrictValidator() {
        // do not instantiate
    }

    public static void validate(Element interfaceEl, final ProcessingEnvironment processingEnv) {
        final var annotation = interfaceEl.getAnnotation(DuhMap.class);
        if (!annotation.strictChecks()) {
            return;
        }

        for (final var el : interfaceEl.getEnclosedElements()) {
            if (!el.getKind().equals(ElementKind.METHOD)) {
                continue;
            }

            final TypeMirror paramType = ((ExecutableElement) el).getParameters().get(0).asType();
            final var paramEl = (TypeElement) ((DeclaredType) paramType).asElement();
            final var paramClassFields = DuhMapProcessorUtils.getFields(paramEl);

            final var returnTypeEl = asTypeElement(
                    processingEnv, ((ExecutableType) el.asType()).getReturnType()
            );
            final var returnTypeClassFields = DuhMapProcessorUtils.getFields(returnTypeEl);

            validateFieldsMatch(paramClassFields, returnTypeClassFields, paramEl, returnTypeEl);
            validateIgnoredFieldsExist(
                    paramClassFields,
                    el.getAnnotation(DuhMapMethod.class),
                    interfaceEl
            );
        }
    }

    private static void validateFieldsMatch(
            final List<String> paramClassFields,
            final List<String> returnTypeClassFields,
            final TypeElement paramEl,
            final TypeElement returnTypeEl
    ) {
        final var paramClassName = getFullyQualifiedName(paramEl);
        final var returnTypeClassName = getFullyQualifiedName(returnTypeEl);

        if (paramClassFields.size() != returnTypeClassFields.size()) {
            throw new StrictDuhMapException(
                    String.format(
                            "There was a mismatch between the number of fields for the classes: %s(%s) %s(%s)",
                            returnTypeClassName,
                            returnTypeClassFields,
                            paramClassName,
                            paramClassFields
                    )
            );
        }

        final var equalFields = returnTypeClassFields
                .stream()
                .sorted()
                .toList()
                .equals(paramClassFields.stream().sorted().toList());
        if (!equalFields) {
            throw new StrictDuhMapException(
                    String.format(
                            "There was a mismatch between the parameters of the classes: %s %s",
                            returnTypeClassName,
                            paramClassName
                    )
            );
        }
    }

    // we only need one set of fields
    // as we've already validated both
    // classes have identical fields
    private static void validateIgnoredFieldsExist(
            final List<String> paramClassFields,
            final DuhMapMethod annotation,
            final Element interfaceEl
    ) {
        for (final var ignoredField : annotation.ignoredFields()) {
            if (!paramClassFields.contains(ignoredField)) {
                throw new StrictDuhMapException(
                        String.format(
                                "For interface: %s the ignored field %s does not exist in the source and target classes",
                                getFullyQualifiedName((TypeElement) interfaceEl),
                                ignoredField
                        )
                );
            }
        }
    }
}
