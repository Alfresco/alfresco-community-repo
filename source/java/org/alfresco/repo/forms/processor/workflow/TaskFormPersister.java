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

import org.alfresco.repo.forms.processor.node.ItemData;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;

/**
 * @author Nick Smith
 *
 */
public class TaskFormPersister extends ContentModelFormPersister<WorkflowTask>
{
    private final TaskUpdater updater;
    
    public TaskFormPersister(ItemData<WorkflowTask> itemData,
                NamespaceService namespaceService,
                DictionaryService dictionaryService,
                WorkflowService workflowService,
                Log logger)
    {
        super(itemData, namespaceService, dictionaryService, logger);
        WorkflowTask item = itemData.getItem();
        this.updater = new TaskUpdater(item.id, workflowService);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.ContentModelFormPersister#addAssociation(org.alfresco.service.namespace.QName, java.util.List)
     */
    @Override
    protected boolean addAssociation(QName qName, List<NodeRef> values)
    {
        updater.addAssociation(qName, values);
        return true;
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.ContentModelFormPersister#removeAssociation(org.alfresco.service.namespace.QName, java.util.List)
     */
    @Override
    protected boolean removeAssociation(QName qName, List<NodeRef> values)
    {
        updater.removeAssociation(qName, values);
        return true;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.ContentModelFormPersister#updateProperty(org.alfresco.service.namespace.QName, java.io.Serializable)
     */
    @Override
    protected boolean updateProperty(QName qName, Serializable value)
    {
        updater.addProperty(qName, value);
        return true;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.workflow.ContentModelFormPersister#persist()
     */
    @Override
    public WorkflowTask persist()
    {
        return updater.update();
    }

}
