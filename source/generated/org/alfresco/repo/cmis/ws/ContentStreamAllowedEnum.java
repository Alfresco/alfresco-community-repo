
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for contentStreamAllowedEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="contentStreamAllowedEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="notAllowed"/>
 *     &lt;enumeration value="allowed"/>
 *     &lt;enumeration value="required"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum ContentStreamAllowedEnum {

    @XmlEnumValue("allowed")
    ALLOWED("allowed"),
    @XmlEnumValue("notAllowed")
    NOT_ALLOWED("notAllowed"),
    @XmlEnumValue("required")
    REQUIRED("required");
    private final String value;

    ContentStreamAllowedEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ContentStreamAllowedEnum fromValue(String v) {
        for (ContentStreamAllowedEnum c: ContentStreamAllowedEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
