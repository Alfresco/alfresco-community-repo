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
