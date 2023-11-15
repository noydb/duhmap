package com.noydb.duhmap.processor;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;
import com.noydb.duhmap.error.DuhMapProcessorException;

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

import static com.noydb.duhmap.processor.ProcessorUtils.asTypeElement;
import static com.noydb.duhmap.processor.ProcessorUtils.getName;

@SupportedAnnotationTypes("com.noydb.duhmap.annotation.DuhMap")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class DuhMapAnnotationProcessor extends AbstractProcessor {

    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        ProcessorUtils.performValidations(roundEnv, processingEnv);

        for (final Element el : roundEnv.getElementsAnnotatedWith(DuhMap.class)) {
            final var interfaceEl = (TypeElement) el;
            final var name = getName(interfaceEl);
            final var packageName = ProcessorUtils.getPackageName(interfaceEl);
            final var outputClassName = "Duh" + name;

            final var builder = new StringBuilder();
            builder.append(
                    String.format(
                            ProcessorUtils.CLASS_TEMPLATE,
                            packageName,
                            ProcessorUtils.getGeneratedAnnotation(),
                            outputClassName,
                            name
                    )
            );

            for (final var method : interfaceEl.getEnclosedElements()) {
                mapMethod(builder, method, interfaceEl);
            }

            builder.append("}");

            writeToFile(packageName, outputClassName, builder.toString());
        }

        return true;
    }

    private void mapMethod(final StringBuilder builder, final Element methodEl, final Element interfaceEl) {
        final var methodExEl = ((ExecutableElement) methodEl);
        final var methodName = getName(methodEl);
        final var returnTypeEl = asTypeElement(processingEnv, methodExEl.getReturnType());
        final var sourceClassEl = asTypeElement(processingEnv, methodExEl.getParameters().get(0).asType());
        builder.append(
                String.format(
                        ProcessorUtils.METHOD_TEMPLATE,
                        getName(returnTypeEl),
                        methodName,
                        getName(sourceClassEl)
                )
        );

        final DuhMap ann = interfaceEl.getAnnotation(DuhMap.class);
        if (Arrays.asList(ann.ignoredMethods()).contains(methodName)) {
            builder.append(" return null; }\n");
            return;
        }

        builder.append("\n");
        builder.append("        final ");
        builder.append(returnTypeEl);
        builder.append(" target = new ");
        builder.append(returnTypeEl);
        builder.append("();");
        builder.append("\n");

        // we have validated already that
        // there is only one parameter
        final var paramTypeMirror = methodExEl.getParameters().get(0).asType();

        final var methodAnnotation = methodEl.getAnnotation(DuhMapMethod.class);
        List<String> ignoredFields = new ArrayList<>();
        if (methodAnnotation != null) {
            ignoredFields = Arrays.asList(methodAnnotation.ignoredFields());
        }
        System.out.println(ignoredFields);
        // we know it's a class (DeclaredType.
        // we validated it already) so cast
        mapFields(
                builder,
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
            System.out.println(fieldName);
            if (!field.getKind().isField() || ignoredFields.contains(fieldName)) {
                continue;
            }

            final var fieldNameUppercase = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            builder.append(String.format(ProcessorUtils.SET_TEMPLATE, fieldNameUppercase, fieldNameUppercase));
            builder.append("\n");
        }
    }

    private void writeToFile(final String packageName, final String className, final String content) {
        final Filer filer = processingEnv.getFiler();
        final FileObject fileObject;

        try {
            fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, className + ".java");
        } catch (final IOException e) {
            throw new DuhMapProcessorException(e);
        }

        try (final Writer writer = fileObject.openWriter()) {
            writer.write(content);
        } catch (final IOException e) {
            throw new DuhMapProcessorException(e);
        }
    }
}
