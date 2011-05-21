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

package org.alfresco.repo.forms.processor.workflow;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.forms.processor.node.ContentModelItemData;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.workflow.WorkflowBuilder;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;

/**
 * Utility class that assists in persisting workflow related form data.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class WorkflowFormPersister extends ContentModelFormPersister<WorkflowInstance>
{
    private final WorkflowBuilder builder;
    
    public WorkflowFormPersister(ContentModelItemData<?> itemData,
                NamespaceService namespaceService,
                DictionaryService dictionaryService,
                WorkflowService workflowService,
                NodeService nodeService,
                BehaviourFilter behaviourFilter, Log logger)
    {
        super(itemData, namespaceService, dictionaryService, logger);
        WorkflowDefinition definition = (WorkflowDefinition) itemData.getItem();
        this.builder = new WorkflowBuilder(definition, workflowService, nodeService, behaviourFilter);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.ContentModelFormPersister#addAssociation(org.alfresco.service.namespace.QName, java.util.List)
     */
    @Override
    protected boolean addAssociation(QName qName, List<NodeRef> values)
    {
        builder.addAssociationParameter(qName, values);
        return true;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.ContentModelFormPersister#persist()
     */
    @Override
    public WorkflowInstance persist()
    {
        return builder.build();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.ContentModelFormPersister#removeAssociation(org.alfresco.service.namespace.QName, java.util.List)
     */
    @Override
    protected boolean removeAssociation(QName qName, List<NodeRef> values)
    {
        // Do nothing!
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.ContentModelFormPersister#updateProperty(org.alfresco.service.namespace.QName, java.io.Serializable)
     */
    @Override
    protected boolean updateProperty(QName qName, Serializable value)
    {
        builder.addParameter(qName, value);
        return true;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.ContentModelFormPersister#addTransientAssociation(java.lang.String, java.util.List)
     */
    @Override
    protected boolean addTransientAssociation(String fieldName, List<NodeRef> values)
    {
        if (PackageItemsFieldProcessor.KEY.equals(fieldName))
        {
            builder.addPackageItems(values);
            return true;
        }
        return false;
    }
}
