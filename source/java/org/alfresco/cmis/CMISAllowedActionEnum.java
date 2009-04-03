/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis;

/**
 * CMIS Allowed Action Enum
 * 
 * @author davidc
 */
public enum CMISAllowedActionEnum implements EnumLabel
{
    CAN_DELETE("canDelete"), 
    CAN_UPDATE_PROPERTIES("canUpdateProperties"), 
    CAN_GET_PROPERTIES("canGetProperties"), 
    CAN_GET_RELATIONSHIPS("canGetRelationships"), 
    CAN_GET_PARENTS("canGetParents"), 
    CAN_GET_FOLDER_PARENT("canGetFolderParent"), 
    CAN_GET_DESCENDANTS("canGetDescendants"), 
    CAN_MOVE("canMove"), 
    CAN_DELETE_VERSION("canDeleteVersion"), 
    CAN_DELETE_CONTENT("canDeleteContent"), 
    CAN_CHECKOUT("canCheckout"), 
    CAN_CANCEL_CHECKOUT("canCancelCheckout"), 
    CAN_CHECKIN("canCheckin"), 
    CAN_SET_CONTENT("canSetContent"), 
    CAN_GET_ALL_VERSIONS("canGetAllVersions"), 
    CAN_ADD_TO_FOLDER("canAddToFolder"), 
    CAN_REMOVE_FROM_FOLDER("canRemoveFromFolder"), 
    CAN_VIEW_CONTENT("canViewContent"), 
    CAN_ADD_POLICY("canAddPolicy"), 
    CAN_GET_APPLIED_POLICIES("canGetAppliedPolicies"), 
    CAN_REMOVE_POLICY("canRemovePolicy"), 
    CAN_GET_CHILDREN("canGetChildren"), 
    CAN_CREATE_DOCUMENT("canCreateDocument"), 
    CAN_CREATE_FOLDER("canCreateFolder"), 
    CAN_CREATE_RELATIONSHIP("canCreateRelationship"), 
    CAN_CREATE_POLICY("canCreatePolicy"), 
    CAN_DELETE_TREE("canDeleteTree");


    private String label;
    
    /**
     * Construct
     * 
     * @param label
     */
    CMISAllowedActionEnum(String label)
    {
        this.label = label;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.EnumLabel#label()
     */
    public String getLabel()
    {
        return label;
    }

    public static EnumFactory<CMISAllowedActionEnum> FACTORY = new EnumFactory<CMISAllowedActionEnum>(CMISAllowedActionEnum.class); 
}
