package org.alfresco.repo.activities.feed;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.action.executer.TestModeable;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class ErrorProneActionExecutor extends ActionExecuterAbstractBase
                                implements InitializingBean, TestModeable
{
    private static Log logger = LogFactory.getLog(ErrorProneActionExecutor.class);

    public static final String PARAM_FAILING_PERSON_NODEREF = "failingPersonNodeRef";
    public static final String PARAM_PERSON_NODEREF = "personNodeRef";
    public static final String PARAM_USERNAME = "userName";

    public static final String NAME = "errorProneActionExecutor";

    // count of number of successful notifications
	private AtomicInteger numSuccessful = new AtomicInteger();
	
    // count of number of failed notifications
	private AtomicInteger numFailed = new AtomicInteger();
    
	public int getNumSuccess()
	{
		return numSuccessful.get();
	}
	
	public int getNumFailed()
	{
		return numFailed.get();
	}

    /**
     * Send an email message
     * 
     * @throws AlfrescoRuntimeExeption
     */
    @Override
    protected void executeImpl(
            final Action ruleAction,
            final NodeRef actionedUponNodeRef) 
    {
		NodeRef failingPersonNodeRef = (NodeRef)ruleAction.getParameterValue(PARAM_FAILING_PERSON_NODEREF);
		NodeRef personNodeRef = (NodeRef)ruleAction.getParameterValue(PARAM_PERSON_NODEREF);
		String userName = (String)ruleAction.getParameterValue(PARAM_USERNAME);

		System.out.println("userName = " + userName);

		if(personNodeRef.equals(failingPersonNodeRef))
		{
			numFailed.incrementAndGet();
			throw new AlfrescoRuntimeException("");
		}

		numSuccessful.incrementAndGet();
    }
    
    /**
     * Add the parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_FAILING_PERSON_NODEREF, DataTypeDefinition.NODE_REF, true, "Failing Person NodeRef"));
        paramList.add(new ParameterDefinitionImpl(PARAM_PERSON_NODEREF, DataTypeDefinition.NODE_REF, true, "Person NodeRef"));
        paramList.add(new ParameterDefinitionImpl(PARAM_USERNAME, DataTypeDefinition.TEXT, true, "Username"));
    }

	@Override
	public boolean isTestMode()
	{
		return true;
	}

	@Override
	public void setTestMode(boolean testMode)
	{
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		
	}
}
