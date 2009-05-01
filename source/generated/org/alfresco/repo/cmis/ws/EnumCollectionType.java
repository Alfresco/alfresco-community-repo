
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumCollectionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumCollectionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="rootchildren"/>
 *     &lt;enumeration value="rootdescendants"/>
 *     &lt;enumeration value="unfiled"/>
 *     &lt;enumeration value="checkedout"/>
 *     &lt;enumeration value="typeschildren"/>
 *     &lt;enumeration value="typesdescendants"/>
 *     &lt;enumeration value="query"/>
 *     &lt;enumeration value="changes"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumCollectionType", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumCollectionType {

    @XmlEnumValue("rootchildren")
    ROOTCHILDREN("rootchildren"),
    @XmlEnumValue("rootdescendants")
    ROOTDESCENDANTS("rootdescendants"),
    @XmlEnumValue("unfiled")
    UNFILED("unfiled"),
    @XmlEnumValue("checkedout")
    CHECKEDOUT("checkedout"),
    @XmlEnumValue("typeschildren")
    TYPESCHILDREN("typeschildren"),
    @XmlEnumValue("typesdescendants")
    TYPESDESCENDANTS("typesdescendants"),
    @XmlEnumValue("query")
    QUERY("query"),
    @XmlEnumValue("changes")
    CHANGES("changes");
    private final String value;

    EnumCollectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumCollectionType fromValue(String v) {
        for (EnumCollectionType c: EnumCollectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
