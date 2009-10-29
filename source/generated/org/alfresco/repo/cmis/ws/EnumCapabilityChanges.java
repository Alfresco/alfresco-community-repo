
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumCapabilityChanges.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumCapabilityChanges">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="objectidsonly"/>
 *     &lt;enumeration value="properties"/>
 *     &lt;enumeration value="all"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumCapabilityChanges", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumCapabilityChanges {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("objectidsonly")
    OBJECTIDSONLY("objectidsonly"),
    @XmlEnumValue("properties")
    PROPERTIES("properties"),
    @XmlEnumValue("all")
    ALL("all");
    private final String value;

    EnumCapabilityChanges(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumCapabilityChanges fromValue(String v) {
        for (EnumCapabilityChanges c: EnumCapabilityChanges.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
