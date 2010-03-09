
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
 *     &lt;enumeration value="cmis:isImmutable"/>
 *     &lt;enumeration value="cmis:isLatestVersion"/>
 *     &lt;enumeration value="cmis:isMajorVersion"/>
 *     &lt;enumeration value="cmis:isLatestMajorVersion"/>
 *     &lt;enumeration value="cmis:versionLabel"/>
 *     &lt;enumeration value="cmis:versionSeriesId"/>
 *     &lt;enumeration value="cmis:isVersionSeriesCheckedOut"/>
 *     &lt;enumeration value="cmis:versionSeriesCheckedOutBy"/>
 *     &lt;enumeration value="cmis:versionSeriesCheckedOutId"/>
 *     &lt;enumeration value="cmis:checkinComment"/>
 *     &lt;enumeration value="cmis:contentStreamLength"/>
 *     &lt;enumeration value="cmis:contentStreamMimeType"/>
 *     &lt;enumeration value="cmis:contentStreamFileName"/>
 *     &lt;enumeration value="cmis:contentStreamId"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumPropertiesDocument", namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/")
@XmlEnum
public enum EnumPropertiesDocument {

    @XmlEnumValue("cmis:isImmutable")
    CMIS_IS_IMMUTABLE("cmis:isImmutable"),
    @XmlEnumValue("cmis:isLatestVersion")
    CMIS_IS_LATEST_VERSION("cmis:isLatestVersion"),
    @XmlEnumValue("cmis:isMajorVersion")
    CMIS_IS_MAJOR_VERSION("cmis:isMajorVersion"),
    @XmlEnumValue("cmis:isLatestMajorVersion")
    CMIS_IS_LATEST_MAJOR_VERSION("cmis:isLatestMajorVersion"),
    @XmlEnumValue("cmis:versionLabel")
    CMIS_VERSION_LABEL("cmis:versionLabel"),
    @XmlEnumValue("cmis:versionSeriesId")
    CMIS_VERSION_SERIES_ID("cmis:versionSeriesId"),
    @XmlEnumValue("cmis:isVersionSeriesCheckedOut")
    CMIS_IS_VERSION_SERIES_CHECKED_OUT("cmis:isVersionSeriesCheckedOut"),
    @XmlEnumValue("cmis:versionSeriesCheckedOutBy")
    CMIS_VERSION_SERIES_CHECKED_OUT_BY("cmis:versionSeriesCheckedOutBy"),
    @XmlEnumValue("cmis:versionSeriesCheckedOutId")
    CMIS_VERSION_SERIES_CHECKED_OUT_ID("cmis:versionSeriesCheckedOutId"),
    @XmlEnumValue("cmis:checkinComment")
    CMIS_CHECKIN_COMMENT("cmis:checkinComment"),
    @XmlEnumValue("cmis:contentStreamLength")
    CMIS_CONTENT_STREAM_LENGTH("cmis:contentStreamLength"),
    @XmlEnumValue("cmis:contentStreamMimeType")
    CMIS_CONTENT_STREAM_MIME_TYPE("cmis:contentStreamMimeType"),
    @XmlEnumValue("cmis:contentStreamFileName")
    CMIS_CONTENT_STREAM_FILE_NAME("cmis:contentStreamFileName"),
    @XmlEnumValue("cmis:contentStreamId")
    CMIS_CONTENT_STREAM_ID("cmis:contentStreamId");
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
