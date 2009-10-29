
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumUsers.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumUsers">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="cmis:user"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumUsers", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumUsers {


    /**
     * 
     * 						This user can be used on setting ACLs to specify
     * 						the permission this
     * 						user context should have.
     * 			
     * 
     */
    @XmlEnumValue("cmis:user")
    CMIS_USER("cmis:user");
    private final String value;

    EnumUsers(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumUsers fromValue(String v) {
        for (EnumUsers c: EnumUsers.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
