
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for versionEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="versionEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="this"/>
 *     &lt;enumeration value="latest"/>
 *     &lt;enumeration value="latestMajor"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum VersionEnum {

    @XmlEnumValue("latest")
    LATEST("latest"),
    @XmlEnumValue("latestMajor")
    LATEST_MAJOR("latestMajor"),
    @XmlEnumValue("this")
    THIS("this");
    private final String value;

    VersionEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VersionEnum fromValue(String v) {
        for (VersionEnum c: VersionEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
