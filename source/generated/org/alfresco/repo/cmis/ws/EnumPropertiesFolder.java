
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumPropertiesFolder.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumPropertiesFolder">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="cmis:ParentId"/>
 *     &lt;enumeration value="cmis:AllowedChildObjectTypeNames"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumPropertiesFolder", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumPropertiesFolder {

    @XmlEnumValue("cmis:ParentId")
    CMIS_PARENT_ID("cmis:ParentId"),
    @XmlEnumValue("cmis:AllowedChildObjectTypeNames")
    CMIS_ALLOWED_CHILD_OBJECT_TYPE_NAMES("cmis:AllowedChildObjectTypeNames");
    private final String value;

    EnumPropertiesFolder(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumPropertiesFolder fromValue(String v) {
        for (EnumPropertiesFolder c: EnumPropertiesFolder.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
