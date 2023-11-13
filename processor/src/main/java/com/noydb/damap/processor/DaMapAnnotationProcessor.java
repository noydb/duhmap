package com.noydb.damap.processor;

import com.noydb.damap.annotation.DuhMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

// TODO: potentially don't implement provided interface and
// then make methods static?
@SupportedAnnotationTypes("com.noydb.damap.annotation.DuhMap")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class DaMapAnnotationProcessor extends AbstractProcessor {

    // 0 = fully qualified package
    // 1 = class name
    // 2 = original annotated interface
    private final String CLASS_TOP_TEMPLATE = """
            package %s;

            public final class %s implements %s {

            """;

    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        performValidations(roundEnv);

        System.out.println("SDOIFYHSDIOFHSDJKLFSHDFJKSHGDFJKSGHDFJKSDFG");
        for (final Element element : roundEnv.getElementsAnnotatedWith(DuhMap.class)) {
            final var interfaceEl = (TypeElement) element;
            final var name = interfaceEl.getSimpleName().toString();
            final var packageName = getPackageName(interfaceEl) + ".impl";
            System.out.println("wedfsfddsf " + packageName);
            final var outputClassName = "Duh" + name;

            final var builder = new StringBuilder();
            builder.append(
                    String.format(
                            CLASS_TOP_TEMPLATE,
                            packageName,
                            outputClassName,
                            name
                    )
            );

            for (final var method : interfaceEl.getEnclosedElements()) {
                builder.append(mapMethod(method));
            }

            builder.append("}");

            System.out.println(builder);
            writeToFile(packageName, outputClassName, builder.toString());
        }

        return true;
    }

    private String getPackageName(TypeElement interfaceEl) {
        final var enclosingElement = interfaceEl.getEnclosingElement();

        if (enclosingElement instanceof PackageElement packageElement) {
            return packageElement.getQualifiedName().toString();
        }

        throw new RuntimeException("DuhMap failed to determine the package");
    }

    private String mapMethod(final Element method) {
        final var methodEx = ((ExecutableElement) method);

        TypeMirror returnType = methodEx.getReturnType();
        TypeElement returnTypeElement = (TypeElement) processingEnv.getTypeUtils().asElement(returnType);
        String returnTypeName = returnTypeElement.getSimpleName().toString();

        final var sourceClassEl = (TypeElement) processingEnv.getTypeUtils().asElement(methodEx.getParameters().get(0).asType());
        String sourceClassName = sourceClassEl.getSimpleName().toString();
        final var METHOD_TEMPLATE = "    public %s %s(%s source) {";
        final var SET_TEMPLATE = "        target.set%s(source.get%s());";
        final var methodName = method.getSimpleName().toString();

        final var builder = new StringBuilder(
                String.format(
                        METHOD_TEMPLATE,
                        returnTypeName,
                        methodName,
                        sourceClassName
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
        final var parameter = methodEx.getParameters().get(0);

        final var paramTypeMirror = parameter.asType();
        // we know it's a class (DeclaredType)
        // we validated it already so cast
        final Element parameterTypeElement = ((DeclaredType) paramTypeMirror).asElement();
        final List<? extends Element> fields = (parameterTypeElement).getEnclosedElements();

        for (final Element field : fields) {
            if (field.getKind().isField()) {
                final var fieldName = field.getSimpleName().toString();
                final String fieldNameUppercase = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

                builder.append(String.format(SET_TEMPLATE, fieldNameUppercase, fieldNameUppercase));
                builder.append("\n");
            }
        }

        builder.append("\n        return target;\n");
        builder.append("    }");
        builder.append("\n");

        builder.append("\n");
        return builder.toString();
    }

    // must be a method
    // each method must have one parameter
    // each method's parameter must be a class
    private void performValidations(final RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(DuhMap.class)) {
            // Validate that interfaces are the top-level elements
            if (element.getKind() != ElementKind.INTERFACE) {
                throw new RuntimeException("You may only use DuhMap annotation with an interface");
            }

            if (element.getEnclosedElements().stream().anyMatch(e -> e.getKind() != ElementKind.METHOD)) {
                throw new RuntimeException("Interfaces in DuhMap must contain methods only");
            }

            for (final var enclosedElement : element.getEnclosedElements()) {
                final var methodEl = (ExecutableElement) enclosedElement;

                List<? extends VariableElement> parameters = methodEl.getParameters();
                if (parameters.size() != 1) {
                    throw new RuntimeException("You can only map exactly one class to another in a DuhMap method");
                }

                final VariableElement param = parameters.get(0);
                final var parameterType = param.asType();
                final Types types = processingEnv.getTypeUtils();

                // Validate that the parameter is a class
                if (parameterType.getKind() != TypeKind.DECLARED) {
                    throw new RuntimeException("You can only map classes (to classes) in DuhMap");
                }

                if (types.isAssignable(parameterType, types.getDeclaredType((TypeElement) element))) {
                    throw new RuntimeException("Interfaces cannot be mapped in DuhMap");
                }
            }

            // TODO: Validate the classes have equal methods or not
        }
    }

    private void writeToFile(final String packageName, final String className, final String content) {
        final Filer filer = processingEnv.getFiler();
        final FileObject fileObject;

        try {
            fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, className + ".java");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (final Writer writer = fileObject.openWriter()) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
