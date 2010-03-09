
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumPropertiesRelationship.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumPropertiesRelationship">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="cmis:sourceId"/>
 *     &lt;enumeration value="cmis:targetId"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumPropertiesRelationship", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumPropertiesRelationship {

    @XmlEnumValue("cmis:sourceId")
    CMIS_SOURCE_ID("cmis:sourceId"),
    @XmlEnumValue("cmis:targetId")
    CMIS_TARGET_ID("cmis:targetId");
    private final String value;

    EnumPropertiesRelationship(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumPropertiesRelationship fromValue(String v) {
        for (EnumPropertiesRelationship c: EnumPropertiesRelationship.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
