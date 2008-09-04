
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for updatabilityEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="updatabilityEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="readOnly"/>
 *     &lt;enumeration value="readWrite"/>
 *     &lt;enumeration value="readWriteWhenCheckedOut"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum UpdatabilityEnum {

    @XmlEnumValue("readOnly")
    READ_ONLY("readOnly"),
    @XmlEnumValue("readWrite")
    READ_WRITE("readWrite"),
    @XmlEnumValue("readWriteWhenCheckedOut")
    READ_WRITE_WHEN_CHECKED_OUT("readWriteWhenCheckedOut");
    private final String value;

    UpdatabilityEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UpdatabilityEnum fromValue(String v) {
        for (UpdatabilityEnum c: UpdatabilityEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
