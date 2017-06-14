/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
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
    @SuppressWarnings("unused")
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
