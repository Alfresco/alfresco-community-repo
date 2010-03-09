
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumCapabilityJoin.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumCapabilityJoin">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="inneronly"/>
 *     &lt;enumeration value="innerandouter"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumCapabilityJoin", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumCapabilityJoin {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("inneronly")
    INNERONLY("inneronly"),
    @XmlEnumValue("innerandouter")
    INNERANDOUTER("innerandouter");
    private final String value;

    EnumCapabilityJoin(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumCapabilityJoin fromValue(String v) {
        for (EnumCapabilityJoin c: EnumCapabilityJoin.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
