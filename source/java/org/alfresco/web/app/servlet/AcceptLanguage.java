/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
