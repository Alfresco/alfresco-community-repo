
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumUnfileNonfolderObjects.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumUnfileNonfolderObjects">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="unfile"/>
 *     &lt;enumeration value="deletesinglefiled"/>
 *     &lt;enumeration value="delete"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumUnfileNonfolderObjects", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumUnfileNonfolderObjects {

    @XmlEnumValue("unfile")
    UNFILE("unfile"),
    @XmlEnumValue("deletesinglefiled")
    DELETESINGLEFILED("deletesinglefiled"),
    @XmlEnumValue("delete")
    DELETE("delete");
    private final String value;

    EnumUnfileNonfolderObjects(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumUnfileNonfolderObjects fromValue(String v) {
        for (EnumUnfileNonfolderObjects c: EnumUnfileNonfolderObjects.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
