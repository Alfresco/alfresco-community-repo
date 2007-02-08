/*
 * Copyright (C) 2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service.cmr.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import org.alfresco.i18n.I18NUtil;

/**
 * Class to represent a multilingual (ML) text value.
 * <p>
 * The language codes used should conform to the
 * {@linkplain http://www.loc.gov/standards/iso639-2/php/English_list.php ISO639-2}
 * language code standard, although there is no enforcement of the standard in this
 * class.
 * <p>
 * This is a simple extension of a <code>HashMap</code> with a few convenience methods.
 * 
 * @see <a href=http://www.loc.gov/standards/iso639-2/php/English_list.php>ISO639-2</a>
 * 
 * @author Philippe Dubois
 * @author Derek Hulley
 */
public class MLText extends HashMap<Locale, String>
{
    private static final long serialVersionUID = -3696135175650511841L;

    private Locale defaultLocale;
    
    public MLText()
    {
        super(3, 0.75F);
    }
    
    /**
     * Construct an instance with a value corresponding to the current context locale.
     * 
     * @param value the value for the current default locale
     * 
     * @see I18NUtil#getLocale()
     * @see #MLText(Locale, String)
     * @see #getDefaultValue()
     */
    public MLText(String value)
    {
        this(I18NUtil.getLocale(), value);
    }
    
    /**
     * Construct an instance with a value for the given locale.
     * 
     * @param locale the locale
     * @param value the value
     * 
     * @see #getDefaultValue()
     */
    public MLText(Locale locale, String value)
    {
        super(3, 0.75F);
        defaultLocale = locale;
        super.put(locale, value);
    }

    /**
     * @return Returns all the language locales defined in the text
     */
    public Set<Locale> getLocales()
    {
        return keySet();
    }
    
    /**
     * @return Returns all the values stored
     */
    public Collection<String> getValues()
    {
        return values();
    }

    /**
     * Add a multilingual text value
     * 
     * @param locale the language locale
     * @param value the multilingual text
     */
    public void addValue(Locale locale, String value)
    {
        put(locale, value);
    }
    
    /**
     * Retrieve a multilingual text value
     * 
     * @param locale the language locale
     */
    public String getValue(Locale locale)
    {
        return get(locale);
    }
    
    /**
     * Retrieves a default value from the set of available locales.<br/>
     * 
     * @see I18NUtil#getLocale()
     * @see #getClosestValue(Locale)
     */
    public String getDefaultValue()
    {
        Locale locale = I18NUtil.getLocale();
        return getClosestValue(locale);
    }
    
    /**
     * The given locale is used to search for a matching value according to:
     * <ul>
     *   <li>An exact locale match</li>
     *   <li>A match of locale ISO language codes</li>
     *   <li>The value for the locale provided in the {@link MLText#MLText(Locale, String) constructor}</li>
     *   <li>An arbitrary value</li>
     *   <li><tt>null</tt></li>
     * </ul>
     * 
     * @param locale the locale to use as the starting point of the value search
     * @return Returns a default <tt>String</tt> value or null if one isn't available.
     *      <tt>null</tt> will only be returned if there are no values associated with
     *      this instance.  With or without a match, the return value may be <tt>null</tt>,
     *      depending on the values associated with the locales.
     */
    public String getClosestValue(Locale locale)
    {
        if (this.size() == 0)
        {
            return null;
        }
        // Use the available keys as options
        Set<Locale> options = keySet();
        // Get a match
        Locale match = I18NUtil.getNearestLocale(locale, options);
        if (match == null)
        {
            // No close matches for the locale - go for the default locale
            locale = defaultLocale;
            match = I18NUtil.getNearestLocale(locale, options);
            if (match == null)
            {
                // just get any locale
                match = I18NUtil.getNearestLocale(null, options);
            }
        }
        // Did we get a match
        if (match == null)
        {
            // We could find no locale matches
            return null;
        }
        else
        {
            return get(match);
        }
    }
    
    /**
     * Remove a multilingual text value
     * 
     * @param locale the language locale
     */
    public void removeValue(Locale locale)
    {
        remove(locale);
    }
}