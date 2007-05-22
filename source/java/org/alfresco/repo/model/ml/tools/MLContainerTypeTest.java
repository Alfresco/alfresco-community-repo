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
package org.alfresco.repo.model.ml.tools;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Multilingual container type test cases
 * 
 * @see org.alfresco.service.cmr.ml.MLContainerType
 * 
 * @author yanipig
 */
public class MLContainerTypeTest extends AbstractMultilingualTestCases
{
    @SuppressWarnings("unused")
    public void testEditLocale() throws Exception
    {
        NodeRef trans1 = createContent();
        NodeRef trans2 = createContent();
        NodeRef trans3 = createContent();
        NodeRef empty  = null;
        
        NodeRef mlContainer = multilingualContentService.makeTranslation(trans1, Locale.FRENCH);
        multilingualContentService.addTranslation(trans2, trans1, Locale.GERMAN);
        multilingualContentService.addTranslation(trans3, trans1, Locale.ITALIAN);
        empty = multilingualContentService.addEmptyTranslation(trans1, "EMPTY_" + System.currentTimeMillis(), Locale.JAPANESE);
        
        // 1. Locale as null
        
        // Ensure that the setting of the locale of the mlContainer as null throws an excpetion
        assertTrue("The setting of the locale of a mlContainer must throws an exception", 
                setLocaleProp(mlContainer, null));
        // Ensure that the locale of the mlContainer is not changed
        assertEquals("The locale of the mlContainer would not be changed", 
                Locale.FRENCH,
                nodeService.getProperty(mlContainer, ContentModel.PROP_LOCALE));
        
        // 2. Set an non-existing locale
        
        // Ensure that the setting of the locale of the mlContainer as a non-existing translation language throws an excpetion
        assertTrue("The setting of the locale of a mlContainer as a non-existing translation language must throws an exception", 
                setLocaleProp(mlContainer, Locale.US));
        // Ensure that the locale of the mlContainer is not changed
        assertEquals("The locale of the mlContainer would not be changed", 
                Locale.FRENCH,
                nodeService.getProperty(mlContainer, ContentModel.PROP_LOCALE));
        
        // 3. Set the locale of a empty translation
        
        //    Ensure that the setting of the locale of the mlContainer as an empty translation language throws an excpetion
        assertTrue("The setting of the locale of a mlContainer as an empty translation language must throws an exception", 
                setLocaleProp(mlContainer, Locale.JAPANESE));
        // Ensure that the locale of the mlContainer is not changed
        assertEquals("The locale of the mlContainer would not be changed", 
                Locale.FRENCH,
                nodeService.getProperty(mlContainer, ContentModel.PROP_LOCALE));
        
        // 4. Set an existing and valid locale
        
        // Ensure that the setting of the locale of the mlContainer as an existing and a non-empty translation DOESN'T throw an excpetion 
        assertFalse("The setting of the locale of a mlContainer as an existing and a non-empty translation DOESN'T throw an excpetion", 
                setLocaleProp(mlContainer, Locale.ITALIAN));
        // Ensure that the locale of the mlContainer is not changed
        assertEquals("The locale of the mlContainer would be changed", 
                Locale.ITALIAN,
                nodeService.getProperty(mlContainer, ContentModel.PROP_LOCALE));
        
    }
    
    
    
    
    private boolean setLocaleProp(NodeRef node, Locale locale) throws Exception
    {
        Map<QName, Serializable> props = nodeService.getProperties(node);
        props.put(ContentModel.PROP_LOCALE, locale);
        
        boolean exceptionCatched = false;
        
        try 
        {
            nodeService.setProperties(node, props);
            
        } 
        catch (IllegalArgumentException ignore) 
        {
            exceptionCatched = true;
        }
        catch(Exception ex)
        {
            throw new Exception(ex);
        }

        
        return exceptionCatched;
    }
}
