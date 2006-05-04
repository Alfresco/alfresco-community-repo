/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.repo.transaction;

import java.util.Properties;

import javax.transaction.TransactionManager;

import org.jboss.cache.TransactionManagerLookup;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;

/**
 * Helper lookup class to supply JBoss components with a <code>TransactionManager</code>.
 * <p>
 * The <code>JBossTransactionManagerLookup</code> will work when Alfresco is running in JBoss,
 * but the <code>TreeCache</code> can be used within other containers; there might not be any
 * container and the <code>TransactionManager</code> may held in a local JNDI tree.
 * <p>
 * For compatibility with other app servers, the JBoss <code>GenericTransactionManagerLookup</code>
 * could also be used.
 * <p>
 * The default constructor configures the object to look in <b>java:/TransactionManager</b>
 * for a <code>TransactionManager</code>.  The only customisation that should be required is
 * to change the {@link #setJndiName(String) jndiName} property.  If more JNDI details need
 * changing, then the actual {@link #setJndiLookup(JndiObjectFactoryBean) jndiLookup object} can
 * be substituted with a customized version.
 * 
 * @author Derek Hulley
 */
public class TransactionManagerJndiLookup implements TransactionManagerLookup
{
    public static final String DEFAULT_JNDI_NAME = "java:/TransactionManager";
    
    private JndiObjectFactoryBean jndiLookup;
    
    public TransactionManagerJndiLookup()
    {
        jndiLookup = new JndiObjectFactoryBean();
        jndiLookup.setJndiName(DEFAULT_JNDI_NAME);
        jndiLookup.setProxyInterface(TransactionManager.class);
    }
    
    /**
     * @see org.springframework.jndi.JndiAccessor#setJndiTemplate(org.springframework.jndi.JndiTemplate)
     */
    public void setJndiTemplate(JndiTemplate jndiTemplate)
    {
        this.jndiLookup.setJndiTemplate(jndiTemplate);
    }

    /**
     * @see org.springframework.jndi.JndiAccessor#setJndiEnvironment(java.util.Properties)
     */
    public void setJndiEnvironment(Properties jndiEnvironment)
    {
        this.jndiLookup.setJndiEnvironment(jndiEnvironment);
    }

    /**
     * Set the JNDI location where the <code>TransactionManager</code> can be found.
     * 
     * @param jndiName
     */
    public void setJndiName(String jndiName)
    {
        jndiLookup.setJndiName(jndiName);
    }

    /**
     * @return Returns a <code>TransactionManager</code> looked up at the JNDI location
     */
    public TransactionManager getTransactionManager() throws Exception
    {
        return (TransactionManager) jndiLookup.getObject();
    }
}
