package com.noydb.duhmap.kit;

import com.noydb.duhmap.annotation.DuhMap;
import com.noydb.duhmap.annotation.DuhMapBeanType;
import com.noydb.duhmap.error.DuhMapException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
                            
                @Component
                %spublic final class %s implements %s {

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

    // 0 = return type class
    // 1 = method name
    // 2 = source (parameter) type class
    public static final String METHOD_SIGNATURE = """
                    @Override
                    public %s %s(final %s source) {
                """;

    // 0 = (capitalized) field name
    // 1 = (capitalized) field name
    public static final String SET_METHOD = "        target.set%s(source.get%s());";

    private static final String GENERATED_ANNOTATION = """
                @Generated(
                     value = "com.noydb.duhmap.processor.DuhMapAnnotationProcessor",
                     date = "%s",
                     comments = "java version: %s"
                )
                        """;

    private DuhMapTemplates() {
        // do not instantiate
    }

    public static String getTemplate(final DuhMap annotation) {
        final var type = annotation.beanType();
        if (type == null || type == DuhMapBeanType.DEFAULT) {
            return DEFAULT_CLASS;
        }

        if (type == DuhMapBeanType.SPRING) return DuhMapTemplates.SPRING_CLASS;
        else if (type == DuhMapBeanType.STATIC) return DuhMapTemplates.STATIC_CLASS;

        throw new DuhMapException("Unable to determine class template for DuhMap");
    }

    public static String getGeneratedAnnotation() {
        return String.format(
                DuhMapTemplates.GENERATED_ANNOTATION,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                System.getProperty("java.version")
        );
    }
}
