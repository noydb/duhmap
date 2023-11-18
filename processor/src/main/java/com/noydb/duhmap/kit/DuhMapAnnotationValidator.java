package com.noydb.duhmap.kit;

import com.noydb.duhmap.annotation.DuhMap;
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

import static com.noydb.duhmap.kit.DuhMapAnnotationStrictValidator.validate;

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

            if (element.getAnnotation(DuhMap.class).strictChecks()) {
                validate(element, processingEnv);
            }
        }

        validateMethodAnnotations(roundEnv);
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
}
