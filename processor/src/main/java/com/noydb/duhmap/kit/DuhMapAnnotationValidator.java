package com.noydb.duhmap.kit;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.noydb.duhmap.kit.DuhMapProcessorUtils.DUH_MAP_VERSION;
import static com.noydb.duhmap.kit.DuhMapProcessorUtils.asTypeElement;
import static com.noydb.duhmap.kit.DuhMapProcessorUtils.getFullyQualifiedName;
import static com.noydb.duhmap.kit.DuhMapProcessorUtils.getMismatchingFieldsLog;

public final class DuhMapAnnotationValidator {

    private static final String START_WARNING_LOG = String.format("duhmap:%s - WARNING", DUH_MAP_VERSION);
    private static final String END_WARNING_LOG = "NOTE: having strict checks enabled would have caused this build to fail.\n";

    private DuhMapAnnotationValidator() {
        // do not instantiate
    }

    // 1. only an interface can be annotated
    // with @DuhMap
    // 2. it must only contain methods
    // 3. each method must have one parameter
    // 4. each method's parameter (source) and
    // return type (target) must be a class
    public static void run(
            final RoundEnvironment roundEnv,
            final ProcessingEnvironment processingEnv
    ) {
        validateAnnotatedInterfaces(processingEnv, roundEnv);
        validateAnnotatedMethods(roundEnv);
    }

