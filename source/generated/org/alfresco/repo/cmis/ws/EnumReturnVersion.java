
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumReturnVersion.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumReturnVersion">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="this"/>
 *     &lt;enumeration value="latest"/>
 *     &lt;enumeration value="latestmajor"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumReturnVersion", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumReturnVersion {

    @XmlEnumValue("this")
    THIS("this"),
    @XmlEnumValue("latest")
    LATEST("latest"),
    @XmlEnumValue("latestmajor")
    LATESTMAJOR("latestmajor");
    private final String value;

    EnumReturnVersion(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumReturnVersion fromValue(String v) {
        for (EnumReturnVersion c: EnumReturnVersion.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
