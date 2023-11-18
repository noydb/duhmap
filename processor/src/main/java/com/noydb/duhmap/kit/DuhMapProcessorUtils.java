package com.noydb.duhmap.kit;

import com.noydb.duhmap.error.DuhMapException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public final class DuhMapProcessorUtils {

    private DuhMapProcessorUtils() {
        // do not instantiate
    }

    public static String getPackageName(final TypeElement interfaceOrClassEl) {
        final var enclosingElement = interfaceOrClassEl.getEnclosingElement();

        if (enclosingElement instanceof PackageElement packageElement) {
            return packageElement.getQualifiedName().toString();
        }

        throw new DuhMapException("DuhMap failed to determine the package");
    }

    public static TypeElement asTypeElement(final ProcessingEnvironment processingEnv, final TypeMirror tm) {
        return (TypeElement) processingEnv.getTypeUtils().asElement(tm);
    }

    public static String getName(final Element element) {
        return element.getSimpleName().toString();
    }

    public static List<Element> getFields(final Element classEl) {
        final var sourceFields = new ArrayList<Element>();
        for (final var field : classEl.getEnclosedElements()) {
            if (field.getKind().isField()) {
                sourceFields.add(field);
            }
        }

        return sourceFields;
    }

    public static String getFullyQualifiedName(final TypeElement element) {
        return String.format(
                "%s.%s",
                element.getEnclosingElement().toString(),
                element.getSimpleName().toString()
        );
    }

    public static void writeToFile(
            final ProcessingEnvironment processingEnv,
            final String packageName,
            final String className,
            final String content
    ) {
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
