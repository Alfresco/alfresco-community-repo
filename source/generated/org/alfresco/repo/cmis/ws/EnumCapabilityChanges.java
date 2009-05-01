
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumCapabilityChanges.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumCapabilityChanges">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="includeACL"/>
 *     &lt;enumeration value="includeProperties"/>
 *     &lt;enumeration value="includeFolders"/>
 *     &lt;enumeration value="includeDocuments"/>
 *     &lt;enumeration value="includeRelationships"/>
 *     &lt;enumeration value="includePolicies"/>
 *     &lt;enumeration value="all"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumCapabilityChanges", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumCapabilityChanges {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("includeACL")
    INCLUDE_ACL("includeACL"),
    @XmlEnumValue("includeProperties")
    INCLUDE_PROPERTIES("includeProperties"),
    @XmlEnumValue("includeFolders")
    INCLUDE_FOLDERS("includeFolders"),
    @XmlEnumValue("includeDocuments")
    INCLUDE_DOCUMENTS("includeDocuments"),
    @XmlEnumValue("includeRelationships")
    INCLUDE_RELATIONSHIPS("includeRelationships"),
    @XmlEnumValue("includePolicies")
    INCLUDE_POLICIES("includePolicies"),
    @XmlEnumValue("all")
    ALL("all");
    private final String value;

    EnumCapabilityChanges(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumCapabilityChanges fromValue(String v) {
        for (EnumCapabilityChanges c: EnumCapabilityChanges.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
