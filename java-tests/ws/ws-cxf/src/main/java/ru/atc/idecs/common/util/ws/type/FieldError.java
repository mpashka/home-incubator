package ru.atc.idecs.common.util.ws.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "FieldError",
        propOrder = {"fieldName", "error"}
)
@Data
public class FieldError implements Serializable {
    private static final long serialVersionUID = -1897519234354384676L;
    @XmlElement(
            required = true
    )
    protected String fieldName;
    @XmlElement(
            required = true
    )
    protected Error error;
}
