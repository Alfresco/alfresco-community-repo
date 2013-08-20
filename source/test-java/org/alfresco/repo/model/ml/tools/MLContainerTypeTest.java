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
 * @author Yannick Pignot
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
        
        multilingualContentService.makeTranslation(trans1, Locale.FRENCH);
        NodeRef mlContainer = multilingualContentService.getTranslationContainer(trans1);
        multilingualContentService.addTranslation(trans2, trans1, Locale.GERMAN);
        multilingualContentService.addTranslation(trans3, trans1, Locale.ITALIAN);
        empty = multilingualContentService.addEmptyTranslation(trans1, "EMPTY_" + System.currentTimeMillis(), Locale.JAPANESE);
        
        // 1. Locale as null
        
        // Setting a null locale has no effect on any node
        assertFalse("The setting of the locale of a mlContainer must throws an exception", 
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
