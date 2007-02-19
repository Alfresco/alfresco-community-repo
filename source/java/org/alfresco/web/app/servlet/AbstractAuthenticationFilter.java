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
