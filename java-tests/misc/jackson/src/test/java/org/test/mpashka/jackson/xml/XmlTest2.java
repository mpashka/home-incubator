package org.test.mpashka.jackson.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmlTest2 {
    @Test
    public void test() throws Exception {
        XmlMapper mapper = XmlMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .defaultUseWrapper(false)
                // enable/disable Features, change AnnotationIntrospector
                .build();
        mapper.registerModule(new JaxbAnnotationModule());


//        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
        StringWriter out = new StringWriter();
        XMLStreamWriter sw = xmlOutputFactory.createXMLStreamWriter(out);

        sw.writeStartDocument();
        sw.setPrefix("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        sw.writeStartElement("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
        sw.writeNamespace("soap", "http://schemas.xmlsoap.org/soap/envelope/");
//        sw.writeDefaultNamespace("http://schemas.xmlsoap.org/soap/envelope/");
//        sw.setDefaultNamespace("http://schemas.xmlsoap.org/soap/envelope/");
//        sw.writeStartElement("soap", "Envelope", "http://schemas.xmlsoap.org/soap/envelope/");
//        sw.writeAttribute("xmlns", "http://schemas.xmlsoap.org/soap/envelope/", "soap", "http://schemas.xmlsoap.org/soap/envelope/");
        sw.writeStartElement("http://schemas.xmlsoap.org/soap/envelope/", "Body");

// Write whatever content POJOs...
//        sw.setPrefix("ns2", "http://idecs.atc.ru/config/ws/");
//        sw.writeStartElement("http://idecs.atc.ru/config/ws/", "test");
//        sw.writeNamespace("ns", "http://idecs.atc.ru/config/ws/");
////        sw.setDefaultNamespace("http://idecs.atc.ru/config/ws/");
//        sw.writeStartElement("test2");
//        sw.setDefaultNamespace("");
        ListParametersRequest request = new ListParametersRequest();
        request.setFilter("fil1");
        request.setLocation("loc1");
        mapper.writeValue(sw, request);
//        sw.writeEndElement();
//        sw.writeEndElement();
        sw.writeEndElement();
        sw.writeEndElement();
        sw.writeEndDocument();
        sw.close();

        log.info("Res: {}", out.toString());
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
//            name = "ListParametersRequest",
            propOrder = {"filter", "location"},
            namespace = "http://idecs.atc.ru/config/ws/"
    )
    @XmlRootElement(
            name = "listParametersRequest2"
            , namespace = "http://idecs.atc.ru/config/ws/"
    )
    public static class ListParametersRequest implements Serializable {
        @XmlElement(
                required = true
                , namespace = "http://idecs.atc.ru/config/ws/"
        )
        protected String filter;
        @XmlElement(
                namespace = "http://idecs.atc.ru/config/ws/"
        )
        protected String location;

        public ListParametersRequest() {
        }

        public String getFilter() {
            return this.filter;
        }

        public void setFilter(String value) {
            this.filter = value;
        }

        public String getLocation() {
            return this.location;
        }

        public void setLocation(String value) {
            this.location = value;
        }
    }
}
