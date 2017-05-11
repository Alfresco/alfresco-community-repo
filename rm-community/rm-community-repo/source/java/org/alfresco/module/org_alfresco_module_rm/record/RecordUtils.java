/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.record;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_IDENTIFIER;
import static org.alfresco.util.ParameterCheck.mandatory;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Util class for records
 *
 * @author Tuna Aksoy
 * @since 2.4
 */
public class RecordUtils
{
    private RecordUtils()
    {
        // Will not be called
    }

    /**
     * Appends the record identifier to the name of the record
     *
     * @param nodeRef The node reference of the record.
     */
    public static void appendIdentifierToName(NodeService nodeService, NodeRef nodeRef)
    {
        mandatory("nodeService", nodeService);
        mandatory("nodeRef", nodeRef);
        
        if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT))
        {
        	return;
        }

        // get the record id
        String recordId = (String) nodeService.getProperty(nodeRef, PROP_IDENTIFIER);

        if (isNotBlank(recordId))
        {
            // get the record name
            String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

            // skip this step if the record name already contains the identifier
            if(name.contains(" (" + recordId + ")"))
            {
                return;
            }

            // rename the record
            int dotIndex = name.lastIndexOf('.');
            String prefix = name;
            String postfix = "";
            if (dotIndex > 0)
            {
                prefix = name.substring(0, dotIndex);
                postfix = name.substring(dotIndex);
            }
            String recordName = prefix + " (" + recordId + ")" + postfix;

            nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, recordName);
        }
    }
}
