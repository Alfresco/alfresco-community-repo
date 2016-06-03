package org.alfresco.repo.action.script;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.repo.jscript.Scopeable;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.mozilla.javascript.Scriptable;

/**
 * ExecutionDetails JavaScript Object. This class is a JavaScript-friendly wrapper for
 *  the {@link ExecutionDetails} (and embeded {@link ExecutionSummary}) class.
 * 
 * @author Nick Burch
 * @see org.alfresco.service.cmr.action.ExecutionDetails
 */
public final class ScriptExecutionDetails implements Serializable, Scopeable
{
    private static final long serialVersionUID = 3182925511891455490L;
    
    /** Root scope for this object */
    private Scriptable scope;
    
    /** The details we wrap */
    private ExecutionDetails details;
    
    /** Services, used when building Script objects */
    private ServiceRegistry services;
    
    public ScriptExecutionDetails(ExecutionDetails details, ServiceRegistry services)
    {
    	 this.details = details;
    	 this.services = services;
    }
    
    protected ExecutionDetails getExecutionDetails() 
    {
       return details;
    }
    

    public String getActionType() {
       return details.getActionType();
    }

    public String getActionId() {
       return details.getActionId();
    }

    public int getExecutionInstance() {
       return details.getExecutionInstance();
    }
    
    public ScriptNode getPersistedActionRef() {
       return new ScriptNode(details.getPersistedActionRef(), services); 
    }

    public String getRunningOn() {
       return details.getRunningOn();
    }

    public Date getStartedAt() {
       return details.getStartedAt();
    }

    public boolean isCancelRequested() {
       return details.isCancelRequested();
    }

    /**
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }

    @Override
    public String toString() 
    {
       StringBuilder builder = new StringBuilder();
       builder.append("Executing Action: ");
       builder.append(details.getActionType()).append(' ');
       builder.append(details.getActionId()).append(' ');
       builder.append(details.getExecutionInstance()).append(' ');
       if(details.getPersistedActionRef() != null)
       {
          builder.append(details.getPersistedActionRef());
       }
       
       return builder.toString();
    }
}
