
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumPropertiesDocument.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumPropertiesDocument">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Name"/>
 *     &lt;enumeration value="ObjectId"/>
 *     &lt;enumeration value="BaseTypeId"/>
 *     &lt;enumeration value="Uri"/>
 *     &lt;enumeration value="ObjectTypeId"/>
 *     &lt;enumeration value="CreatedBy"/>
 *     &lt;enumeration value="CreationDate"/>
 *     &lt;enumeration value="LastModifiedBy"/>
 *     &lt;enumeration value="LastModificationDate"/>
 *     &lt;enumeration value="ChangeToken"/>
 *     &lt;enumeration value="IsImmutable"/>
 *     &lt;enumeration value="IsLatestVersion"/>
 *     &lt;enumeration value="IsMajorVersion"/>
 *     &lt;enumeration value="IsLatestMajorVersion"/>
 *     &lt;enumeration value="VersionLabel"/>
 *     &lt;enumeration value="VersionSeriesId"/>
 *     &lt;enumeration value="IsVersionSeriesCheckedOut"/>
 *     &lt;enumeration value="VersionSeriesCheckedOutBy"/>
 *     &lt;enumeration value="VersionSeriesCheckedOutId"/>
 *     &lt;enumeration value="CheckinComment"/>
 *     &lt;enumeration value="ContentStreamAllowed"/>
 *     &lt;enumeration value="ContentStreamLength"/>
 *     &lt;enumeration value="ContentStreamMimeType"/>
 *     &lt;enumeration value="ContentStreamFilename"/>
 *     &lt;enumeration value="ContentStreamUri"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumPropertiesDocument", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumPropertiesDocument {

    @XmlEnumValue("Name")
    NAME("Name"),
    @XmlEnumValue("ObjectId")
    OBJECT_ID("ObjectId"),
    @XmlEnumValue("BaseTypeId")
    BASE_TYPE_ID("BaseTypeId"),
    @XmlEnumValue("Uri")
    URI("Uri"),
    @XmlEnumValue("ObjectTypeId")
    OBJECT_TYPE_ID("ObjectTypeId"),
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
    @XmlEnumValue("IsImmutable")
    IS_IMMUTABLE("IsImmutable"),
    @XmlEnumValue("IsLatestVersion")
    IS_LATEST_VERSION("IsLatestVersion"),
    @XmlEnumValue("IsMajorVersion")
    IS_MAJOR_VERSION("IsMajorVersion"),
    @XmlEnumValue("IsLatestMajorVersion")
    IS_LATEST_MAJOR_VERSION("IsLatestMajorVersion"),
    @XmlEnumValue("VersionLabel")
    VERSION_LABEL("VersionLabel"),
    @XmlEnumValue("VersionSeriesId")
    VERSION_SERIES_ID("VersionSeriesId"),
    @XmlEnumValue("IsVersionSeriesCheckedOut")
    IS_VERSION_SERIES_CHECKED_OUT("IsVersionSeriesCheckedOut"),
    @XmlEnumValue("VersionSeriesCheckedOutBy")
    VERSION_SERIES_CHECKED_OUT_BY("VersionSeriesCheckedOutBy"),
    @XmlEnumValue("VersionSeriesCheckedOutId")
    VERSION_SERIES_CHECKED_OUT_ID("VersionSeriesCheckedOutId"),
    @XmlEnumValue("CheckinComment")
    CHECKIN_COMMENT("CheckinComment"),
    @XmlEnumValue("ContentStreamAllowed")
    CONTENT_STREAM_ALLOWED("ContentStreamAllowed"),
    @XmlEnumValue("ContentStreamLength")
    CONTENT_STREAM_LENGTH("ContentStreamLength"),
    @XmlEnumValue("ContentStreamMimeType")
    CONTENT_STREAM_MIME_TYPE("ContentStreamMimeType"),
    @XmlEnumValue("ContentStreamFilename")
    CONTENT_STREAM_FILENAME("ContentStreamFilename"),
    @XmlEnumValue("ContentStreamUri")
    CONTENT_STREAM_URI("ContentStreamUri");
    private final String value;

    EnumPropertiesDocument(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumPropertiesDocument fromValue(String v) {
        for (EnumPropertiesDocument c: EnumPropertiesDocument.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
