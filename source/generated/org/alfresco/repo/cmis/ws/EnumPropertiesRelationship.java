
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
 *     &lt;enumeration value="cmis:SourceId"/>
 *     &lt;enumeration value="cmis:TargetId"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumPropertiesRelationship", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumPropertiesRelationship {

    @XmlEnumValue("cmis:SourceId")
    CMIS_SOURCE_ID("cmis:SourceId"),
    @XmlEnumValue("cmis:TargetId")
    CMIS_TARGET_ID("cmis:TargetId");
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
