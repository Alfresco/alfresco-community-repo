/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Simple action to increment an integer value. The runtime NodeService is used so any user
 * can increment the counter value on a node.
 * 
 * @author Kevin Roast
 */
public class CounterIncrementActionExecuter extends ActionExecuterAbstractBase
{
    /** Runtime NodeService with no permissions protection */
    private NodeService nodeService;
    
    
    /**
     * @param nodeService   The Runtime NodeService to set.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // add the cm:countable aspect as required
        if (this.nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_COUNTABLE) == false)
        {
            // set the value to 1 by default
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
            props.put(ContentModel.PROP_COUNTER, 1);
            this.nodeService.addAspect(actionedUponNodeRef, ContentModel.ASPECT_COUNTABLE, props);
        }
        else
        {
            // increment the value and handle possibility that no value has been set yet
            int resultValue = 1;
            Integer value = (Integer)this.nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_COUNTER);
            if (value != null)
            {
                resultValue = value.intValue() + 1;
            }
            this.nodeService.setProperty(actionedUponNodeRef, ContentModel.PROP_COUNTER, resultValue);
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // none required
    }
}
