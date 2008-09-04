
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for propertyTypeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="propertyTypeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="String"/>
 *     &lt;enumeration value="Decimal"/>
 *     &lt;enumeration value="Integer"/>
 *     &lt;enumeration value="Boolean"/>
 *     &lt;enumeration value="DateTime"/>
 *     &lt;enumeration value="URI"/>
 *     &lt;enumeration value="ID"/>
 *     &lt;enumeration value="XML"/>
 *     &lt;enumeration value="HTML"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum PropertyTypeEnum {

    @XmlEnumValue("Boolean")
    BOOLEAN("Boolean"),
    @XmlEnumValue("DateTime")
    DATE_TIME("DateTime"),
    @XmlEnumValue("Decimal")
    DECIMAL("Decimal"),
    HTML("HTML"),
    ID("ID"),
    @XmlEnumValue("Integer")
    INTEGER("Integer"),
    @XmlEnumValue("String")
    STRING("String"),
    URI("URI"),
    XML("XML");
    private final String value;

    PropertyTypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PropertyTypeEnum fromValue(String v) {
        for (PropertyTypeEnum c: PropertyTypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
