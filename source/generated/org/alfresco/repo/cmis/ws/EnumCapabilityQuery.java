
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumCapabilityQuery.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumCapabilityQuery">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="metadataonly"/>
 *     &lt;enumeration value="fulltextonly"/>
 *     &lt;enumeration value="bothseparate"/>
 *     &lt;enumeration value="bothcombined"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumCapabilityQuery", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumCapabilityQuery {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("metadataonly")
    METADATAONLY("metadataonly"),
    @XmlEnumValue("fulltextonly")
    FULLTEXTONLY("fulltextonly"),
    @XmlEnumValue("bothseparate")
    BOTHSEPARATE("bothseparate"),
    @XmlEnumValue("bothcombined")
    BOTHCOMBINED("bothcombined");
    private final String value;

    EnumCapabilityQuery(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumCapabilityQuery fromValue(String v) {
        for (EnumCapabilityQuery c: EnumCapabilityQuery.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
