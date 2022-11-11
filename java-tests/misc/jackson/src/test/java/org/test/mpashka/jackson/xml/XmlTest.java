package org.test.mpashka.jackson.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.cfg.MapperConfigBase;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmlTest {

    private static final TypeReference<SoapEnvelope<SimpleBean>> myType = new TypeReference<>(){};

    @Test
    public void test() throws Exception {
        XmlMapper mapper = XmlMapper.builder()
                .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
                .visibility(new VisibilityChecker.Std(
                        JsonAutoDetect.Visibility.NONE,
                        JsonAutoDetect.Visibility.NONE,
                        JsonAutoDetect.Visibility.NONE,
                        JsonAutoDetect.Visibility.NONE,
                        JsonAutoDetect.Visibility.ANY))
//                .annotationIntrospector()
                .build();
/*
        JaxbAnnotationModule module = new JaxbAnnotationModule();
        mapper.registerModule(module);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
*/

/*
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        // if ONLY using JAXB annotations:
        mapper.setAnnotationIntrospector(introspector);
        // if using BOTH JAXB annotations AND Jackson annotations:
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        mapper.setAnnotationIntrospector(AnnotationIntrospector.pair(introspector, secondary));
*/

        mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()) {
            @Override
            public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
                return super.findTypeResolver(config, ac, baseType);
            }

            @Override
            public String findTypeName(AnnotatedClass ac) {
                String typeName = super.findTypeName(ac);
                log.info("Type name for {}: {}", ac, typeName);
                return typeName;
            }

            @Override
            public PropertyName findNameForSerialization(Annotated a) {
                PropertyName nameForSerialization = super.findNameForSerialization(a);
                if (nameForSerialization != null) {
                    log.info("Name for serail {}: {}", a, nameForSerialization);
                }
                else if (a instanceof AnnotatedField) {
                    AnnotatedField af = (AnnotatedField) a;
                    XmlRootElement child = af.getRawType().getAnnotation(XmlRootElement.class);
                    if (child != null) {
                        nameForSerialization = new PropertyName(child.name(), child.namespace());
                    } else {
                        XmlRootElement parent = af.getDeclaringClass().getAnnotation(XmlRootElement.class);
                        if (parent != null) {
                            nameForSerialization = new PropertyName(af.getName(), parent.namespace());
                        }
                    }
                    log.info("Field: {}, Type: {}, RawType: {} -> {}", af.getAnnotated(), af.getType(), af.getRawType(), nameForSerialization);
                }

                return nameForSerialization;
            }

            @Override
            public String findNamespace(MapperConfig<?> config, Annotated ann) {
                String namespace = super.findNamespace(config, ann);
                if (ann instanceof AnnotatedField) {
                    AnnotatedField af = (AnnotatedField) ann;
                    XmlRootElement child = af.getRawType().getAnnotation(XmlRootElement.class);
                    if (child != null) {
                        namespace = child.namespace();
                    } else {
                        XmlRootElement parent = af.getDeclaringClass().getAnnotation(XmlRootElement.class);
                        if (parent != null) {
                            namespace = parent.namespace();
                        }
                    }
                }
                log.info("Namespace for {}: {}", ann, namespace);
                return namespace;
            }

            @Override
            public PropertyName findRootName(AnnotatedClass ac) {
                PropertyName rootName = super.findRootName(ac);
                log.info("Root name {}: {}", ac, rootName);
                return rootName;
            }
        });

        String xml = mapper.writeValueAsString(SoapEnvelope.builder()
                .body(SoapBody.builder()
                        .data(SimpleBean.builder()
                                .x(100)
                                .y(200)
                                .build())
                        .data2(SimpleBean2.builder()
                                .x(400)
                                .y(500)
                                .build())
                        .build())
                .build());
        log.info("Serialize: {}", xml);


        SoapEnvelope<SimpleBean> simpleBean = mapper.readValue(xml, myType);
        log.info("DeSerialize: {}", simpleBean);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    @XmlAccessorType(XmlAccessType.FIELD)
//    @XmlType(name = "", propOrder = {"Root"})
    public static class SoapEnvelope<T> {
        @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
        private SoapBody<T> body;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlRootElement(name = "Body2", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SoapBody<T> {
//        @XmlElement(name = "MyOperation", namespace = "http://customerservice.example.com/")
        private T data;

        private SimpleBean2 data2;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @XmlRootElement(name = "MyOperation", namespace = "http://customerservice.example.com/")
//    @XmlType(name = "MyOperation", namespace = "http://customerservice.example.com/")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SimpleBean {
//        @XmlElement(namespace = "http://customerservice.example.com/")
        private int x = 1;
//        @XmlElement(namespace = "http://customerservice.example.com/")
        private int y = 2;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @XmlRootElement(name = "MyOperation2", namespace = "http://customerservice2.example.com/")
//    @XmlType(name = "MyOperation", namespace = "http://customerservice.example.com/")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SimpleBean2 {
//        @XmlElement(namespace = "http://customerservice.example.com/")
        private int x = 1;
//        @XmlElement(namespace = "http://customerservice.example.com/")
        private int y = 2;
    }
}
