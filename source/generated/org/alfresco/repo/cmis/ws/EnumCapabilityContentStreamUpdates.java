
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumCapabilityContentStreamUpdates.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumCapabilityContentStreamUpdates">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="anytime"/>
 *     &lt;enumeration value="pwconly"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumCapabilityContentStreamUpdates", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumCapabilityContentStreamUpdates {

    @XmlEnumValue("anytime")
    ANYTIME("anytime"),
    @XmlEnumValue("pwconly")
    PWCONLY("pwconly"),
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    EnumCapabilityContentStreamUpdates(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumCapabilityContentStreamUpdates fromValue(String v) {
        for (EnumCapabilityContentStreamUpdates c: EnumCapabilityContentStreamUpdates.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
