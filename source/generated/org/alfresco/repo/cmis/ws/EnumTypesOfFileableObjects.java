
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumTypesOfFileableObjects.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumTypesOfFileableObjects">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="documents"/>
 *     &lt;enumeration value="folders"/>
 *     &lt;enumeration value="policies"/>
 *     &lt;enumeration value="any"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumTypesOfFileableObjects", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumTypesOfFileableObjects {

    @XmlEnumValue("documents")
    DOCUMENTS("documents"),
    @XmlEnumValue("folders")
    FOLDERS("folders"),
    @XmlEnumValue("policies")
    POLICIES("policies"),
    @XmlEnumValue("any")
    ANY("any");
    private final String value;

    EnumTypesOfFileableObjects(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumTypesOfFileableObjects fromValue(String v) {
        for (EnumTypesOfFileableObjects c: EnumTypesOfFileableObjects.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
