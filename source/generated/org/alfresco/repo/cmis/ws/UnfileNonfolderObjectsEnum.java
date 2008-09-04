
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for unfileNonfolderObjectsEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="unfileNonfolderObjectsEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="unfile"/>
 *     &lt;enumeration value="deleteSingleFiled"/>
 *     &lt;enumeration value="delete"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum UnfileNonfolderObjectsEnum {

    @XmlEnumValue("delete")
    DELETE("delete"),
    @XmlEnumValue("deleteSingleFiled")
    DELETE_SINGLE_FILED("deleteSingleFiled"),
    @XmlEnumValue("unfile")
    UNFILE("unfile");
    private final String value;

    UnfileNonfolderObjectsEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UnfileNonfolderObjectsEnum fromValue(String v) {
        for (UnfileNonfolderObjectsEnum c: UnfileNonfolderObjectsEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
