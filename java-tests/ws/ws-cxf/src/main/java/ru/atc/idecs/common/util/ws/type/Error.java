package ru.atc.idecs.common.util.ws.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Error",propOrder = {"code", "message"})
@Data
public class Error implements Serializable {
    private static final long serialVersionUID = -189751945691384676L;
    protected long code;
    @XmlElement(required = true)
    protected String message;
}
