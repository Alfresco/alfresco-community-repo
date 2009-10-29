
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
 *     &lt;enumeration value="cmis:IsImmutable"/>
 *     &lt;enumeration value="cmis:IsLatestVersion"/>
 *     &lt;enumeration value="cmis:IsMajorVersion"/>
 *     &lt;enumeration value="cmis:IsLatestMajorVersion"/>
 *     &lt;enumeration value="cmis:VersionLabel"/>
 *     &lt;enumeration value="cmis:VersionSeriesId"/>
 *     &lt;enumeration value="cmis:IsVersionSeriesCheckedOut"/>
 *     &lt;enumeration value="cmis:VersionSeriesCheckedOutBy"/>
 *     &lt;enumeration value="cmis:VersionSeriesCheckedOutId"/>
 *     &lt;enumeration value="cmis:CheckinComment"/>
 *     &lt;enumeration value="cmis:ContentStreamLength"/>
 *     &lt;enumeration value="cmis:ContentStreamMimeType"/>
 *     &lt;enumeration value="cmis:ContentStreamFileName"/>
 *     &lt;enumeration value="cmis:ContentStreamId"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumPropertiesDocument", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumPropertiesDocument {

    @XmlEnumValue("cmis:IsImmutable")
    CMIS_IS_IMMUTABLE("cmis:IsImmutable"),
    @XmlEnumValue("cmis:IsLatestVersion")
    CMIS_IS_LATEST_VERSION("cmis:IsLatestVersion"),
    @XmlEnumValue("cmis:IsMajorVersion")
    CMIS_IS_MAJOR_VERSION("cmis:IsMajorVersion"),
    @XmlEnumValue("cmis:IsLatestMajorVersion")
    CMIS_IS_LATEST_MAJOR_VERSION("cmis:IsLatestMajorVersion"),
    @XmlEnumValue("cmis:VersionLabel")
    CMIS_VERSION_LABEL("cmis:VersionLabel"),
    @XmlEnumValue("cmis:VersionSeriesId")
    CMIS_VERSION_SERIES_ID("cmis:VersionSeriesId"),
    @XmlEnumValue("cmis:IsVersionSeriesCheckedOut")
    CMIS_IS_VERSION_SERIES_CHECKED_OUT("cmis:IsVersionSeriesCheckedOut"),
    @XmlEnumValue("cmis:VersionSeriesCheckedOutBy")
    CMIS_VERSION_SERIES_CHECKED_OUT_BY("cmis:VersionSeriesCheckedOutBy"),
    @XmlEnumValue("cmis:VersionSeriesCheckedOutId")
    CMIS_VERSION_SERIES_CHECKED_OUT_ID("cmis:VersionSeriesCheckedOutId"),
    @XmlEnumValue("cmis:CheckinComment")
    CMIS_CHECKIN_COMMENT("cmis:CheckinComment"),
    @XmlEnumValue("cmis:ContentStreamLength")
    CMIS_CONTENT_STREAM_LENGTH("cmis:ContentStreamLength"),
    @XmlEnumValue("cmis:ContentStreamMimeType")
    CMIS_CONTENT_STREAM_MIME_TYPE("cmis:ContentStreamMimeType"),
    @XmlEnumValue("cmis:ContentStreamFileName")
    CMIS_CONTENT_STREAM_FILE_NAME("cmis:ContentStreamFileName"),
    @XmlEnumValue("cmis:ContentStreamId")
    CMIS_CONTENT_STREAM_ID("cmis:ContentStreamId");
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
