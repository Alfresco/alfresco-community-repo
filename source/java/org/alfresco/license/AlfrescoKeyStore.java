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
package org.alfresco.license;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.schlichtherle.license.KeyStoreParam;


/**
 * Alfresco Public KeyStore Parameters
 * 
 * @author davidc
 */
public class AlfrescoKeyStore implements KeyStoreParam
{
    // location of alfresco public keystore
    private final static String KEYSTORE = "/org/alfresco/license/alfresco.keystore";
    

    /* (non-Javadoc)
     * @see de.schlichtherle.license.KeyStoreParam#getStream()
     */
    public InputStream getStream() throws IOException
    {
        final InputStream in = getClass().getResourceAsStream(KEYSTORE);
        if (in == null)
        {
            throw new FileNotFoundException(KEYSTORE);
        }
        return in;
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.KeyStoreParam#getStorePwd()
     */
    public String getStorePwd()
    {
        return "ocs3rf1a";
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.KeyStoreParam#getAlias()
     */
    public String getAlias()
    {
        return "alfresco";
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.KeyStoreParam#getKeyPwd()
     */
    public String getKeyPwd()
    {
        // Note: not required for public key
        return null;
    }
        
}
