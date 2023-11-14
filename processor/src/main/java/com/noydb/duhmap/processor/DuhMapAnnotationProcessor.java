package com.noydb.duhmap.processor;

import com.noydb.duhmap.annotation.DuhMap;
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
import java.util.Arrays;
import java.util.Set;

@SupportedAnnotationTypes("com.noydb.duhmap.annotation.DuhMap")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class DuhMapAnnotationProcessor extends AbstractProcessor {


    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        ProcessorUtils.performValidations(roundEnv, processingEnv);

        for (final Element element : roundEnv.getElementsAnnotatedWith(DuhMap.class)) {
            final var interfaceEl = (TypeElement) element;
            final var name = interfaceEl.getSimpleName().toString();
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

    private void mapMethod(final StringBuilder builder, final Element method, final Element interfaceEl) {
        final var methodEx = ((ExecutableElement) method);
        final var returnTypeElement = (TypeElement) processingEnv.getTypeUtils().asElement(methodEx.getReturnType());
        final var sourceClassEl = (TypeElement) processingEnv.getTypeUtils().asElement(methodEx.getParameters().get(0).asType());

        builder.append(
                String.format(
                        ProcessorUtils.METHOD_TEMPLATE,
                        returnTypeElement.getSimpleName().toString(),
                        method.getSimpleName().toString(),
                        sourceClassEl.getSimpleName().toString()
                )
        );
        builder.append("\n");
        builder.append("        final ");
        builder.append(returnTypeElement);
        builder.append(" target = new ");
        builder.append(returnTypeElement);
        builder.append("();");
        builder.append("\n");

        // we have validated already that
        // there is only one parameter
        final var paramTypeMirror = methodEx.getParameters().get(0).asType();

        // we know it's a class (DeclaredType,
        // (we validated it already) so cast
        mapFields(builder, ((DeclaredType) paramTypeMirror).asElement(), interfaceEl);

        builder.append("\n        return target;\n");
        builder.append("    }");
        builder.append("\n");
        builder.append("\n");
    }

    private void mapFields(final StringBuilder builder, final Element paramElement, final Element interfaceEl) {
        final var duhMapAnnotation = interfaceEl.getAnnotation(DuhMap.class);
        final var ignoreFields = duhMapAnnotation != null ? duhMapAnnotation.ignoredFields() : new String[]{};

        for (final Element field : paramElement.getEnclosedElements()) {
            if (!field.getKind().isField() || Arrays.stream(ignoreFields).anyMatch(ignoredField -> ignoredField.equals(field.getSimpleName().toString()))) {
                continue;
            }

            final var fieldName = field.getSimpleName().toString();
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
