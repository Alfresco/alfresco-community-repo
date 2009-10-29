
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumContentStreamAllowed.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumContentStreamAllowed">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="notallowed"/>
 *     &lt;enumeration value="allowed"/>
 *     &lt;enumeration value="required"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumContentStreamAllowed", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumContentStreamAllowed {

    @XmlEnumValue("notallowed")
    NOTALLOWED("notallowed"),
    @XmlEnumValue("allowed")
    ALLOWED("allowed"),
    @XmlEnumValue("required")
    REQUIRED("required");
    private final String value;

    EnumContentStreamAllowed(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumContentStreamAllowed fromValue(String v) {
        for (EnumContentStreamAllowed c: EnumContentStreamAllowed.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
