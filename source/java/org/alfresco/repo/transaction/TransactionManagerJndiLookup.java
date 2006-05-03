/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Alfresco Network License. You may obtain a
 * copy of the License at
 *
 *   http://www.alfrescosoftware.com/legal/
 *
 * Please view the license relevant to your network subscription.
 *
 * BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 * READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 * YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 * ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 * THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 * AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 * TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 * BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 * HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 * SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 * TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 * CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
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
