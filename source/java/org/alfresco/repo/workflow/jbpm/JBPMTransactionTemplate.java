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
    

    /**
     * {@inheritDoc}
      */
    @SuppressWarnings("synthetic-access")
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

    
    /**
     * {@inheritDoc}
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


    /**
     * {@inheritDoc}
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


    /**
     * {@inheritDoc}
      */
    public void flush()
    {
        //NOOP
    }

    
    /**
     * {@inheritDoc}
      */
    public void beforeCommit(boolean readOnly)
    {
        //NOOP
    }

    
    /**
    * {@inheritDoc}
     */
    public void beforeCompletion()
    {
        //NOOP
    }

    
    /**
     * {@inheritDoc}
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

    
    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
      */
    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }
    
    /**
     * {@inheritDoc}
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
