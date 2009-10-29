
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
 *     &lt;enumeration value="repositorydetermined"/>
 *     &lt;enumeration value="objectonly"/>
 *     &lt;enumeration value="propagate"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumACLPropagation", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumACLPropagation {

    @XmlEnumValue("repositorydetermined")
    REPOSITORYDETERMINED("repositorydetermined"),
    @XmlEnumValue("objectonly")
    OBJECTONLY("objectonly"),
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
