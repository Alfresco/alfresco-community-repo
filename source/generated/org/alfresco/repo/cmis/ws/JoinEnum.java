
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for joinEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="joinEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="noJoin"/>
 *     &lt;enumeration value="innerOnly"/>
 *     &lt;enumeration value="innerAndOuter"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum JoinEnum {

    @XmlEnumValue("innerAndOuter")
    INNER_AND_OUTER("innerAndOuter"),
    @XmlEnumValue("innerOnly")
    INNER_ONLY("innerOnly"),
    @XmlEnumValue("noJoin")
    NO_JOIN("noJoin");
    private final String value;

    JoinEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static JoinEnum fromValue(String v) {
        for (JoinEnum c: JoinEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
