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
package org.alfresco.module.org_alfresco_module_rm.record;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_RECORD;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_RECORD_COMPONENT_ID;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_IDENTIFIER;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_ORIGIONAL_NAME;
import static org.alfresco.module.org_alfresco_module_rm.record.RecordUtils.appendIdentifierToName;
import static org.alfresco.util.ParameterCheck.mandatory;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

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
     * Utility method that generates a record identifier and adds the new identifier to the record's name
     * 
     * @param record the record to generate the identifier for
     */
    public static void generateRecordIdentifier(NodeService nodeService, IdentifierService identifierService, NodeRef record)
    {
        if(nodeService.getProperty(record, PROP_IDENTIFIER) == null)
        {
            // get the record id
            String recordId = identifierService.generateIdentifier(ASPECT_RECORD,
                    nodeService.getPrimaryParent(record).getParentRef());

            // get the record name
            String name = (String)nodeService.getProperty(record, ContentModel.PROP_NAME);

            // add the properties to the record
            Map<QName, Serializable> props = new HashMap<>();
            props.put(PROP_IDENTIFIER, recordId);
            props.put(PROP_ORIGIONAL_NAME, name);
            nodeService.addProperties(record, props);
        }

        // append the identifier to the name even if it's been randomly generated or it was already set
        appendIdentifierToName(nodeService, record);
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
