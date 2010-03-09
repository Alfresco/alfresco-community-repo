
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumBaseObjectTypeIds.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumBaseObjectTypeIds">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="cmis:document"/>
 *     &lt;enumeration value="cmis:folder"/>
 *     &lt;enumeration value="cmis:relationship"/>
 *     &lt;enumeration value="cmis:policy"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumBaseObjectTypeIds", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumBaseObjectTypeIds {

    @XmlEnumValue("cmis:document")
    CMIS_DOCUMENT("cmis:document"),
    @XmlEnumValue("cmis:folder")
    CMIS_FOLDER("cmis:folder"),
    @XmlEnumValue("cmis:relationship")
    CMIS_RELATIONSHIP("cmis:relationship"),
    @XmlEnumValue("cmis:policy")
    CMIS_POLICY("cmis:policy");
    private final String value;

    EnumBaseObjectTypeIds(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumBaseObjectTypeIds fromValue(String v) {
        for (EnumBaseObjectTypeIds c: EnumBaseObjectTypeIds.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
