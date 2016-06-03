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
	public static final String PARAM_CONSTRAINT = "ac-aspects";
	
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
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.action.Action, NodeRef)
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
