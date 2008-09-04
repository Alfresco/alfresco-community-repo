
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for cardinalityEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="cardinalityEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="singleValued"/>
 *     &lt;enumeration value="multiValued"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum CardinalityEnum {

    @XmlEnumValue("multiValued")
    MULTI_VALUED("multiValued"),
    @XmlEnumValue("singleValued")
    SINGLE_VALUED("singleValued");
    private final String value;

    CardinalityEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CardinalityEnum fromValue(String v) {
        for (CardinalityEnum c: CardinalityEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
