
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumPropertiesPolicy.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumPropertiesPolicy">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Name"/>
 *     &lt;enumeration value="ObjectId"/>
 *     &lt;enumeration value="Uri"/>
 *     &lt;enumeration value="ObjectTypeId"/>
 *     &lt;enumeration value="BaseTypeId"/>
 *     &lt;enumeration value="CreatedBy"/>
 *     &lt;enumeration value="CreationDate"/>
 *     &lt;enumeration value="LastModifiedBy"/>
 *     &lt;enumeration value="LastModificationDate"/>
 *     &lt;enumeration value="ChangeToken"/>
 *     &lt;enumeration value="PolicyName"/>
 *     &lt;enumeration value="PolicyText"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumPropertiesPolicy", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumPropertiesPolicy {

    @XmlEnumValue("Name")
    NAME("Name"),
    @XmlEnumValue("ObjectId")
    OBJECT_ID("ObjectId"),
    @XmlEnumValue("Uri")
    URI("Uri"),
    @XmlEnumValue("ObjectTypeId")
    OBJECT_TYPE_ID("ObjectTypeId"),
    @XmlEnumValue("BaseTypeId")
    BASE_TYPE_ID("BaseTypeId"),
    @XmlEnumValue("CreatedBy")
    CREATED_BY("CreatedBy"),
    @XmlEnumValue("CreationDate")
    CREATION_DATE("CreationDate"),
    @XmlEnumValue("LastModifiedBy")
    LAST_MODIFIED_BY("LastModifiedBy"),
    @XmlEnumValue("LastModificationDate")
    LAST_MODIFICATION_DATE("LastModificationDate"),
    @XmlEnumValue("ChangeToken")
    CHANGE_TOKEN("ChangeToken"),
    @XmlEnumValue("PolicyName")
    POLICY_NAME("PolicyName"),
    @XmlEnumValue("PolicyText")
    POLICY_TEXT("PolicyText");
    private final String value;

    EnumPropertiesPolicy(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumPropertiesPolicy fromValue(String v) {
        for (EnumPropertiesPolicy c: EnumPropertiesPolicy.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
