
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for typesOfObjectsEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="typesOfObjectsEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Folders"/>
 *     &lt;enumeration value="FoldersAndDocumets"/>
 *     &lt;enumeration value="Documents"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum TypesOfObjectsEnum {

    @XmlEnumValue("Documents")
    DOCUMENTS("Documents"),
    @XmlEnumValue("Folders")
    FOLDERS("Folders"),
    @XmlEnumValue("FoldersAndDocumets")
    FOLDERS_AND_DOCUMETS("FoldersAndDocumets");
    private final String value;

    TypesOfObjectsEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TypesOfObjectsEnum fromValue(String v) {
        for (TypesOfObjectsEnum c: TypesOfObjectsEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
