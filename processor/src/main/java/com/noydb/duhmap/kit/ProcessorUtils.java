package com.noydb.duhmap.kit;

import com.noydb.duhmap.error.DuhMapException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public final class ProcessorUtils {

    private ProcessorUtils() {
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

    public static List<String> getFields(final Element classEl) {
        final var sourceFields = new ArrayList<String>();
        for (final var field : classEl.getEnclosedElements()) {
            if (field.getKind().isField()) {
                sourceFields.add(getName(field));
            }
        }

        return sourceFields;
    }

}