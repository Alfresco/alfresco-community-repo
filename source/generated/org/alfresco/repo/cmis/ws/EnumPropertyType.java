
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumPropertyType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumPropertyType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="boolean"/>
 *     &lt;enumeration value="id"/>
 *     &lt;enumeration value="integer"/>
 *     &lt;enumeration value="datetime"/>
 *     &lt;enumeration value="decimal"/>
 *     &lt;enumeration value="html"/>
 *     &lt;enumeration value="string"/>
 *     &lt;enumeration value="uri"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumPropertyType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumPropertyType {

    @XmlEnumValue("boolean")
    BOOLEAN("boolean"),
    @XmlEnumValue("id")
    ID("id"),
    @XmlEnumValue("integer")
    INTEGER("integer"),
    @XmlEnumValue("datetime")
    DATETIME("datetime"),
    @XmlEnumValue("decimal")
    DECIMAL("decimal"),
    @XmlEnumValue("html")
    HTML("html"),
    @XmlEnumValue("string")
    STRING("string"),
    @XmlEnumValue("uri")
    URI("uri");
    private final String value;

    EnumPropertyType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumPropertyType fromValue(String v) {
        for (EnumPropertyType c: EnumPropertyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
