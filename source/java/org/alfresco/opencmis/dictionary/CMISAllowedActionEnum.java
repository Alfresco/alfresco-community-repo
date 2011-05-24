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
package org.alfresco.opencmis.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.BasicPermissions;

/**
 * CMIS Allowed Action Enum
 * 
 * @author davidc
 */
public enum CMISAllowedActionEnum
{
    
    // navigation services
    CAN_GET_DESCENDANTS("canGetDescendants", "canGetDescendents.Folder", BasicPermissions.READ, "canGetDescendents.Folder", "{http://www.alfresco.org/model/system/1.0}base.ReadChildren"),
    //CAN_GET_FOLDER_TREE("canGetFolderTree", "canGetFolderTree.Folder", BasicPermissions.READ, "canGetFolderTree.Folder", "{http://www.alfresco.org/model/system/1.0}base.ReadChildren"),
    CAN_GET_CHILDREN("canGetChildren", "canGetChildren.Folder", BasicPermissions.READ, "canGetChildren.Folder", "{http://www.alfresco.org/model/system/1.0}base.ReadChildren"), 
    CAN_GET_FOLDER_PARENT("canGetFolderParent", "canGetParents.Folder", BasicPermissions.READ, "canGetParents.Folder", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"), 
    CAN_GET_OBJECT_PARENTS("canGetObjectParents", "canGetFolderParent.Object", BasicPermissions.READ, "canGetFolderParent.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"), 
    
    // object services
    CAN_CREATE_DOCUMENT("canCreateDocument", "canCreateDocument.Folder", BasicPermissions.ALL, "canCreateDocument.Folder", "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"), 
    CAN_CREATE_FOLDER("canCreateFolder", "canCreateFolder.Folder", BasicPermissions.ALL, "canCreateFolder.Folder", "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"), 
    CAN_CREATE_RELATIONSHIP("canCreateRelationship"), 
    CAN_GET_PROPERTIES("canGetProperties", "canGetProperties.Object", BasicPermissions.READ, "canGetProperties.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"), 
    CAN_GET_RENDITIONS("canGetRenditions"/*, "canGetRenditions.Object", BasicPermissions.READ, "canGetRenditions.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"*/), 
    CAN_GET_CONTENT_STREAM("canGetContentStream", "canViewContent.Object", BasicPermissions.READ, "canViewContent.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadContent"), 
    CAN_UPDATE_PROPERTIES("canUpdateProperties", "canUpdateProperties.Object", BasicPermissions.WRITE, "canUpdateProperties.Object", "{http://www.alfresco.org/model/system/1.0}base.WriteProperties"), 
    CAN_MOVE_OBJECT("canMoveObject", "canMove.Object", BasicPermissions.ALL,  "canMove.Target", BasicPermissions.ALL, "canMove.Object", "{http://www.alfresco.org/model/system/1.0}base.DeleteNode",  "canMove.Target", "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"), 
    CAN_DELETE_OBJECT("canDeleteObject", "canDelete.Object", BasicPermissions.ALL, "canDelete.Object", "{http://www.alfresco.org/model/system/1.0}base.DeleteNode"),
    CAN_SET_CONTENT_STREAM("canSetContentStream", "canSetContent.Document", BasicPermissions.WRITE, "canSetContent.Document", "{http://www.alfresco.org/model/system/1.0}base.WriteContent"), 
    CAN_DELETE_CONTENT_STREAM("canDeleteContentStream", "canDeleteContent.Document", BasicPermissions.WRITE, "canDeleteContent.Document", "{http://www.alfresco.org/model/system/1.0}base.WriteContent"), 
    CAN_DELETE_TREE("canDeleteTree", "canDeleteTree.Folder", BasicPermissions.ALL, "canDeleteTree.Folder", "{http://www.alfresco.org/model/system/1.0}base.DeleteNode"),
    
    // multi-filing services
    CAN_ADD_OBJECT_TO_FOLDER("canAddObjectToFolder", "canAddToFolder.Object", BasicPermissions.READ, "canAddToFolder.Folder", BasicPermissions.ALL, "canAddToFolder.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties", "canAddToFolder.Folder", "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"), 
    CAN_REMOVE_OBJECT_FROM_FOLDER("canRemoveObjectFromFolder", "canRemoveFromFolder.Object", BasicPermissions.ALL, "canRemoveFromFolder.Object", "{http://www.alfresco.org/model/system/1.0}base.DeleteNode"), 
    
    // versioning services
    CAN_CHECKOUT("canCheckOut", "canCheckout.Document", BasicPermissions.ALL, "canCheckout.Document", "{http://www.alfresco.org/model/content/1.0}lockable.CheckOut"), 
    CAN_CANCEL_CHECKOUT("canCancelCheckOut", "canCancelCheckout.Document", BasicPermissions.ALL, "canCancelCheckout.Document", "{http://www.alfresco.org/model/content/1.0}lockable.CancelCheckOut"), 
    CAN_CHECKIN("canCheckIn", "canCheckin.Document", BasicPermissions.ALL, "canCheckin.Document", "{http://www.alfresco.org/model/content/1.0}lockable.CheckIn"), 
    CAN_GET_ALL_VERSIONS("canGetAllVersions", "canGetAllVersions.VersionSeries", BasicPermissions.READ, "canGetAllVersions.VersionSeries", "{http://www.alfresco.org/model/system/1.0}base.Read"), 

    // relationship services
    CAN_GET_OBJECT_RELATIONSHIPS("canGetObjectRelationships"),
    
    // policy services
    CAN_APPLY_POLICY("canApplyPolicy", "canAddPolicy.Object", BasicPermissions.WRITE, "canAddPolicy.Policy", BasicPermissions.READ, "canAddPolicy.Object", "{http://www.alfresco.org/model/system/1.0}base.Write"), 
    CAN_REMOVE_POLICY("canRemovePolicy", "canRemovePolicy.Object", BasicPermissions.WRITE, "canRemovePolicy.Policy", BasicPermissions.READ, "canRemovePolicy.Object", "{http://www.alfresco.org/model/system/1.0}base.Write"), 
    CAN_GET_APPLIED_POLICIES("canGetAppliedPolicies", "canGetAppliedPolicies.Object", BasicPermissions.READ, "canGetAppliedPolicies.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"), 
    
    // acl services
    CAN_GET_ACL("canGetACL", "canGetACL.Object", BasicPermissions.ALL, "canGetACL.Object", "{http://www.alfresco.org/model/system/1.0}base.ReadPermissions"),
    CAN_APPLY_ACL("canApplyACL", "canApplyACL.Object", BasicPermissions.ALL, "canApplyACL.Object", "{http://www.alfresco.org/model/system/1.0}base.ChangePermissions");
    

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


    public String getLabel()
    {
        return label;
    }
    
    public Map<String, List<String>> getPermissionMapping()
    {
        return mapping;
    }
}
