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
package org.alfresco.repo.security.authentication.ldap;

import java.util.Map;

import javax.naming.directory.InitialDirContext;

import org.alfresco.repo.security.authentication.AuthenticationException;

/**
 * Interface that defines a factory for obtaining ldap directory contexts.
 * 
 * @author Andy Hind
 */
public interface LDAPInitialDirContextFactory
{
    /**
     * Set the LDAP environment Hashtable properties used ot initialise the LDAP connection.
     * 
     * @param environment
     */
    public void setInitialDirContextEnvironment(Map<String, String> environment);
    
    /**
     * Use the environment properties and connect to the LDAP server.
     * Used to obtain read only access to the LDAP server.
     * 
     * @return
     * @throws AuthenticationException
     */
    public InitialDirContext getDefaultIntialDirContext() throws AuthenticationException;
    
    /**
     * Augment the connection environment with the identity and credentials and bind to the ldap server.
     * Mainly used to validate a user's credentials during authentication. 
     * 
     * @param principal
     * @param credentials
     * @return
     * @throws AuthenticationException
     */
    public InitialDirContext getInitialDirContext(String principal, String credentials)  throws AuthenticationException;
}
