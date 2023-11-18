package com.noydb.duhmap.kit;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapBeanType;
import com.noydb.duhmap.annotation.DuhMapMethod;
import com.noydb.duhmap.error.DuhMapException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Types;
import java.util.List;

import static com.noydb.duhmap.kit.DuhMapProcessorUtils.getFullyQualifiedName;

public final class DuhMapAnnotationValidator {

    private DuhMapAnnotationValidator() {
        // do not instantiate
    }

    // must be a method
    // each method must have one parameter
    // each method's parameter must be a class
    public static void run(
            final RoundEnvironment roundEnv,
            final ProcessingEnvironment processingEnv
    ) {
        validateDuhMapAnnotations(processingEnv, roundEnv);
        validateDuhMapMethodAnnotations(roundEnv);
    }

    private static void validateDuhMapAnnotations(
            final ProcessingEnvironment processingEnv,
            final RoundEnvironment roundEnv
    ) {
        final var interfaceEls = roundEnv.getElementsAnnotatedWith(DuhMap.class);
        for (final var el : interfaceEls) {
            final var interfaceEl = (TypeElement) el;
            if (interfaceEl.getKind() != ElementKind.INTERFACE) {
                throw new DuhMapException(
                        String.format(
                                "You may only use @DuhMap annotation with an interface (problem in %s)",
                                getFullyQualifiedName(interfaceEl)
                        )
                );
            }

            if (interfaceEl.getEnclosedElements().stream().anyMatch(e -> e.getKind() != ElementKind.METHOD)) {
                throw new DuhMapException("Interfaces in DuhMap must contain methods only", interfaceEl);
            }

            validateMethods(processingEnv, interfaceEl);

            if (interfaceEl.getAnnotation(DuhMap.class).strictChecks()) {
                DuhMapAnnotationStrictValidator.run(processingEnv, interfaceEl);
            }
        }
    }

    private static void validateMethods(
            final ProcessingEnvironment processingEnv,
            final TypeElement interfaceEl
    ) {
        for (final var enclosedElement : interfaceEl.getEnclosedElements()) {
            final var methodEl = (ExecutableElement) enclosedElement;
            final List<? extends VariableElement> parameters = methodEl.getParameters();
            if (parameters.size() != 1) {
                throw new DuhMapException(
                        "You can only map exactly one class to another in a DuhMap method",
                        methodEl,
                        interfaceEl
                );
            }

            final VariableElement param = parameters.get(0);
            final var parameterType = param.asType();
            final Types types = processingEnv.getTypeUtils();

            // declared is interface or class
            if (parameterType.getKind() != TypeKind.DECLARED) {
                throw new DuhMapException("You can only map classes (to classes) in DuhMap", methodEl, interfaceEl);
            }

            if (types.isAssignable(parameterType, types.getDeclaredType(interfaceEl))) {
                throw new DuhMapException("Interfaces cannot be the source nor target type in DuhMap", methodEl, interfaceEl);
            }
        }
    }

    private static void validateDuhMapMethodAnnotations(final RoundEnvironment roundEnv) {
        for (final Element methodEl : roundEnv.getElementsAnnotatedWith(DuhMapMethod.class)) {
            if (methodEl.getKind() != ElementKind.METHOD) {
                throw new DuhMapException("You may only use DuhMapMethod annotation with a method", (ExecutableElement) methodEl);
            }

            final var enclosingEl = methodEl.getEnclosingElement();
            if (!enclosingEl.getKind().equals(ElementKind.INTERFACE)) {
                throw new DuhMapException(
                        "You may only use a DuhMapMethod annotation inside an interface annotated with @DuhMap",
                        (ExecutableElement) methodEl,
                        (TypeElement) enclosingEl
                );
            }

            final DuhMap interfaceAnnotation = enclosingEl.getAnnotation(DuhMap.class);
            if (interfaceAnnotation == null) {
                throw new DuhMapException(
                        String.format(
                                "You must annotate the interface %s with @DuhMap in order to use @DuhMapMethod",
                                getFullyQualifiedName((TypeElement) enclosingEl)
                        )
                );
            }

            final var isSpringBean = interfaceAnnotation.beanType().equals(DuhMapBeanType.SPRING);
            if (isSpringBean && methodEl.getAnnotation(DuhMapMethod.class).mapList()) {
                throw new DuhMapException(
                        "You cannot currently use mapList=true in conjunction with a Spring Bean due to the list " +
                                "method generation mechanism",
                        (ExecutableElement) methodEl,
                        (TypeElement) enclosingEl
                );
            }

        }
    }
}
