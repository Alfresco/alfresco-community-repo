
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
 *     &lt;enumeration value="root-children"/>
 *     &lt;enumeration value="root-descendants"/>
 *     &lt;enumeration value="unfiled"/>
 *     &lt;enumeration value="checkedout"/>
 *     &lt;enumeration value="types-children"/>
 *     &lt;enumeration value="types-descendants"/>
 *     &lt;enumeration value="query"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumCollectionType")
@XmlEnum
public enum EnumCollectionType {

    @XmlEnumValue("root-children")
    ROOT_CHILDREN("root-children"),
    @XmlEnumValue("root-descendants")
    ROOT_DESCENDANTS("root-descendants"),
    @XmlEnumValue("unfiled")
    UNFILED("unfiled"),
    @XmlEnumValue("checkedout")
    CHECKEDOUT("checkedout"),
    @XmlEnumValue("types-children")
    TYPES_CHILDREN("types-children"),
    @XmlEnumValue("types-descendants")
    TYPES_DESCENDANTS("types-descendants"),
    @XmlEnumValue("query")
    QUERY("query");
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
