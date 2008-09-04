
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for typesOfFileableObjectsEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="typesOfFileableObjectsEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Documents"/>
 *     &lt;enumeration value="Folders"/>
 *     &lt;enumeration value="Policies"/>
 *     &lt;enumeration value="Any"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum TypesOfFileableObjectsEnum {

    @XmlEnumValue("Any")
    ANY("Any"),
    @XmlEnumValue("Documents")
    DOCUMENTS("Documents"),
    @XmlEnumValue("Folders")
    FOLDERS("Folders"),
    @XmlEnumValue("Policies")
    POLICIES("Policies");
    private final String value;

    TypesOfFileableObjectsEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TypesOfFileableObjectsEnum fromValue(String v) {
        for (TypesOfFileableObjectsEnum c: TypesOfFileableObjectsEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
