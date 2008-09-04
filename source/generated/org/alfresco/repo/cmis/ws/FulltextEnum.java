
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for fulltextEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="fulltextEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="noFulltext"/>
 *     &lt;enumeration value="fulltextOnly"/>
 *     &lt;enumeration value="fulltextAndStructured"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum FulltextEnum {

    @XmlEnumValue("fulltextAndStructured")
    FULLTEXT_AND_STRUCTURED("fulltextAndStructured"),
    @XmlEnumValue("fulltextOnly")
    FULLTEXT_ONLY("fulltextOnly"),
    @XmlEnumValue("noFulltext")
    NO_FULLTEXT("noFulltext");
    private final String value;

    FulltextEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FulltextEnum fromValue(String v) {
        for (FulltextEnum c: FulltextEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
