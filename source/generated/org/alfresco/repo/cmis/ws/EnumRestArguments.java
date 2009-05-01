
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for enumRestArguments.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="enumRestArguments">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="childTypes"/>
 *     &lt;enumeration value="continueOnFailure"/>
 *     &lt;enumeration value="checkin"/>
 *     &lt;enumeration value="checkinComment"/>
 *     &lt;enumeration value="depth"/>
 *     &lt;enumeration value="direction"/>
 *     &lt;enumeration value="filter"/>
 *     &lt;enumeration value="folderByPath"/>
 *     &lt;enumeration value="folderId"/>
 *     &lt;enumeration value="includeAllowableActions"/>
 *     &lt;enumeration value="includePropertyDefinitions"/>
 *     &lt;enumeration value="includeRelationships"/>
 *     &lt;enumeration value="includeSubrelationshipTypes"/>
 *     &lt;enumeration value="length"/>
 *     &lt;enumeration value="major"/>
 *     &lt;enumeration value="majorVersion"/>
 *     &lt;enumeration value="maxItems"/>
 *     &lt;enumeration value="offset"/>
 *     &lt;enumeration value="removeFrom"/>
 *     &lt;enumeration value="relationshipType"/>
 *     &lt;enumeration value="repositoryId"/>
 *     &lt;enumeration value="returnToRoot"/>
 *     &lt;enumeration value="returnVersion"/>
 *     &lt;enumeration value="skipCount"/>
 *     &lt;enumeration value="thisVersion"/>
 *     &lt;enumeration value="typeId"/>
 *     &lt;enumeration value="types"/>
 *     &lt;enumeration value="unfileMultiFiledDocuments"/>
 *     &lt;enumeration value="versioningState"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "enumRestArguments", namespace = "http://docs.oasis-open.org/ns/cmis/core/200901")
@XmlEnum
public enum EnumRestArguments {

    @XmlEnumValue("childTypes")
    CHILD_TYPES("childTypes"),
    @XmlEnumValue("continueOnFailure")
    CONTINUE_ON_FAILURE("continueOnFailure"),
    @XmlEnumValue("checkin")
    CHECKIN("checkin"),
    @XmlEnumValue("checkinComment")
    CHECKIN_COMMENT("checkinComment"),
    @XmlEnumValue("depth")
    DEPTH("depth"),
    @XmlEnumValue("direction")
    DIRECTION("direction"),
    @XmlEnumValue("filter")
    FILTER("filter"),
    @XmlEnumValue("folderByPath")
    FOLDER_BY_PATH("folderByPath"),
    @XmlEnumValue("folderId")
    FOLDER_ID("folderId"),
    @XmlEnumValue("includeAllowableActions")
    INCLUDE_ALLOWABLE_ACTIONS("includeAllowableActions"),
    @XmlEnumValue("includePropertyDefinitions")
    INCLUDE_PROPERTY_DEFINITIONS("includePropertyDefinitions"),
    @XmlEnumValue("includeRelationships")
    INCLUDE_RELATIONSHIPS("includeRelationships"),
    @XmlEnumValue("includeSubrelationshipTypes")
    INCLUDE_SUBRELATIONSHIP_TYPES("includeSubrelationshipTypes"),
    @XmlEnumValue("length")
    LENGTH("length"),
    @XmlEnumValue("major")
    MAJOR("major"),
    @XmlEnumValue("majorVersion")
    MAJOR_VERSION("majorVersion"),
    @XmlEnumValue("maxItems")
    MAX_ITEMS("maxItems"),
    @XmlEnumValue("offset")
    OFFSET("offset"),
    @XmlEnumValue("removeFrom")
    REMOVE_FROM("removeFrom"),
    @XmlEnumValue("relationshipType")
    RELATIONSHIP_TYPE("relationshipType"),
    @XmlEnumValue("repositoryId")
    REPOSITORY_ID("repositoryId"),
    @XmlEnumValue("returnToRoot")
    RETURN_TO_ROOT("returnToRoot"),
    @XmlEnumValue("returnVersion")
    RETURN_VERSION("returnVersion"),
    @XmlEnumValue("skipCount")
    SKIP_COUNT("skipCount"),
    @XmlEnumValue("thisVersion")
    THIS_VERSION("thisVersion"),
    @XmlEnumValue("typeId")
    TYPE_ID("typeId"),
    @XmlEnumValue("types")
    TYPES("types"),
    @XmlEnumValue("unfileMultiFiledDocuments")
    UNFILE_MULTI_FILED_DOCUMENTS("unfileMultiFiledDocuments"),
    @XmlEnumValue("versioningState")
    VERSIONING_STATE("versioningState");
    private final String value;

    EnumRestArguments(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumRestArguments fromValue(String v) {
        for (EnumRestArguments c: EnumRestArguments.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
