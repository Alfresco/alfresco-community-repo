
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumCapabilityFullText.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumCapabilityFullText">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="fulltextonly"/>
 *     &lt;enumeration value="fulltextandstructured"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumCapabilityFullText")
@XmlEnum
public enum EnumCapabilityFullText {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("fulltextonly")
    FULLTEXTONLY("fulltextonly"),
    @XmlEnumValue("fulltextandstructured")
    FULLTEXTANDSTRUCTURED("fulltextandstructured");
    private final String value;

    EnumCapabilityFullText(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumCapabilityFullText fromValue(String v) {
        for (EnumCapabilityFullText c: EnumCapabilityFullText.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
