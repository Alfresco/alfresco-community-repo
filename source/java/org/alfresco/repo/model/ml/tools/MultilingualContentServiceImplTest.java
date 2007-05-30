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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;

/**
 * @see org.alfresco.repo.ml.MultilingualContentServiceImpl
 * 
 * @author Derek Hulley
 * @author Philippe Dubois
 */
public class MultilingualContentServiceImplTest extends AbstractMultilingualTestCases
{
    
    public void testMakeTranslation() throws Exception
    {
        NodeRef contentNodeRef = createContent();
        // Turn the content into a translation with the appropriate structures
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(contentNodeRef, Locale.CHINESE);
        // Check it
        assertNotNull("Container not created", mlContainerNodeRef);
        // Check the container child count
        assertEquals("Incorrect number of child nodes", 1, nodeService.getChildAssocs(mlContainerNodeRef).size());
    }
    
    public void testAddTranslationUsingContainer() throws Exception
    {
        // Make a container with a single translation
        NodeRef chineseContentNodeRef = createContent();
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        // Create some more content
        NodeRef frenchContentNodeRef = createContent();
        // Make this a translation of the Chinese
        NodeRef newMLContainerNodeRef = multilingualContentService.addTranslation(
                frenchContentNodeRef,
                mlContainerNodeRef,
                Locale.FRENCH);
        // Make sure that the original container was used
        assertEquals("Existing container should have been used", mlContainerNodeRef, newMLContainerNodeRef);
        // Check the container child count
        assertEquals("Incorrect number of child nodes", 2, nodeService.getChildAssocs(mlContainerNodeRef).size());
    }
    
    public void testAddTranslationUsingContent() throws Exception
    {
        // Make a container with a single translation
        NodeRef chineseContentNodeRef = createContent();
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        // Create some more content
        NodeRef frenchContentNodeRef = createContent();
        // Make this a translation of the Chinese
        NodeRef newMLContainerNodeRef = multilingualContentService.addTranslation(
                frenchContentNodeRef,
                chineseContentNodeRef,
                Locale.FRENCH);
        // Make sure that the original container was used
        assertEquals("Existing container should have been used", mlContainerNodeRef, newMLContainerNodeRef);
        // Check the container child count
        assertEquals("Incorrect number of child nodes", 2, nodeService.getChildAssocs(mlContainerNodeRef).size());
    }
    
    @SuppressWarnings("unused") 
    public void testGetMissingTranslation() throws Exception
    {
        List<String> langList = contentFilterLanguagesService.getFilterLanguages();
        int langListSize = langList.size();
        
        // make sure that it exists at least tree language filter
        assertFalse("The testGetMissingTranslation test case needs at least three language", langListSize < 3);
            
        // get the first tree locale of the content filter language list 
        Locale loc1 = I18NUtil.parseLocale(langList.get(0));
        Locale loc2 = I18NUtil.parseLocale(langList.get(1));
        Locale loc3 = I18NUtil.parseLocale(langList.get(2));
        
        // create three content 
        NodeRef nodeRef1 = createContent();
        NodeRef nodeRef2 = createContent();
        NodeRef nodeRef3 = createContent();
        
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(nodeRef1, loc1);
        
        List<Locale> missing = multilingualContentService.getMissingTranslations(mlContainerNodeRef, false); 
        
        // make sure that the missing language list size is correct 
        assertFalse("Missing Translation Size false. " +
                "Real size : " + missing.size() + ". Normal Size " + (langListSize - 1), missing.size() != (langListSize - 1));
        
        // make sure that the missing language list is correct
        assertFalse("Missing Translation List false. Locale " + loc1 + " found", missing.contains(loc1.toString()));
        
        multilingualContentService.addTranslation(nodeRef2, mlContainerNodeRef, loc2);
        multilingualContentService.addTranslation(nodeRef3, mlContainerNodeRef, loc3);
        
        
        missing = multilingualContentService.getMissingTranslations(mlContainerNodeRef, false);
        
        //   make sure that the missing language list size is correct 
        assertFalse("Missing Translation Size false. " +
                "Real size : " + missing.size() + ". Normal Size " + (langListSize - 3), missing.size() != (langListSize - 3));
        
        // make sure that the missing language list is correct
        assertFalse("Missing Translation List false. Locale " + loc2 + " or " + loc3 + " found", missing.contains(loc2.toString()) || missing.contains(loc3.toString()));
    }
    
