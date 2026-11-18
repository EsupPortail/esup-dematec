package fr.univrouen.poste.domain;

import org.springframework.format.annotation.DateTimeFormat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@DateTimeFormat(pattern = "uuuu-MM-dd'T'HH:mm", fallbackPatterns = {
        "dd/MM/yyyy HH:mm",
        // Fractional-second patterns match a fixed number of digits.
        "uuuu-MM-dd'T'HH:mm:ss[.S]",
        "uuuu-MM-dd'T'HH:mm:ss.SS",
        "uuuu-MM-dd'T'HH:mm:ss.SSS",
        "uuuu-MM-dd'T'HH:mm:ss.SSSS",
        "uuuu-MM-dd'T'HH:mm:ss.SSSSS",
        "uuuu-MM-dd'T'HH:mm:ss.SSSSSS",
        "uuuu-MM-dd'T'HH:mm:ss.SSSSSSS",
        "uuuu-MM-dd'T'HH:mm:ss.SSSSSSSS",
        "uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSS"
})
@interface FormDateTime {
}
