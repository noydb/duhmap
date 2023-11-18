package com.noydb.duhmap.error;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import static com.noydb.duhmap.kit.DuhMapProcessorUtils.getFullyQualifiedName;

public class DuhMapException extends RuntimeException {

    public DuhMapException(final String message) {
        super(message);
    }

    public DuhMapException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DuhMapException(final String message, final ExecutableElement methodEl, final TypeElement interfaceEl) {
        super(
                String.format(
                        "%s | %s#%s",
                        message,
                        getFullyQualifiedName(interfaceEl),
                        methodEl.getSimpleName()
                )
        );
    }

    public DuhMapException(final String message, final TypeElement interfaceEl) {
        super(
                String.format(
                        "%s | interface: %s",
                        message,
                        getFullyQualifiedName(interfaceEl)
                )
        );
    }

    public DuhMapException(final String message, final ExecutableElement methodEl) {
        super(String.format("%s | method: %s", message, methodEl.getSimpleName()));
    }
}
