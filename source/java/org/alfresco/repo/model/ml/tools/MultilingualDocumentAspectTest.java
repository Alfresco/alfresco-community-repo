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
 * Multilingual document aspect test cases
 *
 * @see org.alfresco.service.cmr.ml.MultilingualDocumentAspect
 *
 * @author Yannick Pignot
 */
public class MultilingualDocumentAspectTest extends AbstractMultilingualTestCases
{
    public void testCopy() throws Exception
    {
        NodeRef original = createContent();
        multilingualContentService.makeTranslation(original, Locale.FRENCH);
        NodeRef mlContainer = multilingualContentService.getTranslationContainer(original);

        NodeRef copy =
                fileFolderService.copy(original, nodeService.getPrimaryParent(original).getParentRef(), "COPY" + System.currentTimeMillis()).getNodeRef();

        // Ensure that the copy removes the mlDocument aspect
        assertFalse("The copy of a mlDocument can't have the multilingual aspect", nodeService.hasAspect(copy, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT));

        // Ensure that the copy removes the association between the mlConatiner and the new node
        assertEquals("The copy of a mlDocument can't be a children of the mlContainer", 1, multilingualContentService.getTranslations(mlContainer).size());
    }

    public void testDeleteNode() throws Exception
    {
        NodeRef trad1 = createContent();
        NodeRef trad2 = createContent();
        NodeRef trad3 = createContent();

        NodeRef parent = nodeService.getPrimaryParent(trad1).getParentRef();

        multilingualContentService.makeTranslation(trad1, Locale.FRENCH);
        multilingualContentService.addTranslation(trad2, trad1, Locale.GERMAN);
        multilingualContentService.addTranslation(trad3, trad1, Locale.ITALIAN);

        nodeService.deleteNode(trad3);

        // Ensure that the deleted node is romoved from its space
        assertEquals("The deleted node must be removed to the space", 2, nodeService.getChildAssocs(parent).size());
        // Ensure that the mlContainer doesn't keep an association to the deleted node
        assertEquals("The deleted node must be removed to the child associations of the mlContainer", 2, multilingualContentService.getTranslations(trad1).size());

        // retore the deleted node
        NodeRef restoredNode = nodeArchiveService.restoreArchivedNode(nodeArchiveService.getArchivedNode(trad3)).getRestoredNodeRef();

        // Ensure that the restored node is restored to it s original space
        assertEquals("The restored node must be restaured to the the space", 3, nodeService.getChildAssocs(parent).size());
        // Ensure that the restored node is not linked to the mlContainer
        assertEquals("The restored node would not be restaured to the mlContainer", 2, multilingualContentService.getTranslations(trad1).size());
        // Ensure that the restored node doesn't keep the mlDocument aspect
        assertFalse("The restored node can't keep the multilingual aspect", nodeService.hasAspect(restoredNode, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT));
    }

    public void testDeletePivot() throws Exception
    {
        NodeRef pivot  = createContent();
        NodeRef trans1 = createContent();
        multilingualContentService.makeTranslation(pivot, Locale.FRENCH);
        NodeRef mlContainer = multilingualContentService.getTranslationContainer(pivot);
        multilingualContentService.addTranslation(trans1, pivot, Locale.KOREAN);

        //nodeService.deleteNode(trans1);
        nodeService.deleteNode(pivot);

        // Ensure that pivot is removed
        assertFalse("The pivot would be removed", nodeService.exists(pivot));
        // Ensure that the mlContainer is removed
        assertFalse("The mlContainer must be removed if the pivot is removed", nodeService.exists(mlContainer));
        // Ensure that trans1 is NOT removed
        assertTrue("The last translation would not be removed", nodeService.exists(trans1));
        // Ensure that trans1 has no mlDocument aspect
        assertFalse("The last translation can't keep the multilingual aspect", nodeService.hasAspect(trans1, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT));
    }

