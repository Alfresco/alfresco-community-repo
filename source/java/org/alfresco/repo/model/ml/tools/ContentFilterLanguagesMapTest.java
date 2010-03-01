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
package org.alfresco.repo.model.ml.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Content filter language service test cases
 * 
 * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService
 * @see org.alfresco.repo.model.ml.ContentFilterLanguagesMap
 * 
 * @author Yannick Pignot
 */
public class ContentFilterLanguagesMapTest extends AbstractMultilingualTestCases 
{
    
    public void testGetFilterLanguages() throws Exception
    {
        // get the list of content filter languages
        List<String> lggs = contentFilterLanguagesService.getFilterLanguages();

        // Ensure that the list is not null
        assertNotNull("Language list is null", lggs);
        
        // Ensure that the list is read-only
        try 
        {
            lggs.add("NEW LOCALE");
            assertTrue("Add a value to the content filter language list is not permit, this list would be read only", false);
        } 
        catch (Exception e) 
        {
            // test case ok
        }
        
        try 
        {
            lggs.remove(0);
            assertTrue("Remove a value to the content filter language list is not permit, this list would be read only", false);
        } 
        catch (Exception e) 
        {
            // test case ok                        
        }        
    }

    @SuppressWarnings("unchecked")
    public void testGetMissingLanguages() throws Exception
    {
        List<String> lggs = contentFilterLanguagesService.getFilterLanguages();
        
        // get missing languages with null parameter
        List<String> missingLggsNull = contentFilterLanguagesService.getMissingLanguages(null);
        
        // Ensure that the returned list is not null
        assertNotNull("Language list returned with the null parameter is null", missingLggsNull);
        // Ensure that the returned list is entire
        assertEquals("Language list returned with the null parameter  corrupted", missingLggsNull.size(), lggs.size());
        
        // get missing languages with empty collection parameter
        List<String> missingLggsEmpty = contentFilterLanguagesService.getMissingLanguages(Collections.EMPTY_LIST);
        
        // Ensure that the returned list is not null
        assertNotNull("Language list returned with the empty parameter is null", missingLggsEmpty);
        // Ensure that the returned list is entire
        assertEquals("Language list returned with the empty parameter  corrupted", missingLggsEmpty.size(), lggs.size());
        
        // get missing languages with a two locale list 
        List<String> param = new ArrayList<String>();
        param.add(0, lggs.get(0));
        param.add(1, lggs.get(1));
        List<String> missingLggsOk = contentFilterLanguagesService.getMissingLanguages(param);
        
        // Ensure that the returned list is not null
        assertNotNull("Language list returned with the correct parameter is null", missingLggsOk);
        // Ensure that the returned list size is correct
        assertEquals("Language list size returned with the correct parameter is not correct", missingLggsOk.size(), (lggs.size() - 2));
        // Ensure that the returned list don't content the preceding locales 
        assertFalse("Language found : " + param.get(0), missingLggsOk.contains(param.get(0)));
        assertFalse("Language found : " + param.get(1), missingLggsOk.contains(param.get(1)));
        //    get missing languages with a not found locale
        param.add(2, "WRONG LOCALE CODE");
        List<String> missingLggsWrong = contentFilterLanguagesService.getMissingLanguages(param);
        
        // Ensure that the returned list is not null
        assertNotNull("Language list returned with the wrong parameter is null", missingLggsWrong);
        // Ensure that the returned list size is correct
        assertEquals("Language list size returned with the correct parameter is not correct", missingLggsWrong.size(), (lggs.size() - 2));
        // Ensure that the returned list don't content the wrong locale
        assertFalse("Language found : " + param.get(0), missingLggsWrong.contains(param.get(0)));
        assertFalse("Language found : " + param.get(1), missingLggsWrong.contains(param.get(1)));
        assertFalse("Language found : " + param.get(2), missingLggsWrong.contains(param.get(2)));
    }
    
    public void testISOCodeConvertions() throws Exception
    {
        // New ISO code list
        String[] newCode = {"he", "id", "yi"};
        String[] oldCode = {"iw", "in", "ji"};

        Locale loc0 = new Locale(newCode[0]);
        Locale loc1 = new Locale(newCode[1]);
        Locale loc2 = new Locale(newCode[2]);
        
        // Ensure that java.util.Locale has converted the new ISO code into new iso code
        assertEquals("java.util.Locale Convertion not correct for " + newCode[0], oldCode[0], loc0.getLanguage());
        assertEquals("java.util.Locale Convertion not correct for " + newCode[1], oldCode[1], loc1.getLanguage());
        assertEquals("java.util.Locale Convertion not correct for " + newCode[2], oldCode[2], loc2.getLanguage());

        // Ensure that the convertion is correcte
        assertEquals("Convertion of new ISO codes not correct for " + newCode[0], oldCode[0], contentFilterLanguagesService.convertToOldISOCode(newCode[0]));
        assertEquals("Convertion of new ISO codes not correct for " + newCode[1], oldCode[1], contentFilterLanguagesService.convertToOldISOCode(newCode[1]));
        assertEquals("Convertion of new ISO codes not correct for " + newCode[2], oldCode[2], contentFilterLanguagesService.convertToOldISOCode(newCode[2]));

        
        assertEquals("Convertion of old ISO codes not correct for " + oldCode[0], newCode[0], contentFilterLanguagesService.convertToNewISOCode(oldCode[0]));
        assertEquals("Convertion of old ISO codes not correct for " + oldCode[1], newCode[1], contentFilterLanguagesService.convertToNewISOCode(oldCode[1]));
        assertEquals("Convertion of old ISO codes not correct for " + oldCode[2], newCode[2], contentFilterLanguagesService.convertToNewISOCode(oldCode[2]));
    }    
}
