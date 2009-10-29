
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
 *     &lt;enumeration value="cmis:Name"/>
 *     &lt;enumeration value="cmis:ObjectId"/>
 *     &lt;enumeration value="cmis:ObjectTypeId"/>
 *     &lt;enumeration value="cmis:BaseTypeId"/>
 *     &lt;enumeration value="cmis:CreatedBy"/>
 *     &lt;enumeration value="cmis:CreationDate"/>
 *     &lt;enumeration value="cmis:LastModifiedBy"/>
 *     &lt;enumeration value="cmis:LastModificationDate"/>
 *     &lt;enumeration value="cmis:ChangeToken"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumPropertiesBase", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumPropertiesBase {

    @XmlEnumValue("cmis:Name")
    CMIS_NAME("cmis:Name"),
    @XmlEnumValue("cmis:ObjectId")
    CMIS_OBJECT_ID("cmis:ObjectId"),
    @XmlEnumValue("cmis:ObjectTypeId")
    CMIS_OBJECT_TYPE_ID("cmis:ObjectTypeId"),
    @XmlEnumValue("cmis:BaseTypeId")
    CMIS_BASE_TYPE_ID("cmis:BaseTypeId"),
    @XmlEnumValue("cmis:CreatedBy")
    CMIS_CREATED_BY("cmis:CreatedBy"),
    @XmlEnumValue("cmis:CreationDate")
    CMIS_CREATION_DATE("cmis:CreationDate"),
    @XmlEnumValue("cmis:LastModifiedBy")
    CMIS_LAST_MODIFIED_BY("cmis:LastModifiedBy"),
    @XmlEnumValue("cmis:LastModificationDate")
    CMIS_LAST_MODIFICATION_DATE("cmis:LastModificationDate"),
    @XmlEnumValue("cmis:ChangeToken")
    CMIS_CHANGE_TOKEN("cmis:ChangeToken");
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