    public void testDeleteLastNode() throws Exception
    {
        NodeRef pivot  = createContent();
        multilingualContentService.makeTranslation(pivot, Locale.FRENCH);
        NodeRef mlContainer = multilingualContentService.getTranslationContainer(pivot);

        nodeService.deleteNode(pivot);

        // Ensure that the mlContainer is removed too
        assertFalse("The mlContainer must be removed if the last translation is removed", nodeService.exists(mlContainer));

    }

    public void testRemoveAspect() throws Exception
    {
        // entierly covered by the delete tests
    }

    public void testUpdateLocale() throws Exception
    {
        NodeRef pivot  = createContent();
        NodeRef trans1 = createContent();
        multilingualContentService.makeTranslation(pivot, Locale.FRENCH);
        NodeRef mlContainer = multilingualContentService.getTranslationContainer(pivot);
        multilingualContentService.addTranslation(trans1, pivot, Locale.KOREAN);

        // modify the locale for the translation
        Map<QName, Serializable> props = nodeService.getProperties(trans1);
        props.put(ContentModel.PROP_LOCALE, Locale.GERMAN);
        nodeService.setProperties(trans1, props);

        // Ensure that the pivot reference is not changed for the mlContainer and the locale is changed for the translation
        assertEquals("The locale for the pivot would be changed ",Locale.GERMAN, nodeService.getProperty(trans1, ContentModel.PROP_LOCALE));
        assertEquals("The pivot reference would not be changed in the mlContainer", Locale.FRENCH, nodeService.getProperty(mlContainer, ContentModel.PROP_LOCALE));

        // modify the locale for the pivot
        props = nodeService.getProperties(pivot);
        props.put(ContentModel.PROP_LOCALE, Locale.US);
        nodeService.setProperties(pivot, props);

        // Ensure that the pivot reference is changed (in the pivot and in the mlContainer)
        assertEquals("The locale for the pivot would be changed ", Locale.US, nodeService.getProperty(pivot, ContentModel.PROP_LOCALE));
        assertEquals("The pivot reference would be changes in the mlContainer", Locale.US, nodeService.getProperty(mlContainer, ContentModel.PROP_LOCALE));
    }

    public void testUpdateRedundantLocale() throws Exception
    {
        NodeRef pivot  = createContent();
        NodeRef trans1 = createContent();
        NodeRef trans2 = createContent();

        multilingualContentService.makeTranslation(pivot, Locale.FRENCH);
        multilingualContentService.addTranslation(trans1, pivot, Locale.KOREAN);
        multilingualContentService.addTranslation(trans2, pivot, Locale.JAPANESE);

        // 1. Try with redundant locale

        // modify the locale for the translation 2
        Map<QName, Serializable> props = nodeService.getProperties(trans2);
        props.put(ContentModel.PROP_LOCALE, Locale.KOREAN);

        boolean exceptionCatched = false;

        try
        {
            nodeService.setProperties(trans2, props);
            // test failed
        } catch (Exception ignore)
        {
            exceptionCatched = true;
        }

        // Ensure that the the exception was catched.
        assertTrue("The modification of this locale must catch an exception because it is already in use in another translation", exceptionCatched);
        // Ensure that the locale of the trans2 is unchanged
        assertEquals("The locale must not be changed",
                Locale.JAPANESE,
                (Locale) nodeService.getProperty(trans2, ContentModel.PROP_LOCALE));

        // 2. Try with a non-redundant locale

        props = nodeService.getProperties(trans2);
        props.put(ContentModel.PROP_LOCALE, Locale.ITALIAN);

        exceptionCatched = false;

        try
        {
            nodeService.setProperties(trans2, props);

        } catch (Exception ignore)
        {
            // test failed
            exceptionCatched = true;
        }

        // Ensure that the exception was not catched
        assertFalse("The modification of the locale would not throws an exception", exceptionCatched);
        // Ensure that the locale is modified
        assertEquals("The locale must be changed",
                Locale.ITALIAN,
                (Locale) nodeService.getProperty(trans2, ContentModel.PROP_LOCALE));
    }
}

