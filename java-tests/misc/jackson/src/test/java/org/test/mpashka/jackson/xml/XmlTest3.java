package org.test.mpashka.jackson.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
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
public class XmlTest3 {
    private static final String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

    private static final TypeReference<SoapEnvelope<String, Map<String, String>>> requestType = new TypeReference<>(){};

    @Test
    public void test() throws Exception {
        XmlMapper mapper = XmlMapper.builder()
                .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
                .visibility(new VisibilityChecker.Std(JsonAutoDetect.Visibility.NONE,
                        JsonAutoDetect.Visibility.NONE,
                        JsonAutoDetect.Visibility.NONE,
                        JsonAutoDetect.Visibility.NONE,
                        JsonAutoDetect.Visibility.ANY))
                .annotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()) {
                    @Override
                    public String findNamespace(MapperConfig<?> config, Annotated ann) {
                        String namespace = super.findNamespace(config, ann);
                        return namespace != null ? namespace : "http://idecs.atc.ru/config/ws/";
                    }
                })
                .build();
        mapper.registerModule(new JaxbAnnotationModule());

        var envelope = mapper.readValue("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                        "<soap:Body><ns2:listParametersRequest xmlns:ns2=\"http://idecs.atc.ru/config/ws/\"><filter>my-filt</filter>" +
                        "<location>my-loc</location></ns2:listParametersRequest></soap:Body></soap:Envelope>"
                , requestType);

        log.info("Soap: {}", envelope);

        SoapEnvelope<String, Map> response = new SoapEnvelope<>();
        response.setBody(
                Map.of("listParametersResponse",
                        Map.of("error", Map.of("code", "0", "message", "operation completed"),
                                "list", Map.of("param", Map.of(
                                        "name", "geps.RP_CLIENT_TYPE",
                                        "type", "S",
                                        "description", "Description",
                                        "strVal", "SMEV2",
                                        "intVal", "0"
                                ))
                        )));

        String result = mapper.writeValueAsString(response);
        log.info("Result: {}", result);
    }

    @Data
    @XmlRootElement(name = "Envelope", namespace = SOAP_NAMESPACE)
    public static class SoapEnvelope<K, V> {
        @XmlElement(name = "Body", namespace = SOAP_NAMESPACE)
        private Map<K, V> body;
    }
}
