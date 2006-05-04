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
