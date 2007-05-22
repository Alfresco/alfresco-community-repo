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
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Empty translations aspect test cases
 * 
 * @see org.alfresco.service.cmr.ml.EmptyTranslationAspect
 * 
 * @author yanipig
 */
public class EmptyTranslationAspectTest extends AbstractMultilingualTestCases {

    protected ContentService contentService;
    
    protected void setUp() throws Exception
    {
        contentService     = (ContentService)     ctx.getBean("ContentService");
        super.setUp();    
    }
    
    public void testCopy() throws Exception
    {
        NodeRef pivot = createContent();
        NodeRef empty = null;
        
        multilingualContentService.makeTranslation(pivot, Locale.FRENCH);
        
        empty = multilingualContentService.addEmptyTranslation(pivot, "empty_" + System.currentTimeMillis(), Locale.CHINESE);

        boolean exceptionCatched = false;
        NodeRef copy = null;
        
        try 
        {
            copy = fileFolderService.copy(
                    empty, 
                    nodeService.getPrimaryParent(empty).getParentRef(), 
                    "copyOfEmpty" + System.currentTimeMillis()).getNodeRef();    

            // test failed
        } 
        catch (Exception ignore) 
        {
            exceptionCatched = true;
        }
        
        // Ensure that the copy of an empty translation throws an exception 
        assertTrue("The copy of a translation must throws an exception", exceptionCatched);
        // Ensure that the copy node is null 
        assertNull("The copy must fail ", copy);        
    }
    
    public void testDeleteNode() throws Exception
    {
        NodeRef pivot = createContent();
        NodeRef empty = null;
        
        multilingualContentService.makeTranslation(pivot, Locale.FRENCH);
        
        empty = multilingualContentService.addEmptyTranslation(pivot, "empty_" + System.currentTimeMillis(), Locale.CHINESE);
        
        nodeService.deleteNode(empty);
        
        // Ensure that the empty translation is removed from the workspace
        assertFalse("The empty translation must be removed from the wokspace", nodeService.exists(empty));
        // Ensure that the empty translation is not archived
        assertFalse("The empty translation must be removed from the wokspace", nodeService.exists(nodeArchiveService.getArchivedNode(empty)));        
    }
    
    public void testGetContent() throws Exception
    {
        NodeRef pivot = createContent();
        NodeRef otherTranslation = createContent();
        
        NodeRef empty = null;
        
        NodeRef mlContainer = multilingualContentService.makeTranslation(pivot, Locale.FRENCH);
        multilingualContentService.addTranslation(otherTranslation, pivot, Locale.KOREAN);
        
        empty = multilingualContentService.addEmptyTranslation(pivot, "empty_" + System.currentTimeMillis(), Locale.CHINESE);

        // Modify the content of the pivot
        String contentString = fileFolderService.getReader(pivot).getContentString();
        contentString += "_TEST_"; 
        
        fileFolderService.getWriter(pivot).putContent(contentString);
        
        // Ensure that the content retourned by a get reader of the empty document is the same as the pivot
        assertEquals("The content retourned of the empty translation must be the same that the content of the pivot",
                fileFolderService.getReader(pivot).getContentString(), 
                fileFolderService.getReader(empty).getContentString());
        // Ensure that the content retourned by a get reader of the empty document is different to the non-pivot translation
        assertNotSame("The content retourned of the empty translation must be different that the content of a non-pivot translation",
                fileFolderService.getReader(otherTranslation).getContentString(), 
                fileFolderService.getReader(empty).getContentString());

        
        // modify the pivot 
        Map<QName, Serializable> props = nodeService.getProperties(mlContainer);
        props.put(ContentModel.PROP_LOCALE, Locale.KOREAN);
        nodeService.setProperties(mlContainer, props);
        
        // Ensure that the modicfication of the pivot is take in account
        assertEquals("The modification of the pivot is not take in account",
                fileFolderService.getReader(otherTranslation).getContentString(), 
                fileFolderService.getReader(empty).getContentString());
    }
    
    public void testUpdateContent() throws Exception
    {
        NodeRef pivot = createContent();
        NodeRef empty = null;
        
        multilingualContentService.makeTranslation(pivot, Locale.FRENCH);
                
        empty = multilingualContentService.addEmptyTranslation(pivot, "empty_" + System.currentTimeMillis(), Locale.CHINESE);

        // update the empty translation content
        ContentWriter writer = contentService.getWriter(empty, ContentModel.PROP_CONTENT, true);
        writer.setMimetype("text/plain");
        writer.putContent("ANY_CONTENT");  
        
        // Ensure that the URL property of the empty translation content is not null        
        assertNotNull("The url of an updated (ex)empty transation can't be null", ((ContentData) nodeService.getProperty(empty, ContentModel.PROP_CONTENT)).getContentUrl());        
        // Ensure that the mlEmptyTranslation aspect is removed
        assertFalse("The " + ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION + " aspect of an updated (ex)empty translation must be removed", nodeService.hasAspect(empty, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION));        
        // Ensure that the mlDocument aspect is not removed
        assertTrue("The " + ContentModel.ASPECT_MULTILINGUAL_DOCUMENT + " aspect of an updated (ex)empty translation must be keeped", nodeService.hasAspect(empty, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT));
        // Ensure that the content is written
        assertEquals("The content of the (ex)empty translation is not correct", 
                fileFolderService.getReader(empty).getContentString(), 
                "ANY_CONTENT");
        // Ensure that the content is different that the content of the pivot
        assertNotSame("The content of the (ex)empty translation is not updated and is the same as the content of the pivot", 
                fileFolderService.getReader(empty).getContentString(), 
                fileFolderService.getReader(pivot).getContentString());        
    }
}
