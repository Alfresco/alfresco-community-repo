
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for versioningStateEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="versioningStateEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="checkedOut"/>
 *     &lt;enumeration value="checkedInMinor"/>
 *     &lt;enumeration value="checkedInMajor"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum VersioningStateEnum {

    @XmlEnumValue("checkedInMajor")
    CHECKED_IN_MAJOR("checkedInMajor"),
    @XmlEnumValue("checkedInMinor")
    CHECKED_IN_MINOR("checkedInMinor"),
    @XmlEnumValue("checkedOut")
    CHECKED_OUT("checkedOut");
    private final String value;

    VersioningStateEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VersioningStateEnum fromValue(String v) {
        for (VersioningStateEnum c: VersioningStateEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
