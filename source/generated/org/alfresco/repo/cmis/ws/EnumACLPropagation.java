
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumACLPropagation.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumACLPropagation">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="repository-determined"/>
 *     &lt;enumeration value="object-only"/>
 *     &lt;enumeration value="propagate"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumACLPropagation", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumACLPropagation {

    @XmlEnumValue("repository-determined")
    REPOSITORY_DETERMINED("repository-determined"),
    @XmlEnumValue("object-only")
    OBJECT_ONLY("object-only"),
    @XmlEnumValue("propagate")
    PROPAGATE("propagate");
    private final String value;

    EnumACLPropagation(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumACLPropagation fromValue(String v) {
        for (EnumACLPropagation c: EnumACLPropagation.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
