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

import java.util.prefs.Preferences;

import javax.security.auth.x500.X500Principal;

import org.alfresco.service.descriptor.DescriptorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.schlichtherle.license.CipherParam;
import de.schlichtherle.license.KeyStoreParam;
import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.ftp.LicenseParam;


/**
 * Alfresco License Parameters
 *  
 * @author davidc
 */
public class AlfrescoLicenseParam implements LicenseParam
{
    private static final Log logger = LogFactory.getLog(DescriptorService.class);
    
    private KeyStoreParam alfrescoStore = new AlfrescoKeyStore();
    private KeyStoreParam trialStore = new TrialKeyStore();
    private CipherParam cipherParam = new CipherParamImpl();
    private boolean createTrialLicense = true;
    private int days = 30;
    

    /**
     * Construct
     * 
     * @param createTrialLicense  allow the creation of trial license
     */
    public AlfrescoLicenseParam(boolean createTrialLicense)
    {
        this.createTrialLicense = createTrialLicense;
    }
   
    /* (non-Javadoc)
     * @see de.schlichtherle.license.ftp.LicenseParam#getFTPKeyStoreParam()
     */
    public KeyStoreParam getFTPKeyStoreParam()
    {
        return trialStore;
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.ftp.LicenseParam#getFTPDays()
     */
    public int getFTPDays()
    {
        return days;
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.ftp.LicenseParam#isFTPEligible()
     */
    public boolean isFTPEligible()
    {
        return createTrialLicense;
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.ftp.LicenseParam#createFTPLicenseContent()
     */
    public LicenseContent createFTPLicenseContent()
    {
        if (logger.isInfoEnabled())
            logger.info("Alfresco license: Creating time limited trial license");
            
        LicenseContent result = new LicenseContent();
        X500Principal holder = new X500Principal("O=Trial User");
        result.setHolder(holder);
        X500Principal issuer = new X500Principal("CN=Unknown, OU=Unknown, O=Alfresco, L=Maidenhead, ST=Berkshire, C=UK");
        result.setIssuer(issuer);
        result.setConsumerType("System");
        result.setConsumerAmount(1);
        return result;
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.ftp.LicenseParam#removeFTPEligibility()
     */
    public void removeFTPEligibility()
    {
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.ftp.LicenseParam#ftpGranted(de.schlichtherle.license.LicenseContent)
     */
    public void ftpGranted(LicenseContent content)
    {
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.LicenseParam#getSubject()
     */
    public String getSubject()
    {
        return "Enterprise Network";
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.LicenseParam#getPreferences()
     */
    public Preferences getPreferences()
    {
        // note: Alfresco license manager does not store licenses in Preferences
        return null;
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.LicenseParam#getKeyStoreParam()
     */
    public KeyStoreParam getKeyStoreParam()
    {
        return alfrescoStore;
    }

    /* (non-Javadoc)
     * @see de.schlichtherle.license.LicenseParam#getCipherParam()
     */
    public CipherParam getCipherParam()
    {
        return cipherParam;
    }

}
