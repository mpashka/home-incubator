package ru.atc.idecs.common.util.ws.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractResponse",propOrder = {"error"})
@Data
public class AbstractResponse implements Serializable {
    private static final long serialVersionUID = 1891357467354384676L;
    @XmlElement(required = true)
    protected Error error;
}
