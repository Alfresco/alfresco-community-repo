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

import net.sf.acegisecurity.Authentication;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PermissionService;
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
        NodeRef chineseContentNodeRef = createContent();
        // Turn the content into a translation with the appropriate structures
        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        NodeRef mlContainerNodeRef = multilingualContentService.getTranslationContainer(chineseContentNodeRef);
        // Check it
        assertNotNull("Container not created", mlContainerNodeRef);
        // Check the container child count
        assertEquals("Incorrect number of child nodes", 1, nodeService.getChildAssocs(mlContainerNodeRef).size());
    }
    
    public void testAddTranslationUsingContent() throws Exception
    {
        // Make a container with a single translation
        NodeRef chineseContentNodeRef = createContent();
        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        NodeRef mlContainerNodeRef = multilingualContentService.getTranslationContainer(chineseContentNodeRef);
        // Create some more content
        NodeRef frenchContentNodeRef = createContent();
        // Make this a translation of the Chinese
        multilingualContentService.addTranslation(
                frenchContentNodeRef,
                chineseContentNodeRef,
                Locale.FRENCH);
        NodeRef newMLContainerNodeRef = multilingualContentService.getTranslationContainer(frenchContentNodeRef);
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
        
        multilingualContentService.makeTranslation(nodeRef1, loc1);
        
        List<Locale> missing = multilingualContentService.getMissingTranslations(nodeRef1, false); 
        
        // make sure that the missing language list size is correct 
        assertFalse("Missing Translation Size false. " +
                "Real size : " + missing.size() + ". Normal Size " + (langListSize - 1), missing.size() != (langListSize - 1));
        
        // make sure that the missing language list is correct
        assertFalse("Missing Translation List false. Locale " + loc1 + " found", missing.contains(loc1.toString()));
        
        multilingualContentService.addTranslation(nodeRef2, nodeRef1, loc2);
        multilingualContentService.addTranslation(nodeRef3, nodeRef1, loc3);
        
        // Add the missing translations in
        missing = multilingualContentService.getMissingTranslations(nodeRef1, false);
        
        // Make sure that the missing language list size is correct 
        assertFalse("Missing Translation Size false. " +
                "Real size : " + missing.size() + ". Normal Size " + (langListSize - 3), missing.size() != (langListSize - 3));
        
        // make sure that the missing language list is correct
        assertFalse("Missing Translation List false. Locale " + loc2 + " or " + loc3 + " found", missing.contains(loc2.toString()) || missing.contains(loc3.toString()));
    }
    
    public void testGetTranslationForLocale() throws Exception
    {
        NodeRef chineseContentNodeRef = createContent();
        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        NodeRef frenchContentNodeRef = createContent();        
        multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
        
        // Get the chinese translation
        assertEquals("Chinese translation should be present",
                chineseContentNodeRef,
                multilingualContentService.getTranslationForLocale(chineseContentNodeRef, Locale.CHINESE));
        // Get the french translation
        assertEquals("French translation should be present",
                frenchContentNodeRef,
                multilingualContentService.getTranslationForLocale(chineseContentNodeRef, Locale.FRENCH));
        // The Italian should return the pivot
        assertEquals("French translation should be present",
                chineseContentNodeRef,
                multilingualContentService.getTranslationForLocale(chineseContentNodeRef, Locale.ITALIAN));
    }
    
    @SuppressWarnings("unused") 
    public void testGetPivotTranslation() throws Exception
    {
        NodeRef chineseContentNodeRef = createContent();
        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        NodeRef mlContainerNodeRef = multilingualContentService.getTranslationContainer(chineseContentNodeRef);
        
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
        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        
        // This should use the pivot language
        NodeRef emptyNodeRef = multilingualContentService.addEmptyTranslation(chineseContentNodeRef, "Document.txt", Locale.CANADA);
        
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
        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
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
                chineseContentNodeRef,
                "Document.txt",
                Locale.CANADA_FRENCH);
        String sameName = fileFolderService.getFileInfo(sameNameNodeRef).getName();
        assertEquals("Empty translation name not generated correctly.", "Document_fr_CA.txt", sameName);
        // Create with a different name
        NodeRef differentNameNodeRef = multilingualContentService.addEmptyTranslation(
                chineseContentNodeRef,
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
        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
        multilingualContentService.addTranslation(japaneseContentNodeRef, chineseContentNodeRef, Locale.JAPANESE);

        NodeRef mlContainerNodeRef = multilingualContentService.getTranslationContainer(chineseContentNodeRef);
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
    
    public void testGetTranslationContainerPermissions() throws Exception
    {
        // Grant the guest user rights to our working folder
        PermissionService permissionService = serviceRegistry.getPermissionService();
        AuthenticationComponent authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        permissionService.setPermission(
                folderNodeRef,
                PermissionService.GUEST_AUTHORITY,
                PermissionService.ALL_PERMISSIONS,
                true);
        // Get the current authentication
        Authentication authentication = authenticationComponent.getCurrentAuthentication();
        try
        {
            authenticationComponent.setGuestUserAsCurrentUser();
            // Create some documents
            NodeRef chineseContentNodeRef = createContent();
            // Make a translation
            multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
            multilingualContentService.getTranslationContainer(chineseContentNodeRef);
        }
        finally
        {
            try { authenticationComponent.setCurrentAuthentication(authentication); } catch (Throwable e) {}
        }
    }
    
    /**
     * Check whether non-admin users can take part in ML document manipulation
     */
    public void testPermissions() throws Exception
    {
        // Grant the guest user rights to our working folder
        PermissionService permissionService = serviceRegistry.getPermissionService();
        AuthenticationComponent authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        permissionService.setPermission(
                folderNodeRef,
                PermissionService.GUEST_AUTHORITY,
                PermissionService.ALL_PERMISSIONS,
                true);
        // Get the current authentication
        Authentication authentication = authenticationComponent.getCurrentAuthentication();
        try
        {
            authenticationComponent.setGuestUserAsCurrentUser();
            // Create some documents
            NodeRef chineseContentNodeRef = createContent();
            NodeRef frenchContentNodeRef = createContent();
            // Do ML work
            multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
            multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
            multilingualContentService.addEmptyTranslation(chineseContentNodeRef, null, Locale.JAPANESE);
            multilingualContentService.createEdition(chineseContentNodeRef);
        }
        finally
        {
            try { authenticationComponent.setCurrentAuthentication(authentication); } catch (Throwable e) {}
        }
    }
}
