
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for deleteWithMultiFilingEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="deleteWithMultiFilingEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="unfile"/>
 *     &lt;enumeration value="deleteSingleFiledDocs"/>
 *     &lt;enumeration value="delete"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum DeleteWithMultiFilingEnum {

    @XmlEnumValue("delete")
    DELETE("delete"),
    @XmlEnumValue("deleteSingleFiledDocs")
    DELETE_SINGLE_FILED_DOCS("deleteSingleFiledDocs"),
    @XmlEnumValue("unfile")
    UNFILE("unfile");
    private final String value;

    DeleteWithMultiFilingEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DeleteWithMultiFilingEnum fromValue(String v) {
        for (DeleteWithMultiFilingEnum c: DeleteWithMultiFilingEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