    public void testGetTranslationForLocale() throws Exception
    {
        NodeRef chineseContentNodeRef = createContent();
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        NodeRef frenchContentNodeRef = createContent();        
        multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
        
        // Get the chinese translation
        assertEquals("Chinese translation should be present",
                chineseContentNodeRef,
                multilingualContentService.getTranslationForLocale(mlContainerNodeRef, Locale.CHINESE));
        // Get the french translation
        assertEquals("French translation should be present",
                frenchContentNodeRef,
                multilingualContentService.getTranslationForLocale(mlContainerNodeRef, Locale.FRENCH));
        // The Italian should return the pivot
        assertEquals("French translation should be present",
                chineseContentNodeRef,
                multilingualContentService.getTranslationForLocale(mlContainerNodeRef, Locale.ITALIAN));
    }
    
    @SuppressWarnings("unused") 
    public void testGetPivotTranslation() throws Exception
    {
        NodeRef chineseContentNodeRef = createContent();
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        
        //  make sure that the pivot language is set
        assertNotNull("Pivot language not set", nodeService.getProperty(mlContainerNodeRef, ContentModel.PROP_LOCALE));
        
        //  make sure that the pivot language is correctly set
        assertEquals("Pivot language not correctly set", Locale.CHINESE, nodeService.getProperty(mlContainerNodeRef, ContentModel.PROP_LOCALE));        
        
        NodeRef frenchContentNodeRef = createContent();        
        multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
        
        //  make sure that the pivot noderef is correct
        assertEquals("Unable to get pivot from container", chineseContentNodeRef, multilingualContentService.getPivotTranslation(mlContainerNodeRef));        
        assertEquals("Unable to get pivot from translation", chineseContentNodeRef, multilingualContentService.getPivotTranslation(frenchContentNodeRef));        
        
        // modify the pivot language 
        nodeService.setProperty(mlContainerNodeRef, ContentModel.PROP_LOCALE, Locale.FRENCH);
        
        //  make sure that the modified pivot noderef is correct
        assertEquals("Pivot node ref not correct", frenchContentNodeRef, multilingualContentService.getPivotTranslation(mlContainerNodeRef));
    }
    
    @SuppressWarnings("unused") 
    public void testCreateEmptyTranslation() throws Exception
    {
        NodeRef chineseContentNodeRef = createContent("Document.txt");
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        
        // This should use the pivot language
        NodeRef emptyNodeRef = multilingualContentService.addEmptyTranslation(mlContainerNodeRef, "Document.txt", Locale.CANADA);
        
        // Ensure that the empty translation is not null
        assertNotNull("The creation of the empty document failed ", emptyNodeRef);
        // Ensure that the empty translation has the mlDocument aspect
        assertTrue("The empty document must have the mlDocument aspect",
                nodeService.hasAspect(emptyNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT));
        // Ensure that the empty translation has the mlEmptyTranslation aspect 
        assertTrue("The empty document must have the mlEmptyTranslation aspect",
                nodeService.hasAspect(emptyNodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION));
        // Check that the auto renaming worked
        String emptyName = DefaultTypeConverter.INSTANCE.convert(String.class,
                nodeService.getProperty(emptyNodeRef, ContentModel.PROP_NAME));
        assertEquals("Empty auto-rename didn't work for same-named document", "Document_en_CA.txt", emptyName);
        
        // Check that the content is identical
        ContentData chineseContentData = fileFolderService.getReader(chineseContentNodeRef).getContentData();
        ContentData emptyContentData = fileFolderService.getReader(emptyNodeRef).getContentData();
    }
    
