package org.mpashka.totemftc.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(WebParametersConverter.class);

    private static final MyDateTimeConverter DEFAULT_DATE_TIME_FORMATTER = new MyDateTimeConverter(Utils.DATE_TIME_FORMATTER);

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
//        log.debug("::getConverter({})", rawType);
        if (rawType == LocalDateTime.class) {
            DateTimeFormatter dateTimeFormatter = Arrays
                    .stream(annotations)
                    .filter(a -> a.annotationType() == DateTimeFormat.class)
                    .findAny()
                    .map(a -> DateTimeFormatter.ofPattern(((DateTimeFormat) a).value()))
                    .orElse(null);

            return (ParamConverter<T>) (dateTimeFormatter == null ? DEFAULT_DATE_TIME_FORMATTER : new MyDateTimeConverter(dateTimeFormatter));
        }
        return null;
    }

    public static class MyDateTimeConverter implements ParamConverter<LocalDateTime> {

        private DateTimeFormatter dateTimeFormatter;

        public MyDateTimeConverter(DateTimeFormatter dateTimeFormatter) {
            this.dateTimeFormatter = dateTimeFormatter;
        }

        @Override
        public LocalDateTime fromString(String value) {
            try {
                return LocalDateTime.from(dateTimeFormatter.parse(value));
            } catch (Exception e) {
                log.error("Error converting {}", value, e);
                throw new RuntimeException("Error converting LocalDateTime", e);
            }
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

