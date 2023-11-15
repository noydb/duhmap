package com.noydb.duhmap.processor;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;
import com.noydb.duhmap.error.DuhMapException;
import com.noydb.duhmap.error.StrictDuhMapException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;

import static com.noydb.duhmap.processor.ProcessorUtils.asTypeElement;
import static com.noydb.duhmap.processor.ProcessorUtils.getName;

public final class DuhMapAnnotationValidator {

    private DuhMapAnnotationValidator() {
        // do not instantiate
    }

    // must be a method
    // each method must have one parameter
    // each method's parameter must be a class
    public static void performValidations(
            final RoundEnvironment roundEnv,
            final ProcessingEnvironment processingEnv
    ) {
        final var interfaceEls = roundEnv.getElementsAnnotatedWith(DuhMap.class);

        for (final var element : interfaceEls) {
            if (element.getKind() != ElementKind.INTERFACE) {
                throw new DuhMapException("You may only use DuhMap annotation with an interface");
            }

            if (element.getEnclosedElements().stream().anyMatch(e -> e.getKind() != ElementKind.METHOD)) {
                throw new DuhMapException("Interfaces in DuhMap must contain methods only");
            }

            for (final var enclosedElement : element.getEnclosedElements()) {
                final var methodEl = (ExecutableElement) enclosedElement;

                List<? extends VariableElement> parameters = methodEl.getParameters();
                if (parameters.size() != 1) {
                    throw new DuhMapException("You can only map exactly one class to another in a DuhMap method");
                }

                final VariableElement param = parameters.get(0);
                final var parameterType = param.asType();
                final Types types = processingEnv.getTypeUtils();

                // declared is interface or class
                if (parameterType.getKind() != TypeKind.DECLARED) {
                    throw new DuhMapException("You can only map classes (to classes) in DuhMap");
                }

                if (types.isAssignable(parameterType, types.getDeclaredType((TypeElement) element))) {
                    throw new DuhMapException("Interfaces cannot be mapped in DuhMap");
                }
            }
        }

        validateMethodAnnotations(roundEnv);

        for (final var el : interfaceEls) {
            final var annotation = el.getAnnotation(DuhMap.class);
            if (!annotation.strictChecks()) {
                return;
            }

            validateStrictly(el, processingEnv);
        }
    }

    private static void validateMethodAnnotations(final RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(DuhMapMethod.class)) {
            if (element.getKind() != ElementKind.METHOD) {
                throw new DuhMapException("You may only use DuhMapMethod annotation with a method");
            }

            final var enclosingEl = element.getEnclosingElement();
            if (!enclosingEl.getKind().equals(ElementKind.INTERFACE)) {
                throw new DuhMapException(
                        "You may only use a DuhMapMethod annotation inside an interface annotated with @DuhMap"
                );
            }

            final DuhMap interfaceAnnotation = enclosingEl.getAnnotation(DuhMap.class);
            if (interfaceAnnotation == null) {
                throw new DuhMapException(
                        "You must annotate your interface " + ProcessorUtils.getName(enclosingEl) + " with @DuhMap in order to use @DuhMapMethod"
                );
            }
        }
    }

    private static void validateStrictly(final Element interfaceEl, final ProcessingEnvironment processingEnv) {
        for (final var el : interfaceEl.getEnclosedElements()) {
            if (!el.getKind().equals(ElementKind.METHOD)) {
                continue;
            }

            final TypeMirror paramType = ((ExecutableElement) el).getParameters().get(0).asType();
            final var paramEl = (TypeElement) ((DeclaredType) paramType).asElement();
            final var paramClassFields = ProcessorUtils.getFields(paramEl);

            final var returnTypeEl = asTypeElement(
                    processingEnv, ((ExecutableType) el.asType()).getReturnType()
            );
            final var returnTypeClassFields = ProcessorUtils.getFields(returnTypeEl);

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
        final var paramClassName = getName(paramEl);
        final var paramFieldCount = paramClassFields.size();
        final var returnTypeFieldCount = returnTypeClassFields.size();
        final var returnTypeClassName = getName(returnTypeEl);

        if (paramFieldCount != returnTypeFieldCount) {
            throw new StrictDuhMapException(
                    String.format(
                            "There was a mismatch between the number of fields for the classes: %s(%s) %s(%s)",
                            returnTypeClassName,
                            returnTypeFieldCount,
                            paramClassName,
                            paramFieldCount
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
            System.out.println(ignoredField);
            if (!paramClassFields.contains(ignoredField)) {
                throw new StrictDuhMapException(
                        String.format(
                                "For interface: %s the ignored field %s does not exist in the source and target classes",
                                getName(interfaceEl),
                                ignoredField
                        )
                );
            }
        }
    }
}
