
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumRestOutputHeaders.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumRestOutputHeaders">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="contentCopied"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumRestOutputHeaders", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumRestOutputHeaders {

    @XmlEnumValue("contentCopied")
    CONTENT_COPIED("contentCopied");
    private final String value;

    EnumRestOutputHeaders(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumRestOutputHeaders fromValue(String v) {
        for (EnumRestOutputHeaders c: EnumRestOutputHeaders.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
