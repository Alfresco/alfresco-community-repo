
package org.alfresco.repo.audit.model._3;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ScopeAttribute.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ScopeAttribute">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SESSION"/>
 *     &lt;enumeration value="AUDIT"/>
 *     &lt;enumeration value="ALL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ScopeAttribute")
@XmlEnum
public enum ScopeAttribute {

    SESSION,
    AUDIT,
    ALL;

    public String value() {
        return name();
    }

    public static ScopeAttribute fromValue(String v) {
        return valueOf(v);
    }

}
