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
import java.util.Comparator;
import java.util.List;

import static com.noydb.duhmap.kit.DuhMapProcessorUtils.asTypeElement;
import static com.noydb.duhmap.kit.DuhMapProcessorUtils.getFullyQualifiedName;

public final class DuhMapAnnotationStrictValidator {

    private DuhMapAnnotationStrictValidator() {
        // do not instantiate
    }

    public static void run(
            final ProcessingEnvironment processingEnv,
            final Element interfaceEl
    ) {
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
            final List<Element> paramClassFields,
            final List<Element> returnTypeClassFields,
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

        final var sourceFieldsSorted = paramClassFields
                .stream()
                .sorted(Comparator.comparing(e -> e.getSimpleName().toString()))
                .toList();
        final var returnFieldsSorted = returnTypeClassFields
                .stream()
                .sorted(Comparator.comparing(e -> e.getSimpleName().toString()))
                .toList();

        for (int i = 0; i < sourceFieldsSorted.size(); i++) {
            final var sourceField = sourceFieldsSorted.get(i);
            final var returnField = returnFieldsSorted.get(i);

            if (!sourceField.getSimpleName().equals(returnField.getSimpleName())) {
                throw new StrictDuhMapException(
                        String.format(
                                "There was a mismatch between the source and target fields: %s(%s) VS %s(%s)",
                                paramClassName,
                                sourceFieldsSorted,
                                returnTypeClassName,
                                returnFieldsSorted
                        )
                );
            }

            if (!sourceField.asType().equals(returnField.asType())) {
                throw new StrictDuhMapException(
                        String.format(
                                "There was a type mismatch between the source and target fields: %s(%s) VS %s(%s)",
                                paramClassName,
                                sourceField.getSimpleName().toString(),
                                returnTypeClassName,
                                returnField.getSimpleName().toString()
                        )
                );
            }
        }
    }

    // we only need one set of fields
    // as we've already validated both
    // classes have identical fields
    private static void validateIgnoredFieldsExist(
            final List<Element> paramClassFields,
            final DuhMapMethod annotation,
            final Element interfaceEl
    ) {
        final var paramNames = paramClassFields
                .stream()
                .map(el -> el.getSimpleName().toString())
                .toList();

        for (final var ignoredField : annotation.ignoredFields()) {
            if (!paramNames.contains(ignoredField)) {
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
