
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for relationshipDirectionEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="relationshipDirectionEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="source"/>
 *     &lt;enumeration value="target"/>
 *     &lt;enumeration value="sourceAndTarget"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum RelationshipDirectionEnum {

    @XmlEnumValue("source")
    SOURCE("source"),
    @XmlEnumValue("sourceAndTarget")
    SOURCE_AND_TARGET("sourceAndTarget"),
    @XmlEnumValue("target")
    TARGET("target");
    private final String value;

    RelationshipDirectionEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RelationshipDirectionEnum fromValue(String v) {
        for (RelationshipDirectionEnum c: RelationshipDirectionEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
