/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.workflow.jbpm;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.springmodules.workflow.jbpm31.JbpmTemplate;


/**
 * JBPM Template that manages JBPM Context at the Alfresco Transaction level
 * 
 * @author davidc
 */
public class JBPMTransactionTemplate extends JbpmTemplate
    implements TransactionListener
{
    // Logging support
    private static Log logger = LogFactory.getLog("org.alfresco.repo.workflow");
    
    /** Id used in equals and hash */
    private String id = GUID.generate();
    
    // JBPM Template Keys
    private static final String JBPM_CONTEXT_KEY = JBPMTransactionTemplate.class.getName() + ".context";

    /** Use local or transaction bound JBPM Context */
    private boolean localContext = true;
    

    /*
     * Constructor
     */
    public JBPMTransactionTemplate()
    {
        super();
    }

    /*
     * Constructor
     */
    public JBPMTransactionTemplate(JbpmConfiguration jbpmConfiguration, ProcessDefinition processDefinition)
    {
        super(jbpmConfiguration, processDefinition);
    }

    /*
     * Constructor
     */
    public JBPMTransactionTemplate(JbpmConfiguration jbpmConfiguration)
    {
        super(jbpmConfiguration);
    }
    

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        try
        {
            JBPMTransactionTemplate.super.afterPropertiesSet();
        }
        finally
        {
            localContext = false;
        }
    }

    
    /* (non-Javadoc)
     * @see org.springmodules.workflow.jbpm31.JbpmTemplate#getContext()
     */
    @Override
    protected JbpmContext getContext()
    {
        if (localContext)
        {
            return super.getContext();
        }
        else
        {
            JbpmContext context = (JbpmContext)AlfrescoTransactionSupport.getResource(JBPM_CONTEXT_KEY);
            if (context == null)
            {
                context = super.getContext();
                AlfrescoTransactionSupport.bindResource(JBPM_CONTEXT_KEY, context);
                AlfrescoTransactionSupport.bindListener(this);
    
                if (logger.isDebugEnabled())
                    logger.debug("Attached JBPM Context to transaction " + AlfrescoTransactionSupport.getTransactionId());
            }
            return context;
        }
    }


    /* (non-Javadoc)
     * @see org.springmodules.workflow.jbpm31.JbpmTemplate#releaseContext(org.jbpm.JbpmContext)
     */
    @Override
    protected void releaseContext(JbpmContext jbpmContext)
    {
        if (localContext)
        {
            jbpmContext.close();
        }
        else
        {
            // NOTE: Defer release to end of transaction
        }
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#flush()
     */
    public void flush()
    {
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
     */
    public void beforeCommit(boolean readOnly)
    {
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
     */
    public void beforeCompletion()
    {
        // TODO Auto-generated method stub

    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
     */
    public void afterCommit()
    {
        JbpmContext context = (JbpmContext)AlfrescoTransactionSupport.getResource(JBPM_CONTEXT_KEY);
        if (context != null)
        {
            super.releaseContext(context);

            if (logger.isDebugEnabled())
                logger.debug("Detached (commit) JBPM Context from transaction " + AlfrescoTransactionSupport.getTransactionId());
        }
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
     */
    public void afterRollback()
    {
        JbpmContext context = (JbpmContext)AlfrescoTransactionSupport.getResource(JBPM_CONTEXT_KEY);
        if (context != null)
        {
            super.releaseContext(context);

            if (logger.isDebugEnabled())
                logger.debug("Detached (rollback) JBPM Context from transaction " + AlfrescoTransactionSupport.getTransactionId());
        }
    }

    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof JBPMTransactionTemplate)
        {
            JBPMTransactionTemplate that = (JBPMTransactionTemplate) obj;
            return (this.id.equals(that.id));
        }
        else
        {
            return false;
        }
    }
    
}
