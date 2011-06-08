/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.cmis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.opencmis.EnumFactory;
import org.alfresco.opencmis.EnumLabel;

/**
 * CMIS Allowed Action Enum
 * 
 * @author davidc
 */
public enum CMISAllowedActionEnum implements EnumLabel
{
    
    // navigation services
    CAN_GET_DESCENDANTS("canGetDescendants", "canGetDescendents.Folder", CMISAccessControlService.CMIS_READ_PERMISSION, "canGetDescendents.Folder", "{http://www.alfresco.org/model/system/1.0}base.ReadChildren"),
    CAN_GET_FOLDER_TREE("canGetFolderTree"/*, "canGetFolderTree.Folder", CMISAccessControlService.CMIS_READ_PERMISSION, "canGetFolderTree.Folder", "{http://www.alfresco.org/model/system/1.0}base.ReadChildren"*/),
    CAN_GET_CHILDREN("canGetChildren", "canGetChildren.Folder", CMISAccessControlService.CMIS_READ_PERMISSION, "canGetChildren.Folder", "{http://www.alfresco.org/model/system/1.0}base.ReadChildren"), 
    CAN_GET_FOLDER_PARENT("canGetFolderParent", "canGetParents.Folder", CMISAccessControlService.CMIS_READ_PERMISSION, "canGetParents.Folder", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"), 
    CAN_GET_OBJECT_PARENTS("canGetObjectParents", "canGetFolderParent.Object", CMISAccessControlService.CMIS_READ_PERMISSION, "canGetFolderParent.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"), 
    
    // object services
    CAN_CREATE_DOCUMENT("canCreateDocument", "canCreateDocument.Folder", CMISAccessControlService.CMIS_ALL_PERMISSION, "canCreateDocument.Folder", "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"), 
    CAN_CREATE_FOLDER("canCreateFolder", "canCreateFolder.Folder", CMISAccessControlService.CMIS_ALL_PERMISSION, "canCreateFolder.Folder", "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"), 
    CAN_CREATE_RELATIONSHIP("canCreateRelationship"), 
    CAN_GET_PROPERTIES("canGetProperties", "canGetProperties.Object", CMISAccessControlService.CMIS_READ_PERMISSION, "canGetProperties.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"), 
    CAN_GET_RENDITIONS("canGetRenditions"/*, "canGetRenditions.Object", CMISAccessControlService.CMIS_READ_PERMISSION, "canGetRenditions.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"*/), 
    CAN_GET_CONTENT_STREAM("canGetContentStream", "canViewContent.Object", CMISAccessControlService.CMIS_READ_PERMISSION, "canViewContent.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadContent"), 
    CAN_UPDATE_PROPERTIES("canUpdateProperties", "canUpdateProperties.Object", CMISAccessControlService.CMIS_WRITE_PERMISSION, "canUpdateProperties.Object", "{http://www.alfresco.org/model/system/1.0}base.WriteProperties"), 
    CAN_MOVE_OBJECT("canMoveObject", "canMove.Object", CMISAccessControlService.CMIS_ALL_PERMISSION,  "canMove.Target", CMISAccessControlService.CMIS_ALL_PERMISSION, "canMove.Object", "{http://www.alfresco.org/model/system/1.0}base.DeleteNode",  "canMove.Target", "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"), 
    CAN_DELETE_OBJECT("canDeleteObject", "canDelete.Object", CMISAccessControlService.CMIS_ALL_PERMISSION, "canDelete.Object", "{http://www.alfresco.org/model/system/1.0}base.DeleteNode"),
    CAN_SET_CONTENT_STREAM("canSetContentStream", "canSetContent.Document", CMISAccessControlService.CMIS_WRITE_PERMISSION, "canSetContent.Document", "{http://www.alfresco.org/model/system/1.0}base.WriteContent"), 
    CAN_DELETE_CONTENT_STREAM("canDeleteContentStream", "canDeleteContent.Document", CMISAccessControlService.CMIS_WRITE_PERMISSION, "canDeleteContent.Document", "{http://www.alfresco.org/model/system/1.0}base.WriteContent"), 
    CAN_DELETE_TREE("canDeleteTree", "canDeleteTree.Folder", CMISAccessControlService.CMIS_ALL_PERMISSION, "canDeleteTree.Folder", "{http://www.alfresco.org/model/system/1.0}base.DeleteNode"),
    
    // multi-filing services
    CAN_ADD_OBJECT_TO_FOLDER("canAddObjectToFolder", "canAddToFolder.Object", CMISAccessControlService.CMIS_READ_PERMISSION, "canAddToFolder.Folder", CMISAccessControlService.CMIS_ALL_PERMISSION, "canAddToFolder.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties", "canAddToFolder.Folder", "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"), 
    CAN_REMOVE_OBJECT_FROM_FOLDER("canRemoveObjectFromFolder", "canRemoveFromFolder.Object", CMISAccessControlService.CMIS_ALL_PERMISSION, "canRemoveFromFolder.Object", "{http://www.alfresco.org/model/system/1.0}base.DeleteNode"), 
    
    // versioning services
    CAN_CHECKOUT("canCheckOut", "canCheckout.Document", CMISAccessControlService.CMIS_ALL_PERMISSION, "canCheckout.Document", "{http://www.alfresco.org/model/content/1.0}lockable.CheckOut"), 
    CAN_CANCEL_CHECKOUT("canCancelCheckOut", "canCancelCheckout.Document", CMISAccessControlService.CMIS_ALL_PERMISSION, "canCancelCheckout.Document", "{http://www.alfresco.org/model/content/1.0}lockable.CancelCheckOut"), 
    CAN_CHECKIN("canCheckIn", "canCheckin.Document", CMISAccessControlService.CMIS_ALL_PERMISSION, "canCheckin.Document", "{http://www.alfresco.org/model/content/1.0}lockable.CheckIn"), 
    CAN_GET_ALL_VERSIONS("canGetAllVersions", "canGetAllVersions.VersionSeries", CMISAccessControlService.CMIS_READ_PERMISSION, "canGetAllVersions.VersionSeries", "{http://www.alfresco.org/model/system/1.0}base.Read"), 

    // relationship services
    CAN_GET_OBJECT_RELATIONSHIPS("canGetObjectRelationships"),
    
    // policy services
    CAN_APPLY_POLICY("canApplyPolicy", "canAddPolicy.Object", CMISAccessControlService.CMIS_WRITE_PERMISSION, "canAddPolicy.Policy", CMISAccessControlService.CMIS_READ_PERMISSION, "canAddPolicy.Object", "{http://www.alfresco.org/model/system/1.0}base.Write"), 
    CAN_REMOVE_POLICY("canRemovePolicy", "canRemovePolicy.Object", CMISAccessControlService.CMIS_WRITE_PERMISSION, "canRemovePolicy.Policy", CMISAccessControlService.CMIS_READ_PERMISSION, "canRemovePolicy.Object", "{http://www.alfresco.org/model/system/1.0}base.Write"), 
    CAN_GET_APPLIED_POLICIES("canGetAppliedPolicies", "canGetAppliedPolicies.Object", CMISAccessControlService.CMIS_READ_PERMISSION, "canGetAppliedPolicies.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"), 
    
    // acl services
    CAN_GET_ACL("canGetACL", "canGetACL.Object", CMISAccessControlService.CMIS_ALL_PERMISSION, "canGetACL.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadPermissions"),
    CAN_APPLY_ACL("canApplyACL", "canApplyACL.Object", CMISAccessControlService.CMIS_ALL_PERMISSION, "canApplyACL.Object", "{http://www.alfresco.org/model/system/1.0}base.ChangePermissions");
    

    private String label;
    
    private Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    
    /**
     * Construct
     * 
     * @param label
     */
    CMISAllowedActionEnum(String label, String ... keysAndPermissions)
    {
        this.label = label;
        assert(keysAndPermissions.length % 2 == 0);
        for(int i = 0; i < keysAndPermissions.length; i++)
        {
            String key = keysAndPermissions[i];
            String permission = keysAndPermissions[++i];
            List<String> permissions = mapping.get(key);
            if(permissions == null)
            {
                permissions = new ArrayList<String>(1);
                mapping.put(key, permissions);
            }
            permissions.add(permission);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.EnumLabel#label()
     */
    public String getLabel()
    {
        return label;
    }
    
    public Map<String, List<String>> getPermissionMapping()
    {
        return mapping;
    }

    public static EnumFactory<CMISAllowedActionEnum> FACTORY = new EnumFactory<CMISAllowedActionEnum>(CMISAllowedActionEnum.class); 
}
