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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AbstractAuthenticationFilter
{

    private static Log logger = LogFactory.getLog(AbstractAuthenticationFilter.class);
    
    public AbstractAuthenticationFilter()
    {
        super();
    }


    /**
     * Parse the Accept-Lanaguage HTTP header value
     * 
     * @param req HttpServletRequest
     * @return Locale
     */
    protected static final Locale parseAcceptLanguageHeader(HttpServletRequest req, List<String> m_languages)
    {
        // Default the locale
        
        Locale locale = Locale.getDefault();
        
        // Get the accept language header value
        
        String acceptHeader = req.getHeader("Accept-Language");
        if ( acceptHeader != null)
        {
            // Parse the accepted language list
            
            StringTokenizer tokens = new StringTokenizer(acceptHeader, ",");
            List<AcceptLanguage> langList = new ArrayList<AcceptLanguage>();
            
            while ( tokens.hasMoreTokens())
            {
                // Get the current language token
                
                String lang = tokens.nextToken();
                float quality = 1.0f;
                
                // Check if the optional quality has been specified
                
                int qpos = lang.indexOf(";");
                if ( qpos != -1)
                {
                    // Parse the quality value
                    
                    try
                    {
                        quality = Float.parseFloat(lang.substring(qpos+3));
                    }
                    catch (NumberFormatException ex)
                    {
                        logger.error("Error parsing Accept-Language value " + lang);
                    }
                    
                    // Strip the quality value from the language token
                    
                    lang = lang.substring(0,qpos);
                }
                
                // Add the language to the list
                
                langList.add(new AcceptLanguage(lang, quality));
            }
            
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Accept-Language list : " + langList);
            
            // Match the client languages to the available locales
            
            if ( langList.size() > 0)
            {
                // Search for the best match locale to use for this client
    
                AcceptLanguage useLang = null;
                String useName = null;
                boolean match = false;
    
                for ( AcceptLanguage curLang : langList)
                {
                    // Match against the available languages
                    
                    for(String availLang : m_languages)
                    {
                        // The accept language may be in 'cc' or 'cc_cc' format
    
                        match = false;
                        
                        if ( curLang.getLanguage().length() == 2)
                        {
                            if ( availLang.startsWith(curLang.getLanguage()))
                                match = true;
                        }
                        else if ( availLang.equalsIgnoreCase(curLang.getLanguage()))
                            match = true;
                        
                        // If we found a match check if it is a higher quality than the current match.
                        // If the quality is the same we stick with the existing match as it was nearer the
                        // start of the list.
                        
                        if ( match == true)
                        {
                            if ( useLang == null ||
                                    ( curLang.getQuality() > useLang.getQuality()))
                            {
                                useLang = curLang;
                                useName = availLang;
                            }
                        }
                    }
                }
                
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Accept-Language using " + (useLang != null ? useLang.toString() : "<none>"));
                
                // Create the required user locale
                
                if ( useLang != null)
                {
                    Locale useLocale = AcceptLanguage.createLocale(useName);
                    if ( useLocale != null)
                    {
                        locale = useLocale;
                        
                        // Debug
                        
                        if ( logger.isDebugEnabled())
                            logger.debug("Using language " + useLang + ", locale " + locale);
                    }
                }
            }
        }
    
        // Return the selected locale
        
        return locale;
    }

}
