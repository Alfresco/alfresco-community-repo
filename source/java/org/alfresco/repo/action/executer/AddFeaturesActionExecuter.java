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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * Add features action executor implementation.
 * 
 * @author Roy Wetherall
 */
public class AddFeaturesActionExecuter extends ActionExecuterAbstractBase
{
    /**
     * Action constants
     */
	public static final String NAME = "add-features";
	public static final String PARAM_ASPECT_NAME = "aspect-name";
	
	/**
	 * The node service
	 */
	private NodeService nodeService;
	
    /** Transaction Service, used for retrying operations */
    private TransactionService transactionService;
    
    /**
     * Set the node service
     * 
     * @param nodeService  the node service 
     */
	public void setNodeService(NodeService nodeService) 
	{
		this.nodeService = nodeService;
	}
	
    /**
     * Set the transaction service
     * 
     * @param transactionService    the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
	/**
	 * Adhoc properties are allowed for this executor
	 */
	@Override
	protected boolean getAdhocPropertiesAllowed()
	{
		return true;
	}

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef, NodeRef)
     */
    public void executeImpl(final Action ruleAction, final NodeRef actionedUponNodeRef)
    {
        if (this.nodeService.exists(actionedUponNodeRef))
        {
           transactionService.getRetryingTransactionHelper().doInTransaction(
              new RetryingTransactionCallback<Void>() {
                 public Void execute() throws Throwable {
                     Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                     QName aspectQName = null;

                     if(! nodeService.exists(actionedUponNodeRef))
                     {
                         // Node has gone away, skip
                         return null;
                     }

                     // Build the aspect details
                     Map<String, Serializable> paramValues = ruleAction.getParameterValues();
                     for (Map.Entry<String, Serializable> entry : paramValues.entrySet())
                     {
                         if (entry.getKey().equals(PARAM_ASPECT_NAME) == true)
                         {
                             aspectQName = (QName)entry.getValue();
                         }
                         else
                         {
                             // Must be an adhoc property
                             QName propertyQName = QName.createQName(entry.getKey());
                             Serializable propertyValue = entry.getValue();
                             properties.put(propertyQName, propertyValue);
                         }
                     }

                     // Add the aspect
                     nodeService.addAspect(actionedUponNodeRef, aspectQName, properties);
                     return null;
                 }
              }
           );
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
		paramList.add(new ParameterDefinitionImpl(PARAM_ASPECT_NAME, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_ASPECT_NAME), false, "ac-aspects"));
	}

}
