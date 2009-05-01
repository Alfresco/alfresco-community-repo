
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumUpdateability.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumUpdateability">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="readonly"/>
 *     &lt;enumeration value="readwrite"/>
 *     &lt;enumeration value="whencheckedout"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumUpdateability", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumUpdateability {

    @XmlEnumValue("readonly")
    READONLY("readonly"),
    @XmlEnumValue("readwrite")
    READWRITE("readwrite"),
    @XmlEnumValue("whencheckedout")
    WHENCHECKEDOUT("whencheckedout");
    private final String value;

    EnumUpdateability(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumUpdateability fromValue(String v) {
        for (EnumUpdateability c: EnumUpdateability.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
