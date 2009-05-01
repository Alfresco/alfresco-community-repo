
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumObjectType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumObjectType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="document"/>
 *     &lt;enumeration value="folder"/>
 *     &lt;enumeration value="relationship"/>
 *     &lt;enumeration value="policy"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumObjectType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumObjectType {

    @XmlEnumValue("document")
    DOCUMENT("document"),
    @XmlEnumValue("folder")
    FOLDER("folder"),
    @XmlEnumValue("relationship")
    RELATIONSHIP("relationship"),
    @XmlEnumValue("policy")
    POLICY("policy");
    private final String value;

    EnumObjectType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumObjectType fromValue(String v) {
        for (EnumObjectType c: EnumObjectType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
