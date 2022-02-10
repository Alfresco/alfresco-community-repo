/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_IDENTIFIER;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.TYPE_RECORD_CATEGORY;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.TYPE_RECORD_FOLDER;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;

/**
 * Utility class for record categories
 * @author Ross Gale
 * @since 2.7
 */
public class RecordCategoryUtil
{
    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * Setter for the node service
     * @param nodeService Node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Return the record category id for a file plan element
     * @param nodeRef the node reference of the file plan element
     * @param includeFolders return an id for records, folders and categories
     * @return Record category identifier
     */
    public String getCategoryIdFromNodeId(NodeRef nodeRef, boolean includeFolders)
    {
        if(!includeFolders)
        {
            if (nodeService.getType(nodeRef).equals(TYPE_RECORD_FOLDER) || nodeService.getType(nodeRef).equals(TYPE_RECORD_CATEGORY))
            {
                return null;
            }
        }
        //Search for the first category from the end of the path to save time
        Path path = nodeService.getPath(nodeRef);
        for(int x = path.size()-1; x >= 0; x--)
        {
            NodeRef ref = ((ChildAssocElement) path.get(x)).getRef().getChildRef();
            if (nodeService.getType(ref).equals(TYPE_RECORD_CATEGORY))
            {
                return nodeService.getProperty(ref, PROP_IDENTIFIER).toString();
            }
        }
        return null;
    }
}
