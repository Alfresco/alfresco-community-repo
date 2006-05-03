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

package org.alfresco.web.app.servlet;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Accept Language Class
 * 
 * <p>Holds the details of an accepted language from a HTTP Accept-Language header
 * 
 * @author gkspencer
 */
class AcceptLanguage
{
    // Language name
    
    private String m_language;
    
    // Quality
    
    private float m_quality = 1.0f;
    
    /**
     * Class constructor
     * 
     * @param lang String
     * @param quality float
     */
    public AcceptLanguage(String lang, float quality)
    {
        // Convert the language to Java format
        
        m_language = lang.replace('-', '_');
        m_quality  = quality;
    }
    
    /**
     * Return the language
     * 
     * @return String
     */
    public final String getLanguage()
    {
        return m_language;
    }
    
    /**
     * Return the quality
     * 
     * @return float
     */
    public final float getQuality()
    {
        return m_quality;
    }
    
    /**
     * Create a locale for this language
     * 
     * @return Locale
     */
    public final Locale createLocale()
    {
        return createLocale(getLanguage());
    }
    
    /**
     * Create a locale for this language
     * 
     * @param locName String
     * @return Locale
     */
    public final static Locale createLocale(String locName)
    {
        Locale locale = null;
        
        StringTokenizer t = new StringTokenizer(locName, "_");
        int tokens = t.countTokens();
        if (tokens == 1)
        {
           locale = new Locale(locName);
        }
        else if (tokens == 2)
        {
           locale = new Locale(t.nextToken(), t.nextToken());
        }
        else if (tokens == 3)
        {
           locale = new Locale(t.nextToken(), t.nextToken(), t.nextToken());
        }
        
        return locale;
    }
    
    /**
     * Return the accept language as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[");
        str.append(getLanguage());
        str.append(",");
        str.append(getQuality());
        str.append("]");
        
        return str.toString();
    }
}
