
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumPropertiesBase.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumPropertiesBase">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="cmis:name"/>
 *     &lt;enumeration value="cmis:objectId"/>
 *     &lt;enumeration value="cmis:objectTypeId"/>
 *     &lt;enumeration value="cmis:baseTypeId"/>
 *     &lt;enumeration value="cmis:createdBy"/>
 *     &lt;enumeration value="cmis:creationDate"/>
 *     &lt;enumeration value="cmis:lastModifiedBy"/>
 *     &lt;enumeration value="cmis:lastModificationDate"/>
 *     &lt;enumeration value="cmis:changeToken"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumPropertiesBase", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumPropertiesBase {

    @XmlEnumValue("cmis:name")
    CMIS_NAME("cmis:name"),
    @XmlEnumValue("cmis:objectId")
    CMIS_OBJECT_ID("cmis:objectId"),
    @XmlEnumValue("cmis:objectTypeId")
    CMIS_OBJECT_TYPE_ID("cmis:objectTypeId"),
    @XmlEnumValue("cmis:baseTypeId")
    CMIS_BASE_TYPE_ID("cmis:baseTypeId"),
    @XmlEnumValue("cmis:createdBy")
    CMIS_CREATED_BY("cmis:createdBy"),
    @XmlEnumValue("cmis:creationDate")
    CMIS_CREATION_DATE("cmis:creationDate"),
    @XmlEnumValue("cmis:lastModifiedBy")
    CMIS_LAST_MODIFIED_BY("cmis:lastModifiedBy"),
    @XmlEnumValue("cmis:lastModificationDate")
    CMIS_LAST_MODIFICATION_DATE("cmis:lastModificationDate"),
    @XmlEnumValue("cmis:changeToken")
    CMIS_CHANGE_TOKEN("cmis:changeToken");
    private final String value;

    EnumPropertiesBase(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumPropertiesBase fromValue(String v) {
        for (EnumPropertiesBase c: EnumPropertiesBase.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
