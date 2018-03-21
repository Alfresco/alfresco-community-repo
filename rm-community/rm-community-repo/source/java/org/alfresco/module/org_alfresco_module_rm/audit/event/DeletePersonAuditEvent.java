/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.audit.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Audits person deletion.
 *
 * @author Rodica Sutu
 * @since 2.7
 */
@BehaviourBean
public class DeletePersonAuditEvent extends AuditEvent implements BeforeDeleteNodePolicy
{

    private NodeService nodeService;

    private DictionaryService dictionaryService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */

    @Override
    @Behaviour
    (
        kind = BehaviourKind.CLASS,
        type = "cm:person"
    )
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        //get the cm:person properties
        Set<QName> properties = dictionaryService.getPropertyDefs(ContentModel.TYPE_PERSON).keySet();
        //retrive the properties and the values
        Map<QName, Serializable> result = new HashMap<>();
        for (QName property : properties)
        {
            Serializable values = nodeService.getProperty(nodeRef, property);
            if (values != null)
            {
                result.put(property, values);
            }
        }

        //audit the property values before the delete event
        recordsManagementAuditService.auditEvent(nodeRef, getName(), result, null, true, false);
    }


}
