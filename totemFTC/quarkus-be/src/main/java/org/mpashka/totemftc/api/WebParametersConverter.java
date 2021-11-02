package org.mpashka.totemftc.api;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

@Provider
public class WebParametersConverter implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType == LocalDateTime.class) {
            DateTimeFormatter dateTimeFormatter = Arrays
                    .stream(annotations)
                    .filter(a -> a.annotationType() == DateTimeFormat.class)
                    .findAny()
                    .map(a -> DateTimeFormatter.ofPattern(((DateTimeFormat) a).value()))
                    .orElse(Utils.DATE_TIME_FORMATTER);
            return (ParamConverter<T>) new MyDateTimeConverter(dateTimeFormatter);
        }
        return null;
    }

    static class MyDateTimeConverter implements ParamConverter<LocalDateTime> {

        private DateTimeFormatter dateTimeFormatter;

        public MyDateTimeConverter(DateTimeFormatter dateTimeFormatter) {
            this.dateTimeFormatter = dateTimeFormatter;
        }

        @Override
        public LocalDateTime fromString(String value) {
            return LocalDateTime.from(dateTimeFormatter.parse(value));
        }

        @Override
        public String toString(LocalDateTime value) {
            return dateTimeFormatter.format(value);
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER })
    public @interface DateTimeFormat {
        String value() default Utils.DATE_TIME_FORMAT;
    }
}

