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
        }
        finally
        {
            try { authenticationComponent.setCurrentAuthentication(authentication); } catch (Throwable e) {}
        }
    }

    public void testDeleteMultilingualContent() throws Exception
    {
        NodeRef chineseContentNodeRef = createContent();
        NodeRef frenchContentNodeRef = createContent();
        NodeRef japaneseContentNodeRef = createContent();
        NodeRef emptyGermanContentNodeRef = null;

        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
        multilingualContentService.addTranslation(japaneseContentNodeRef, chineseContentNodeRef, Locale.JAPANESE);
        emptyGermanContentNodeRef = multilingualContentService.addEmptyTranslation(chineseContentNodeRef, null, Locale.GERMAN);

        // the mlContainer to remove
        NodeRef mlContainerNodeRef = multilingualContentService.getTranslationContainer(chineseContentNodeRef);

        // Ensure that the the mlContainer is correctly created
        assertEquals("Incorrect number of translations", 4, multilingualContentService.getTranslations(mlContainerNodeRef).size());

        // remove the mlContainer
        multilingualContentService.deleteTranslationContainer(mlContainerNodeRef);

        // get the archived node ref
        NodeRef archivedChineseContentNodeRef = nodeArchiveService.getArchivedNode(chineseContentNodeRef);
        NodeRef archivedFrenchContentNodeRef = nodeArchiveService.getArchivedNode(frenchContentNodeRef);
        NodeRef archivedJapaneseContentNodeRef = nodeArchiveService.getArchivedNode(japaneseContentNodeRef);
        NodeRef archivedEmptyGermanContentNodeRef = nodeArchiveService.getArchivedNode(emptyGermanContentNodeRef);
        NodeRef archivedMlContainerNodeRef = nodeArchiveService.getArchivedNode(mlContainerNodeRef);

        // Ensure that the mlContainer is removed
        assertFalse("The multilingual container must be removed", nodeService.exists(mlContainerNodeRef));
        // Ensure that the mlContainer IS NOT archived
        assertFalse("The multilingual container can't be archived", nodeService.exists(archivedMlContainerNodeRef));
        // Ensure that the translations are removed
        assertFalse("The translation must be removed: " + Locale.CHINESE, nodeService.exists(chineseContentNodeRef));
        assertFalse("The translation must be removed: " + Locale.JAPANESE, nodeService.exists(japaneseContentNodeRef));
        assertFalse("The translation must be removed: " + Locale.FRENCH, nodeService.exists(frenchContentNodeRef));
        assertFalse("The empty translation must be removed: " + Locale.GERMAN, nodeService.exists(emptyGermanContentNodeRef));

        // Ensure that the translations ARE archived
        assertTrue("The translation must be archived: " + Locale.CHINESE, nodeService.exists(archivedChineseContentNodeRef));
        assertTrue("The translation must be archived: " + Locale.JAPANESE, nodeService.exists(archivedJapaneseContentNodeRef));
        assertTrue("The translation must be archived: " + Locale.FRENCH, nodeService.exists(archivedFrenchContentNodeRef));

        // Ensure that the empty translation IS NOT archived
        assertFalse("The empty document can't be archived: " + Locale.GERMAN, nodeService.exists(archivedEmptyGermanContentNodeRef));

        // Ensure that the mlDocument aspect is removed
        assertFalse("The " + ContentModel.ASPECT_MULTILINGUAL_DOCUMENT +  " aspect must be removed for " + Locale.CHINESE, nodeService.hasAspect(archivedChineseContentNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT));
        assertFalse("The " + ContentModel.ASPECT_MULTILINGUAL_DOCUMENT +  " aspect must be removed for " + Locale.JAPANESE, nodeService.hasAspect(archivedJapaneseContentNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT));
        assertFalse("The " + ContentModel.ASPECT_MULTILINGUAL_DOCUMENT +  " aspect must be removed for " + Locale.FRENCH, nodeService.hasAspect(archivedFrenchContentNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT));

    }

    @SuppressWarnings("unused")
    public void testCopyMLContainerInNewSpace() throws Exception
    {
        NodeRef chineseContentNodeRef = createContent();
        NodeRef frenchContentNodeRef = createContent();
        NodeRef japaneseContentNodeRef = createContent();
        NodeRef emptyGermanContentNodeRef = null;

        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
        multilingualContentService.addTranslation(japaneseContentNodeRef, chineseContentNodeRef, Locale.JAPANESE);
        emptyGermanContentNodeRef = multilingualContentService.addEmptyTranslation(chineseContentNodeRef, null, Locale.GERMAN);

        // the mlContainer to copy
        NodeRef mlContainerNodeRef = multilingualContentService.getTranslationContainer(chineseContentNodeRef);

        // Ensure that the the mlContainer is correctly created
        assertEquals("Incorrect number of translations", 4, multilingualContentService.getTranslations(mlContainerNodeRef).size());

        // get the actual space
        NodeRef actualSpace = folderNodeRef;
        // create a new space
        NodeRef destinationSpace = fileFolderService.create(folderNodeRef, "testCopyMLContainerInNewSpace" + System.currentTimeMillis(), ContentModel.TYPE_FOLDER).getNodeRef();

        // Ensure that the new space is created
        assertTrue("The destiation space is not created " + destinationSpace, nodeService.exists(destinationSpace));

        // copy the mlContainer
        NodeRef newMLContainer = multilingualContentService.copyTranslationContainer(mlContainerNodeRef, destinationSpace, "");

        assertEquals("Incorrect number of translations for the new mlContainer", 4, multilingualContentService.getTranslations(newMLContainer).size());

        // Ensure that a new mlContainer is created
        assertTrue("The new mlContainer is not created ", nodeService.exists(newMLContainer));
        // Ensure that the newMLContainer is a copy of the source mlContainer
        assertFalse("The newMLContainer is not a copy of the source mlContainer, the ref is the same " + newMLContainer , newMLContainer.equals(mlContainerNodeRef));
        assertEquals("The newMLContainer is not a copy of the source mlContainer, the locales are not the same " + newMLContainer ,
                        nodeService.getProperty(mlContainerNodeRef, ContentModel.PROP_LOCALE),
                        nodeService.getProperty(newMLContainer, ContentModel.PROP_LOCALE));
        assertEquals("The newMLContainer is not a copy of the source mlContainer, the authors are not the same " + newMLContainer ,
                nodeService.getProperty(mlContainerNodeRef, ContentModel.PROP_AUTHOR),
                nodeService.getProperty(newMLContainer, ContentModel.PROP_AUTHOR));

        // get the source translations
        Map<Locale, NodeRef> sourceTranslations = multilingualContentService.getTranslations(mlContainerNodeRef);
        // get the copies
        Map<Locale, NodeRef> copyTranslations = multilingualContentService.getTranslations(newMLContainer);

        // Ensure that the translations are copies from the source translations
        assertEquals("They are not the same number of translation in the source mlContainer and in its copy", sourceTranslations.size(), copyTranslations.size());

        for(Map.Entry<Locale, NodeRef> entry : sourceTranslations.entrySet())
        {
            Locale locale = entry.getKey();

            NodeRef sourceNodeRef = entry.getValue();
            NodeRef sourceParent = nodeService.getPrimaryParent(sourceNodeRef).getParentRef();

            NodeRef copyTranslation = multilingualContentService.getTranslationForLocale(newMLContainer, locale);
            NodeRef copyParent = nodeService.getPrimaryParent(copyTranslation).getParentRef();

            // Ensure that the copy exists
            assertNotNull("No copy found for the locale " + locale, copyTranslation);
            assertTrue("No copy exists for the locale " + locale, nodeService.exists(copyTranslation));

            // Ensure that the copy has the mlDocument aspect
            assertTrue("The copy must have the mlDocument aspect",
                            nodeService.hasAspect(copyTranslation, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT));

            // Ensure that the copy is an empty translation if the source too
            assertEquals("The call of nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION) must return the same result for the source and the copy",
                            nodeService.hasAspect(sourceNodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION),
                            nodeService.hasAspect(copyTranslation, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION));


            // Ensure that the copy and the source are different
            assertNotSame("The copy has the same ref as the source", sourceNodeRef, copyTranslation);

            // Ensure that the parent of the source is correct
            assertEquals("The source would not be moved", sourceParent, actualSpace);
            // Ensure that the parent of the copy is correct
            assertEquals("The copy is not in the right space", copyParent, destinationSpace);
        }
    }

    @SuppressWarnings("unused")
    public void testCopyMLContainerInSameSpace() throws Exception
    {
        NodeRef chineseContentNodeRef = createContent();
        NodeRef frenchContentNodeRef = createContent();
        NodeRef japaneseContentNodeRef = createContent();
        NodeRef emptyGermanContentNodeRef = null;

        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
        multilingualContentService.addTranslation(japaneseContentNodeRef, chineseContentNodeRef, Locale.JAPANESE);
        emptyGermanContentNodeRef = multilingualContentService.addEmptyTranslation(chineseContentNodeRef, null, Locale.GERMAN);

        // the mlContainer to copy
        NodeRef mlContainerNodeRef = multilingualContentService.getTranslationContainer(chineseContentNodeRef);

        // Ensure that the the mlContainer is correctly created
        assertEquals("Incorrect number of translations", 4, multilingualContentService.getTranslations(mlContainerNodeRef).size());

        // get the actual space
        NodeRef actualSpace = folderNodeRef;

        try
        {
            // copy the mlContainer
            NodeRef newMLContainer = multilingualContentService.copyTranslationContainer(mlContainerNodeRef, actualSpace, "");

            fail("The copy of the mlContainer in the same space would faile");
        }
        catch(Exception e)
        {
            // test asserted
        }
    }

    @SuppressWarnings("unused")
    public void testCopyAndRenameMLContainer() throws Exception
    {
        NodeRef chineseContentNodeRef = createContent();
        NodeRef frenchContentNodeRef = createContent();
        NodeRef japaneseContentNodeRef = createContent();
        NodeRef emptyGermanContentNodeRef = null;

        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
        multilingualContentService.addTranslation(japaneseContentNodeRef, chineseContentNodeRef, Locale.JAPANESE);
        emptyGermanContentNodeRef = multilingualContentService.addEmptyTranslation(chineseContentNodeRef, null, Locale.GERMAN);

        // the mlContainer to copy
        NodeRef mlContainerNodeRef = multilingualContentService.getTranslationContainer(chineseContentNodeRef);

        // Ensure that the the mlContainer is correctly created
        assertEquals("Incorrect number of translations", 4, multilingualContentService.getTranslations(mlContainerNodeRef).size());

        // get the actual space
        NodeRef actualSpace = folderNodeRef;

        // create a new space
        NodeRef destinationSpace = fileFolderService.create(folderNodeRef, "testCopyMLContainerInNewSpace" + System.currentTimeMillis(), ContentModel.TYPE_FOLDER).getNodeRef();

        // Ensure that the new space is created
        assertTrue("The destiation space is not created " + destinationSpace, nodeService.exists(destinationSpace));

        String PREFIX = "COPY OF " ;

        NodeRef newMLContainer = multilingualContentService.copyTranslationContainer(mlContainerNodeRef, destinationSpace, PREFIX);

        // Ensure that a new mlContainer is created
        assertTrue("The new mlContainer is not created ", nodeService.exists(newMLContainer));
        // Ensure that the newMLContainer is a copy of the source mlContainer
        assertFalse("The newMLContainer is not a copy of the source mlContainer, the ref is the same " + newMLContainer , newMLContainer.equals(mlContainerNodeRef));

        // get the source translations
        Map<Locale, NodeRef> sourceTranslations = multilingualContentService.getTranslations(mlContainerNodeRef);
        // get the copies
        Map<Locale, NodeRef> copyTranslations = multilingualContentService.getTranslations(newMLContainer);

        // Ensure that the translations are copies from the source translations
        assertEquals("They are not the same number of translation in the source mlContainer and in its copy", sourceTranslations.size(), copyTranslations.size());

        for(Map.Entry<Locale, NodeRef> entry : sourceTranslations.entrySet())
        {
            Locale locale = entry.getKey();

            NodeRef sourceNodeRef = entry.getValue();
            NodeRef copyNodeRef = multilingualContentService.getTranslationForLocale(newMLContainer, locale);

            String sourceName = (String) nodeService.getProperty(sourceNodeRef, ContentModel.PROP_NAME);
            String copyName = (String) nodeService.getProperty(copyNodeRef, ContentModel.PROP_NAME);

            String theoricalCopyName = PREFIX + sourceName;

            // Ensure that the name of the copy is correct
            assertTrue("The name of the copied translation is incorect: " + copyName + " and should be " + theoricalCopyName, theoricalCopyName.equals(copyName));
        }

    }

    @SuppressWarnings("unused")
    public void testMoveMLContainer() throws Exception
    {
        NodeRef chineseContentNodeRef = createContent();
        NodeRef frenchContentNodeRef = createContent();
        NodeRef japaneseContentNodeRef = createContent();
        NodeRef emptyGermanContentNodeRef = null;

        multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        multilingualContentService.addTranslation(frenchContentNodeRef, chineseContentNodeRef, Locale.FRENCH);
        multilingualContentService.addTranslation(japaneseContentNodeRef, chineseContentNodeRef, Locale.JAPANESE);
        emptyGermanContentNodeRef = multilingualContentService.addEmptyTranslation(chineseContentNodeRef, null, Locale.GERMAN);

        // the mlContainer to copy
        NodeRef mlContainerNodeRef = multilingualContentService.getTranslationContainer(chineseContentNodeRef);

        // Ensure that the the mlContainer is correctly created
        assertEquals("Incorrect number of translations", 4, multilingualContentService.getTranslations(mlContainerNodeRef).size());

        // get the actual space
        NodeRef actualSpace = folderNodeRef;
        // create a new space
        NodeRef destinationSpace = fileFolderService.create(folderNodeRef, "testCopyMLContainerInNewSpace", ContentModel.TYPE_FOLDER).getNodeRef();

        // Ensure that the new space is created
        assertTrue("The destiation space is not created " + destinationSpace, nodeService.exists(destinationSpace));

        // move the mlContainer
        multilingualContentService.moveTranslationContainer(mlContainerNodeRef, destinationSpace);

        // Esure that the nodes are moved
        assertEquals("The node should be moved", destinationSpace, nodeService.getPrimaryParent(chineseContentNodeRef).getParentRef());
        assertEquals("The node should be moved", destinationSpace, nodeService.getPrimaryParent(frenchContentNodeRef).getParentRef());
        assertEquals("The node should be moved", destinationSpace, nodeService.getPrimaryParent(japaneseContentNodeRef).getParentRef());
        assertEquals("The node should be moved", destinationSpace, nodeService.getPrimaryParent(emptyGermanContentNodeRef).getParentRef());

        // Ensure the mlContainer is not changed
        assertEquals("The mlContainer should not be changed", mlContainerNodeRef, multilingualContentService.getTranslationContainer(chineseContentNodeRef));
        assertEquals("The mlContainer should not be changed", mlContainerNodeRef, multilingualContentService.getTranslationContainer(frenchContentNodeRef));
        assertEquals("The mlContainer should not be changed", mlContainerNodeRef, multilingualContentService.getTranslationContainer(japaneseContentNodeRef));
        assertEquals("The mlContainer should not be changed", mlContainerNodeRef, multilingualContentService.getTranslationContainer(emptyGermanContentNodeRef));

    }
}
