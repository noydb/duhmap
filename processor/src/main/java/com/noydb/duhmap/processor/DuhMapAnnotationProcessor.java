package com.noydb.duhmap.processor;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;
import com.noydb.duhmap.error.DuhMapException;
import com.noydb.duhmap.kit.DuhMapAnnotationValidator;
import com.noydb.duhmap.kit.DuhMapTemplates;
import com.noydb.duhmap.kit.DuhMapProcessorUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.noydb.duhmap.kit.DuhMapProcessorUtils.getFullyQualifiedName;
import static com.noydb.duhmap.kit.DuhMapTemplates.getMethodSignature;
import static com.noydb.duhmap.kit.DuhMapTemplates.getTemplate;
import static com.noydb.duhmap.kit.DuhMapProcessorUtils.asTypeElement;
import static com.noydb.duhmap.kit.DuhMapProcessorUtils.getName;

@SupportedAnnotationTypes({
        "com.noydb.duhmap.annotation.DuhMap",
        "com.noydb.duhmap.annotation.DuhMapMethod"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class DuhMapAnnotationProcessor extends AbstractProcessor {

    public boolean process(
            final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv
    ) {
        DuhMapAnnotationValidator.performValidations(roundEnv, processingEnv);

        for (final Element el : roundEnv.getElementsAnnotatedWith(DuhMap.class)) {
            final var interfaceEl = (TypeElement) el;
            final var annotation = interfaceEl.getAnnotation(DuhMap.class);
            final var name = getName(interfaceEl);
            final var packageName = DuhMapProcessorUtils.getPackageName(interfaceEl);
            final var outputClassName = "Duh" + name;

            final var builder = new StringBuilder();
            builder.append(
                    String.format(
                            getTemplate(annotation),
                            packageName,
                            DuhMapTemplates.getGeneratedAnnotation(),
                            outputClassName,
                            name
                    )
            );

            final DuhMap ann = interfaceEl.getAnnotation(DuhMap.class);
            final var ignoredMethods = Arrays.asList(ann.ignoredMethods());
            for (final var method : interfaceEl.getEnclosedElements()) {
                mapMethod(builder, method, ignoredMethods);
            }

            builder.append("}");

            writeToFile(packageName, outputClassName, builder.toString());
        }

        return true;
    }

    private void mapMethod(
            final StringBuilder builder,
            final Element methodEl,
            final List<String> ignoredMethods
    ) {
        final var methodName = getName(methodEl);
        final var methodAnnotation = methodEl.getAnnotation(DuhMapMethod.class);
        final var methodExEl = ((ExecutableElement) methodEl);
        final var returnTypeEl = asTypeElement(processingEnv, methodExEl.getReturnType());
        final var sourceClassEl = asTypeElement(processingEnv, methodExEl.getParameters().get(0).asType());

        if (ignoredMethods.contains(methodName) || methodAnnotation != null && methodAnnotation.ignore()) {
            builder.append(
                    String.format(
                            DuhMapTemplates.IGNORED_METHOD_SIGNATURE,
                            getFullyQualifiedName(returnTypeEl),
                            methodName,
                            getFullyQualifiedName(sourceClassEl)
                    )
            );

            return;
        }

        builder.append(
                String.format(
                        getMethodSignature(methodAnnotation),
                        // we use fully qualified names so we don't
                        // have to worry about importing
                        getFullyQualifiedName(returnTypeEl),
                        methodName,
                        getFullyQualifiedName(sourceClassEl)
                )
        );

        builder.append("        final ");
        builder.append(returnTypeEl);
        builder.append(" target = new ");
        builder.append(returnTypeEl);
        builder.append("();");
        builder.append("\n");

        // we have validated already that
        // there is only one parameter
        final var paramTypeMirror = methodExEl.getParameters().get(0).asType();

        List<String> ignoredFields = new ArrayList<>();
        if (methodAnnotation != null) {
            ignoredFields = Arrays.asList(methodAnnotation.ignoredFields());
        }
        mapFields(
                builder,
                // we've validated it's a class (DeclaredType)
                ((DeclaredType) paramTypeMirror).asElement(),
                ignoredFields
        );

        builder.append("\n        return target;\n");
        builder.append("    }");
        builder.append("\n");
        builder.append("\n");
    }

    private void mapFields(final StringBuilder builder, final Element paramEl, final List<String> ignoredFields) {
        for (final Element field : paramEl.getEnclosedElements()) {
            final var fieldName = getName(field);
            if (!field.getKind().isField() || ignoredFields.contains(fieldName)) {
                continue;
            }

            final var fieldNameUppercase = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            builder.append(String.format(DuhMapTemplates.SET_METHOD, fieldNameUppercase, fieldNameUppercase));
            builder.append("\n");
        }
    }

    private void writeToFile(final String packageName, final String className, final String content) {
        final Filer filer = processingEnv.getFiler();
        final FileObject fileObject;

        try {
            fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, className + ".java");
        } catch (final IOException e) {
            throw new DuhMapException(
                    String.format(
                            "Error during writing of DuhMap file to source output for class: %s.%s",
                            packageName,
                            className
                    ),
                    e
            );
        }

        try (final Writer writer = fileObject.openWriter()) {
            writer.write(content);
        } catch (final IOException e) {
            throw new DuhMapException(
                    String.format(
                            "Error during writing of DuhMap file to source output for class: %s.%s",
                            packageName,
                            className
                    ),
                    e
            );
        }
    }
}
