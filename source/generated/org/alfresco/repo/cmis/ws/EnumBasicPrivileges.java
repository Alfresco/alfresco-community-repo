
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumBasicPrivileges.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumBasicPrivileges">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CMIS.BasicPermission.Read"/>
 *     &lt;enumeration value="CMIS.BasicPermission.Write"/>
 *     &lt;enumeration value="CMIS.BasicPermission.All"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumBasicPrivileges", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumBasicPrivileges {

    @XmlEnumValue("CMIS.BasicPermission.Read")
    CMIS_BASIC_PERMISSION_READ("CMIS.BasicPermission.Read"),
    @XmlEnumValue("CMIS.BasicPermission.Write")
    CMIS_BASIC_PERMISSION_WRITE("CMIS.BasicPermission.Write"),
    @XmlEnumValue("CMIS.BasicPermission.All")
    CMIS_BASIC_PERMISSION_ALL("CMIS.BasicPermission.All");
    private final String value;

    EnumBasicPrivileges(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumBasicPrivileges fromValue(String v) {
        for (EnumBasicPrivileges c: EnumBasicPrivileges.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
