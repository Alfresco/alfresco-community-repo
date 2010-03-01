/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.search;

import java.util.HashSet;
import java.util.Locale;

import junit.framework.TestCase;

public class MLAnaysisModeExpansionTest extends TestCase
{

    public MLAnaysisModeExpansionTest()
    {
        super();
    }

    public MLAnaysisModeExpansionTest(String arg0)
    {
        super(arg0);
    }
    
    public void testIdentity()
    {
        Locale locale = Locale.UK;
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_ONLY, locale, false));
        assertEquals(1, locales.size());
        assertTrue(locales.contains(locale));
    }
    
    public void testIdentityAndAll()
    {
        Locale locale = Locale.UK;
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL, locale, false));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(locale));
        assertTrue(locales.contains(new Locale("", "", "")));
    }
    
    public void testAll()
    {
        Locale locale = Locale.UK;
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_ONLY, locale, false));
        assertEquals(1, locales.size());
        assertTrue(locales.contains(new Locale("", "", "")));
    }
    
    public void testContaining()
    {
        Locale locale = Locale.UK;
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINING_LOCALES, locale, false));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
    }
    
    public void testContainingAndAll()
    {
        Locale locale = Locale.UK;
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINING_LOCALES_AND_ALL, locale, false));
        assertEquals(3, locales.size());
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
    }

    public void testContained()
    {
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINED_LOCALES, new Locale("en", "", ""), false));
        assertTrue(locales.size()>= 9);
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "AU", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "US", "")));
        assertTrue(locales.contains(new Locale("en", "ZA", "")));
        assertTrue(locales.contains(new Locale("en", "CA", "")));
        assertTrue(locales.contains(new Locale("en", "IE", "")));
        assertTrue(locales.contains(new Locale("en", "NZ", "")));
        assertTrue(locales.contains(new Locale("en", "IN", "")));      
    }
    
    public void testLang()
    {
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINED_LOCALES, new Locale("en", "GB", ""), false));
        assertTrue(locales.size() >= 1);
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_LANGUAGES, new Locale("en", "GB", ""), false));
        assertTrue(locales.size()>= 9);
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "AU", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "US", "")));
        assertTrue(locales.contains(new Locale("en", "ZA", "")));
        assertTrue(locales.contains(new Locale("en", "CA", "")));
        assertTrue(locales.contains(new Locale("en", "IE", "")));
        assertTrue(locales.contains(new Locale("en", "NZ", "")));
        assertTrue(locales.contains(new Locale("en", "IN", ""))); 
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_LANGUAGES_AND_ALL, new Locale("en", "GB", ""), false));
        assertTrue(locales.size()>= 10);
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "AU", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "US", "")));
        assertTrue(locales.contains(new Locale("en", "ZA", "")));
        assertTrue(locales.contains(new Locale("en", "CA", "")));
        assertTrue(locales.contains(new Locale("en", "IE", "")));
        assertTrue(locales.contains(new Locale("en", "NZ", "")));
        assertTrue(locales.contains(new Locale("en", "IN", ""))); 
    }
    
    public void testExactLang()
    {
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINED_LOCALES, new Locale("en", "GB", ""), false));
        assertTrue(locales.size() >= 1);
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_LANGUAGE, new Locale("en", "GB", ""), false));
        assertEquals(1, locales.size());
        assertTrue(locales.contains(new Locale("en", "", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_LANGUAGE_AND_ALL, new Locale("en", "GB", ""), false));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("en", "", "")));
    }
    
    public void testCountry()
    {
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINED_LOCALES, new Locale("en", "GB", ""), false));
        assertTrue(locales.size() >= 1);
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_COUNTRIES, new Locale("en", "", ""), false));
        assertTrue(locales.size()>= 9);
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "AU", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "US", "")));
        assertTrue(locales.contains(new Locale("en", "ZA", "")));
        assertTrue(locales.contains(new Locale("en", "CA", "")));
        assertTrue(locales.contains(new Locale("en", "IE", "")));
        assertTrue(locales.contains(new Locale("en", "NZ", "")));
        assertTrue(locales.contains(new Locale("en", "IN", ""))); 
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_COUNTRIES, new Locale("en", "GB", ""), false));
        assertTrue(locales.size() >= 1);
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_COUNTRIES_AND_ALL, new Locale("en", "", ""), false));
        assertTrue(locales.size()>= 10);
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "AU", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "US", "")));
        assertTrue(locales.contains(new Locale("en", "ZA", "")));
        assertTrue(locales.contains(new Locale("en", "CA", "")));
        assertTrue(locales.contains(new Locale("en", "IE", "")));
        assertTrue(locales.contains(new Locale("en", "NZ", "")));
        assertTrue(locales.contains(new Locale("en", "IN", ""))); 
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_COUNTRIES_AND_ALL, new Locale("en", "GB", ""), false));
        assertTrue(locales.size() >= 2);
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
    }
    
    public void testExactCountry()
    {
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINED_LOCALES, new Locale("en", "GB", ""), false));
        assertTrue(locales.size() >= 1);
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_COUNRTY, new Locale("en", "GB", ""), false));
        assertEquals(1, locales.size());
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_COUNRTY, new Locale("en", "", ""), false));
        assertTrue(locales.size()>= 9);
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "AU", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "US", "")));
        assertTrue(locales.contains(new Locale("en", "ZA", "")));
        assertTrue(locales.contains(new Locale("en", "CA", "")));
        assertTrue(locales.contains(new Locale("en", "IE", "")));
        assertTrue(locales.contains(new Locale("en", "NZ", "")));
        assertTrue(locales.contains(new Locale("en", "IN", ""))); 
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_COUNTRY_AND_ALL, new Locale("en", "GB", ""), false));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_COUNTRY_AND_ALL, new Locale("en", "", ""), false));
        assertTrue(locales.size()>= 10);
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "AU", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "US", "")));
        assertTrue(locales.contains(new Locale("en", "ZA", "")));
        assertTrue(locales.contains(new Locale("en", "CA", "")));
        assertTrue(locales.contains(new Locale("en", "IE", "")));
        assertTrue(locales.contains(new Locale("en", "NZ", "")));
        assertTrue(locales.contains(new Locale("en", "IN", ""))); 
    }
    
    
    public void testIdentityWC()
    {
        Locale locale = Locale.UK;
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_ONLY, locale, true));
        assertEquals(1, locales.size());
        assertTrue(locales.contains(locale));
    }
    
    public void testIdentityAndAllWC()
    {
        Locale locale = Locale.UK;
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL, locale, true));
        assertEquals(3, locales.size());
        assertTrue(locales.contains(locale));
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("*", "", "")));
    }
    
    public void testAllWC()
    {
        Locale locale = Locale.UK;
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_ONLY, locale, true));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("*", "", "")));
    }
    
    public void testContainingWC()
    {
        Locale locale = Locale.UK;
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINING_LOCALES, locale, true));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
    }
    
    public void testContainingAndAllWC()
    {
        Locale locale = Locale.UK;
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINING_LOCALES_AND_ALL, locale, true));
        assertEquals(4, locales.size());
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("*", "", "")));
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
    }

    public void testContainedWC()
    {
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINED_LOCALES, new Locale("en", "", ""), true));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("en", "*", ""))); 
        assertTrue(locales.contains(new Locale("en", "", ""))); 
    }
    
    public void testLangWC()
    {
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINED_LOCALES, new Locale("en", "GB", ""), true));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "*")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_LANGUAGES, new Locale("en", "GB", ""), true));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "*", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_LANGUAGES_AND_ALL, new Locale("en", "GB", ""), true));
        assertEquals(4, locales.size());
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("*", "", "")));
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "*", "")));
    }
    
    public void testExactLangWC()
    {
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINED_LOCALES, new Locale("en", "GB", ""), true));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "*")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_LANGUAGE, new Locale("en", "GB", ""), true));
        assertEquals(1, locales.size());
        assertTrue(locales.contains(new Locale("en", "", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_LANGUAGE_AND_ALL, new Locale("en", "GB", ""), true));
        assertEquals(3, locales.size());
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("*", "", "")));
        assertTrue(locales.contains(new Locale("en", "", "")));
    }
    
    public void testCountryWC()
    {
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINED_LOCALES, new Locale("en", "GB", ""), true));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "*")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_COUNTRIES, new Locale("en", "", ""), true));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "*", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_COUNTRIES, new Locale("en", "GB", ""), true));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "*")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_COUNTRIES_AND_ALL, new Locale("en", "", ""), true));
        assertEquals(4, locales.size());
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("*", "", "")));
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "*", "")));
  
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.ALL_COUNTRIES_AND_ALL, new Locale("en", "GB", ""), true));
        assertEquals(4, locales.size());
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("*", "", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "*")));
    }
    
    public void testExactCountryWC()
    {
        HashSet<Locale> locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.LOCALE_AND_ALL_CONTAINED_LOCALES, new Locale("en", "GB", ""), true));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "*")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_COUNRTY, new Locale("en", "GB", ""), true));
        assertEquals(1, locales.size());
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_COUNRTY, new Locale("en", "", ""), true));
        assertEquals(2, locales.size());
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "*", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_COUNTRY_AND_ALL, new Locale("en", "GB", ""), true));
        assertEquals(3, locales.size());
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("*", "", "")));
        assertTrue(locales.contains(new Locale("en", "GB", "")));
        
        locales = new HashSet<Locale>();
        locales.addAll(MLAnalysisMode.getLocales(MLAnalysisMode.EXACT_COUNTRY_AND_ALL, new Locale("en", "", ""), true));
        assertTrue(locales.contains(new Locale("", "", "")));
        assertTrue(locales.contains(new Locale("*", "", "")));
        assertTrue(locales.contains(new Locale("en", "", "")));
        assertTrue(locales.contains(new Locale("en", "*", "")));
    }
    
}
