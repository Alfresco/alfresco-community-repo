
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumRepositoryRelationship.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumRepositoryRelationship">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="self"/>
 *     &lt;enumeration value="replica"/>
 *     &lt;enumeration value="peer"/>
 *     &lt;enumeration value="parent"/>
 *     &lt;enumeration value="child"/>
 *     &lt;enumeration value="archive"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumRepositoryRelationship", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumRepositoryRelationship {

    @XmlEnumValue("self")
    SELF("self"),
    @XmlEnumValue("replica")
    REPLICA("replica"),
    @XmlEnumValue("peer")
    PEER("peer"),
    @XmlEnumValue("parent")
    PARENT("parent"),
    @XmlEnumValue("child")
    CHILD("child"),
    @XmlEnumValue("archive")
    ARCHIVE("archive");
    private final String value;

    EnumRepositoryRelationship(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumRepositoryRelationship fromValue(String v) {
        for (EnumRepositoryRelationship c: EnumRepositoryRelationship.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