    private static void validateAnnotatedInterfaces(
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

            checkMethods(processingEnv, interfaceEl);
            checkFields(processingEnv, interfaceEl);
        }
    }

    private static void checkMethods(
            final ProcessingEnvironment processingEnv,
            final TypeElement interfaceEl
    ) {
        final var methods = interfaceEl.getEnclosedElements();
        for (final var enclosedElement : methods) {
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

        validateIgnoredMethods(interfaceEl, methods);
    }

    private static void validateAnnotatedMethods(final RoundEnvironment roundEnv) {
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
        }
    }

    private static void checkFields(
            final ProcessingEnvironment processingEnv,
            final TypeElement interfaceEl
    ) {
        for (final var el : interfaceEl.getEnclosedElements()) {
            final var strictFails = new HashSet<DuhMapStrictRule>();

            if (!el.getKind().equals(ElementKind.METHOD)) {
                continue;
            }

            final TypeMirror paramType = ((ExecutableElement) el).getParameters().get(0).asType();
            final var paramEl = (TypeElement) ((DeclaredType) paramType).asElement();
            final var sourceFieldEls = DuhMapProcessorUtils.getFields(paramEl);

            final var targetTypeEl = asTypeElement(
                    processingEnv, ((ExecutableType) el.asType()).getReturnType()
            );
            final var targetFieldEls = DuhMapProcessorUtils.getFields(targetTypeEl);

            if (sourceFieldEls.size() != targetFieldEls.size()) {
                strictFails.add(DuhMapStrictRule.MISMATCHED_FIELD_COUNT);
            }

            final var sourceFieldsSorted = sourceFieldEls
                    .stream()
                    .sorted(Comparator.comparing(DuhMapProcessorUtils::getName))
                    .toList();
            final var targetFieldsSorted = targetFieldEls
                    .stream()
                    .sorted(Comparator.comparing(DuhMapProcessorUtils::getName))
                    .toList();

            for (int i = 0; i < sourceFieldsSorted.size(); i++) {
                final var sourceFieldEl = sourceFieldsSorted.get(i);
                // target may not have as many fields
                // this prevents IndexOutOfBoundsException
                if (i == targetFieldsSorted.size()) {
                    break;
                }

                final var targetFieldEl = targetFieldsSorted.get(i);

                if (!sourceFieldEl.getSimpleName().equals(targetFieldEl.getSimpleName())) {
                    strictFails.add(DuhMapStrictRule.MISMATCHED_FIELD_NAMES);
                }

                final var sourceType = sourceFieldEl.asType();
                final var targetType = targetFieldEl.asType();
                if (!sourceType.equals(targetType)) {
                    strictFails.add(DuhMapStrictRule.MISMATCHED_FIELD_TYPE);
                }
            }

            if (!strictFails.isEmpty()) {
                handleFieldStrictFailure(
                        interfaceEl.getAnnotation(DuhMap.class),
                        String.format(
                                "Field rules: %s failed for %s",
                                strictFails,
                                getMismatchingFieldsLog(
                                        sourceFieldEls,
                                        targetFieldEls,
                                        getFullyQualifiedName(paramEl),
                                        getFullyQualifiedName(targetTypeEl)
                                )
                        ),
                        strictFails
                );
            }

            validateIgnoredFieldsExist(sourceFieldEls, el.getAnnotation(DuhMapMethod.class), interfaceEl);
        }
    }

    // we only need one set of fields
    // as we've already validated both
    // classes have identical fields
    private static void validateIgnoredFieldsExist(
            final List<VariableElement> paramClassFields,
            final DuhMapMethod annotation,
            final TypeElement interfaceEl
    ) {
        if (annotation == null) {
            return;
        }

        final var paramNames = paramClassFields
                .stream()
                .map(DuhMapProcessorUtils::getName)
                .toList();

        final var nonExistentFields = new ArrayList<String>();
        for (final var ignoredField : annotation.ignoredFields()) {
            if (!paramNames.contains(ignoredField)) {
                nonExistentFields.add(ignoredField);
            }
        }

        if (!nonExistentFields.isEmpty()) {
            handleFieldStrictFailure(
                    interfaceEl.getAnnotation(DuhMap.class),
                    String.format(
                            "For interface: %s invalid ignored field(s) %s",
                            getFullyQualifiedName(interfaceEl),
                            nonExistentFields
                    ),
                    DuhMapStrictRule.IGNORED_FIELDS
            );
        }
    }

    private static void validateIgnoredMethods(final TypeElement interfaceEl, final List<? extends Element> methods) {
        final var annotation = interfaceEl.getAnnotation(DuhMap.class);
        final var ignoredMethods = annotation.ignoredMethods();
        // second condition: for some reason,
        // specifying no value for the attribute
        // causes a blank string to come through
        // dunno how to default value in the annotation
        // class to not do it
        if (ignoredMethods == null || (ignoredMethods.length == 1 && ignoredMethods[0].trim().isBlank())) {
            return;
        }

        for (final var ignoredMethod : ignoredMethods) {
            if (methods.stream().noneMatch(e -> e.getSimpleName().toString().equals(ignoredMethod))) {
                handleFieldStrictFailure(
                        annotation,
                        String.format(
                                "The specified ignored method \"%s\" does not exist | %s",
                                ignoredMethod,
                                getFullyQualifiedName(interfaceEl)
                        ),
                        DuhMapStrictRule.IGNORED_METHODS
                );
            }
        }
    }

    private static void handleFieldStrictFailure(
            final DuhMap annotation,
            final String message,
            final DuhMapStrictRule strictRule
    ) {
        if (annotation.strictChecks() && doStrictCheck(annotation, Set.of(strictRule))) {
            throw new StrictDuhMapException(message);
        } else {
            System.out.println(START_WARNING_LOG);
            System.out.println(message);
            System.out.println(END_WARNING_LOG);
        }
    }

    private static void handleFieldStrictFailure(
            final DuhMap annotation,
            final String message,
            final Set<DuhMapStrictRule> strictRules
    ) {
        if (annotation.strictChecks() && doStrictCheck(annotation, strictRules)) {
            throw new StrictDuhMapException(message);
        } else {
            System.out.println(START_WARNING_LOG);
            System.out.println(message);
            System.out.println(END_WARNING_LOG);
        }
    }

    private static boolean doStrictCheck(final DuhMap annotation, final Set<DuhMapStrictRule> strictRules) {
        final DuhMapStrictRule[] ignoredStrictChecks = annotation.ignoredStrictChecks();
        return Arrays
                .stream(ignoredStrictChecks != null ? ignoredStrictChecks : new String[0])
                .noneMatch(ignoredStrictCheck -> {
                    for (final var strictRule : strictRules) {
                        if (strictRule.equals(ignoredStrictCheck)) {
                            return true;
                        }
                    }
                    return false;
                });
    }
}