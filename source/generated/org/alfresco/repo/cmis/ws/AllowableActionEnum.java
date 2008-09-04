
package org.alfresco.repo.cmis.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for allowableActionEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="allowableActionEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="canGetProperties"/>
 *     &lt;enumeration value="canUpdateProperties"/>
 *     &lt;enumeration value="canDeleteObject"/>
 *     &lt;enumeration value="canGetObjectParents"/>
 *     &lt;enumeration value="canGetFolderParent"/>
 *     &lt;enumeration value="canMoveObject"/>
 *     &lt;enumeration value="canAddObjectToFolder"/>
 *     &lt;enumeration value="canRemoveObjectFromFolder"/>
 *     &lt;enumeration value="canGetRelationships"/>
 *     &lt;enumeration value="canApplyPolicy"/>
 *     &lt;enumeration value="canRemovePolicy"/>
 *     &lt;enumeration value="canGetAppliedPolicies"/>
 *     &lt;enumeration value="canGetContentStream"/>
 *     &lt;enumeration value="canSetContentStream"/>
 *     &lt;enumeration value="canDeleteContentStream"/>
 *     &lt;enumeration value="canCheckOut"/>
 *     &lt;enumeration value="canCancelCheckout"/>
 *     &lt;enumeration value="canCheckIn"/>
 *     &lt;enumeration value="canGetChildren"/>
 *     &lt;enumeration value="canGetDescendants"/>
 *     &lt;enumeration value="canCreateDocument"/>
 *     &lt;enumeration value="canCreateFolder"/>
 *     &lt;enumeration value="canCreateRelationship"/>
 *     &lt;enumeration value="canCreatePolicy"/>
 *     &lt;enumeration value="canDeleteTree"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum AllowableActionEnum {

    @XmlEnumValue("canAddObjectToFolder")
    CAN_ADD_OBJECT_TO_FOLDER("canAddObjectToFolder"),
    @XmlEnumValue("canApplyPolicy")
    CAN_APPLY_POLICY("canApplyPolicy"),
    @XmlEnumValue("canCancelCheckout")
    CAN_CANCEL_CHECKOUT("canCancelCheckout"),
    @XmlEnumValue("canCheckIn")
    CAN_CHECK_IN("canCheckIn"),
    @XmlEnumValue("canCheckOut")
    CAN_CHECK_OUT("canCheckOut"),
    @XmlEnumValue("canCreateDocument")
    CAN_CREATE_DOCUMENT("canCreateDocument"),
    @XmlEnumValue("canCreateFolder")
    CAN_CREATE_FOLDER("canCreateFolder"),
    @XmlEnumValue("canCreatePolicy")
    CAN_CREATE_POLICY("canCreatePolicy"),
    @XmlEnumValue("canCreateRelationship")
    CAN_CREATE_RELATIONSHIP("canCreateRelationship"),
    @XmlEnumValue("canDeleteContentStream")
    CAN_DELETE_CONTENT_STREAM("canDeleteContentStream"),
    @XmlEnumValue("canDeleteObject")
    CAN_DELETE_OBJECT("canDeleteObject"),
    @XmlEnumValue("canDeleteTree")
    CAN_DELETE_TREE("canDeleteTree"),
    @XmlEnumValue("canGetAppliedPolicies")
    CAN_GET_APPLIED_POLICIES("canGetAppliedPolicies"),
    @XmlEnumValue("canGetChildren")
    CAN_GET_CHILDREN("canGetChildren"),
    @XmlEnumValue("canGetContentStream")
    CAN_GET_CONTENT_STREAM("canGetContentStream"),
    @XmlEnumValue("canGetDescendants")
    CAN_GET_DESCENDANTS("canGetDescendants"),
    @XmlEnumValue("canGetFolderParent")
    CAN_GET_FOLDER_PARENT("canGetFolderParent"),
    @XmlEnumValue("canGetObjectParents")
    CAN_GET_OBJECT_PARENTS("canGetObjectParents"),
    @XmlEnumValue("canGetProperties")
    CAN_GET_PROPERTIES("canGetProperties"),
    @XmlEnumValue("canGetRelationships")
    CAN_GET_RELATIONSHIPS("canGetRelationships"),
    @XmlEnumValue("canMoveObject")
    CAN_MOVE_OBJECT("canMoveObject"),
    @XmlEnumValue("canRemoveObjectFromFolder")
    CAN_REMOVE_OBJECT_FROM_FOLDER("canRemoveObjectFromFolder"),
    @XmlEnumValue("canRemovePolicy")
    CAN_REMOVE_POLICY("canRemovePolicy"),
    @XmlEnumValue("canSetContentStream")
    CAN_SET_CONTENT_STREAM("canSetContentStream"),
    @XmlEnumValue("canUpdateProperties")
    CAN_UPDATE_PROPERTIES("canUpdateProperties");
    private final String value;

    AllowableActionEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AllowableActionEnum fromValue(String v) {
        for (AllowableActionEnum c: AllowableActionEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
