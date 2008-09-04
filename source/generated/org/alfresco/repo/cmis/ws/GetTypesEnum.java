
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for getTypesEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="getTypesEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Document"/>
 *     &lt;enumeration value="Folder"/>
 *     &lt;enumeration value="Relationship"/>
 *     &lt;enumeration value="All"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum GetTypesEnum {

    @XmlEnumValue("All")
    ALL("All"),
    @XmlEnumValue("Document")
    DOCUMENT("Document"),
    @XmlEnumValue("Folder")
    FOLDER("Folder"),
    @XmlEnumValue("Relationship")
    RELATIONSHIP("Relationship");
    private final String value;

    GetTypesEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static GetTypesEnum fromValue(String v) {
        for (GetTypesEnum c: GetTypesEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