    public void testCreateEmptyTranslationNames() throws Exception
    {
        NodeRef chineseContentNodeRef = createContent("Document.txt");
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        NodeRef koreanContentNodeRef = createContent("Document_ko.txt");
        multilingualContentService.addTranslation(koreanContentNodeRef, chineseContentNodeRef, Locale.KOREAN);
        // Create with a null name, and off a non-pivot just to be sure
        NodeRef nullNameNodeRef = multilingualContentService.addEmptyTranslation(
                koreanContentNodeRef,
                null,
                Locale.CANADA);
        String nullName = fileFolderService.getFileInfo(nullNameNodeRef).getName();
        assertEquals("Empty translation name not generated correctly.", "Document_en_CA.txt", nullName);
        // Create with the same name
        NodeRef sameNameNodeRef = multilingualContentService.addEmptyTranslation(
                mlContainerNodeRef,
                "Document.txt",
                Locale.CANADA_FRENCH);
        String sameName = fileFolderService.getFileInfo(sameNameNodeRef).getName();
        assertEquals("Empty translation name not generated correctly.", "Document_fr_CA.txt", sameName);
        // Create with a different name
        NodeRef differentNameNodeRef = multilingualContentService.addEmptyTranslation(
                mlContainerNodeRef,
                "Document2.txt",
                Locale.JAPANESE);
        String differentName = fileFolderService.getFileInfo(differentNameNodeRef).getName();
        assertEquals("Empty translation name not generated correctly.", "Document2.txt", differentName);
    }
    
    @SuppressWarnings("unused") 
    public void testCreateEdition() throws Exception
    {
        // Make some content
        NodeRef chineseContentNodeRef = createContent();
        NodeRef frenchContentNodeRef = createContent();
        NodeRef japaneseContentNodeRef = createContent();
        // Add to container
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        multilingualContentService.addTranslation(frenchContentNodeRef, mlContainerNodeRef, Locale.FRENCH);
        multilingualContentService.addTranslation(japaneseContentNodeRef, mlContainerNodeRef, Locale.JAPANESE);
        // Check the container child count
        assertEquals("Incorrect number of child nodes", 3, nodeService.getChildAssocs(mlContainerNodeRef).size());

        // Version each of the documents
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>(3);
        nodeRefs.add(chineseContentNodeRef);
        nodeRefs.add(frenchContentNodeRef);
        nodeRefs.add(japaneseContentNodeRef);
        versionService.createVersion(nodeRefs, null);
        // Get the current versions of each of the documents
        Version chineseVersionPreEdition = versionService.getCurrentVersion(chineseContentNodeRef);
        Version frenchVersionPreEdition = versionService.getCurrentVersion(frenchContentNodeRef);
        Version japaneseVersionPreEdition = versionService.getCurrentVersion(japaneseContentNodeRef);
        
        // Create the edition, keeping the Chinese translation as the basis
        multilingualContentService.createEdition(chineseContentNodeRef);
        // Check the container child count
        assertEquals("Incorrect number of child nodes", 1, nodeService.getChildAssocs(mlContainerNodeRef).size());
        
        // Get the document versions now
        Version chineseVersionPostEdition = versionService.getCurrentVersion(chineseContentNodeRef);
        assertFalse("Expected document to be gone", nodeService.exists(frenchContentNodeRef));
        assertFalse("Expected document to be gone", nodeService.exists(japaneseContentNodeRef));
        
        // Now be sure that we can get the required information using the version service
        VersionHistory mlContainerVersionHistory = versionService.getVersionHistory(mlContainerNodeRef);
        Collection<Version> mlContainerVersions = mlContainerVersionHistory.getAllVersions();
        // Loop through and get all the children of each version
        for (Version mlContainerVersion : mlContainerVersions)
        {
            NodeRef versionedMLContainerNodeRef = mlContainerVersion.getFrozenStateNodeRef();
            // Get all the children
            Map<Locale, NodeRef> translationsByLocale = multilingualContentService.getTranslations(
                    versionedMLContainerNodeRef);
            // Count the children
            int count = translationsByLocale.size();
        }
    }
}
