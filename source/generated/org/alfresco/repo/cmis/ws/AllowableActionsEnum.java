
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for allowableActionsEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="allowableActionsEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="delete"/>
 *     &lt;enumeration value="updateProperties"/>
 *     &lt;enumeration value="checkOut"/>
 *     &lt;enumeration value="cancelCheckOut"/>
 *     &lt;enumeration value="checkIn"/>
 *     &lt;enumeration value="deleteVersion"/>
 *     &lt;enumeration value="addToFolder"/>
 *     &lt;enumeration value="removeFromFolder"/>
 *     &lt;enumeration value="setContent"/>
 *     &lt;enumeration value="deleteContent"/>
 *     &lt;enumeration value="getAllVersions"/>
 *     &lt;enumeration value="getChilderen"/>
 *     &lt;enumeration value="getParents"/>
 *     &lt;enumeration value="getRelationships"/>
 *     &lt;enumeration value="getProperties"/>
 *     &lt;enumeration value="viewContent"/>
 *     &lt;enumeration value="move"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum AllowableActionsEnum {

    @XmlEnumValue("addToFolder")
    ADD_TO_FOLDER("addToFolder"),
    @XmlEnumValue("cancelCheckOut")
    CANCEL_CHECK_OUT("cancelCheckOut"),
    @XmlEnumValue("checkIn")
    CHECK_IN("checkIn"),
    @XmlEnumValue("checkOut")
    CHECK_OUT("checkOut"),
    @XmlEnumValue("delete")
    DELETE("delete"),
    @XmlEnumValue("deleteContent")
    DELETE_CONTENT("deleteContent"),
    @XmlEnumValue("deleteVersion")
    DELETE_VERSION("deleteVersion"),
    @XmlEnumValue("getAllVersions")
    GET_ALL_VERSIONS("getAllVersions"),
    @XmlEnumValue("getChilderen")
    GET_CHILDEREN("getChilderen"),
    @XmlEnumValue("getParents")
    GET_PARENTS("getParents"),
    @XmlEnumValue("getProperties")
    GET_PROPERTIES("getProperties"),
    @XmlEnumValue("getRelationships")
    GET_RELATIONSHIPS("getRelationships"),
    @XmlEnumValue("move")
    MOVE("move"),
    @XmlEnumValue("removeFromFolder")
    REMOVE_FROM_FOLDER("removeFromFolder"),
    @XmlEnumValue("setContent")
    SET_CONTENT("setContent"),
    @XmlEnumValue("updateProperties")
    UPDATE_PROPERTIES("updateProperties"),
    @XmlEnumValue("viewContent")
    VIEW_CONTENT("viewContent");
    private final String value;

    AllowableActionsEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AllowableActionsEnum fromValue(String v) {
        for (AllowableActionsEnum c: AllowableActionsEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
