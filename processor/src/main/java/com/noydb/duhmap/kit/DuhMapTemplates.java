package com.noydb.duhmap.kit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapMethod;
import com.noydb.duhmap.error.DuhMapException;

/*************************************
 DO NOT FORMAT THIS CLASS. IT WILL BREAK
 THE "PRETTINESS" OF THE TEMPLATES.

 There is probably a better way of
 doing this. processing external
 templates maybe.
 /*************************************/
public final class DuhMapTemplates {

    // 0 = fully qualified package
    // 1 = @Generated annotation
    // 2 = class name
    // 3 = original annotated interface
    private static final String DEFAULT_CLASS = """
            package %s;

            import javax.annotation.processing.Generated;
                        
            %spublic final class %s implements %s {

            """;

    // 0 = fully qualified package
    // 1 = @Generated annotation
    // 2 = class name
    // 3 = original annotated interface
    private static final String SPRING_CLASS = """
                package %s;

                import javax.annotation.processing.Generated;
                import org.springframework.stereotype.Component;
                
                %s@Component
                public class %s implements %s {

                """;

    // 0 = fully qualified package
    // 1 = @Generated annotation
    // 2 = class name
    // 3 = original annotated interface
    // 4 = class name
    private static final String STATIC_CLASS = """
            package %s;

                import javax.annotation.processing.Generated;
                import org.springframework.stereotype.Component;
                            
                %spublic final class %s {
                    private %s () { // do not instantiate }
                
            """;

    // 0 = target class
    // 1 = method name
    // 2 = source (parameter) type class
    public static final String METHOD_SIGNATURE = """
                    @Override
                    public %s %s(final %s source) {
                """;

    public static final String NULL_SAFE_METHOD_SIGNATURE =
            METHOD_SIGNATURE +
                    "        if (source == null) return null;\n"
                    + "\n";

    // 0 = target class
    // 1 = method name
    // 2 = source class
    public static final String IGNORED_METHOD_SIGNATURE = """
                    @Override
                    public %s %s(final %s source) { return null; };
                """;

    // 0 = target class
    // 1 = methodName
    // 2 = source class
    // 3 = target class
    // 4 = source class
    // 5 = original/default map method name
    public static final String LIST_METHOD = """
                    public java.util.List<%s> %s(final java.util.List<%s> sources) {
                        final java.util.List<%s> targets = new java.util.ArrayList<>();
                        for (final %s source : sources) {
                            targets.add(%s(source));
                        }
                    
                        return targets;
                    }
                """;

    // 0 = (capitalized) field name
    // 1 = (capitalized) field name
    public static final String FIELD = "        target.set%s(source.get%s());";

    private static final String GENERATED_ANNOTATION = """
                @Generated(
                     value = "com.noydb.duhmap.DuhMapAnnotationProcessor",
                     date = "%s",
                     comments = "Java version: %s | duhmap version: %s"
                )
                        """;

    private DuhMapTemplates() {
        // do not instantiate
    }

    public static String getTemplate(final DuhMap annotation) {
        final var type = annotation.beanType();

        if (type == null || type == DuhMapClassType.DEFAULT) return DEFAULT_CLASS;
        else if (type == DuhMapClassType.SPRING_BEAN) return DuhMapTemplates.SPRING_CLASS;
        else if (type == DuhMapClassType.STATIC) return DuhMapTemplates.STATIC_CLASS;

        throw new DuhMapException("Unable to determine class template for DuhMap");
    }

    public static String getGeneratedAnnotation(final String processorVersion) {
        return String.format(
                DuhMapTemplates.GENERATED_ANNOTATION,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                System.getProperty("java.version"),
                processorVersion
        );
    }

    public static String getMethodSignature(final DuhMapMethod annotation) {
        if (annotation == null) {
            return METHOD_SIGNATURE;
        }

        final var nullSafe = annotation.nullSafe();

        if (nullSafe) return NULL_SAFE_METHOD_SIGNATURE;

        return METHOD_SIGNATURE;
    }
}