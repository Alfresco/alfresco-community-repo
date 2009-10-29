
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumCapabilityACL.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumCapabilityACL">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="discover"/>
 *     &lt;enumeration value="manage"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumCapabilityACL", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumCapabilityACL {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("discover")
    DISCOVER("discover"),
    @XmlEnumValue("manage")
    MANAGE("manage");
    private final String value;

    EnumCapabilityACL(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumCapabilityACL fromValue(String v) {
        for (EnumCapabilityACL c: EnumCapabilityACL.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
