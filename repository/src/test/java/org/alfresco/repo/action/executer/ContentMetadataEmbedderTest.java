/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.action.executer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Test of the ActionExecuter for embedding metadata
 * 
 * @author Ray Gauss II
 */
@Transactional
public class ContentMetadataEmbedderTest extends BaseSpringTest
{

    private NodeService nodeService;
    private ContentService contentService;
    private DictionaryService dictionaryService;
    private MimetypeService mimetypeService;
    private MetadataExtracterRegistry metadataExtracterRegistry;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;

    private ContentMetadataEmbedder executer;
    private long origSize = -1;
    private long newSize = -1;

    private final static String ID = GUID.generate();

    @Before
    public void before() throws Exception
    {
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");
        this.dictionaryService = (DictionaryService) this.applicationContext.getBean("dictionaryService");
        this.mimetypeService = (MimetypeService) this.applicationContext.getBean("mimetypeService");
        this.metadataExtracterRegistry  = (MetadataExtracterRegistry) this.applicationContext.getBean("metadataExtracterRegistry");

        AuthenticationComponent authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();

        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();

        // Setup the content from the PDF test data
        ContentWriter cw = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        cw.setMimetype(MimetypeMap.MIMETYPE_PDF);
        cw.putContent(AbstractContentTransformerTest.loadQuickTestFile("pdf"));

        // Get the executer instance
        this.executer = new ContentMetadataEmbedder();
        this.executer.setNodeService(nodeService);
        this.executer.setContentService(contentService);
        this.executer.setMetadataExtracterRegistry(metadataExtracterRegistry);
        this.executer.setApplicableTypes(new String[] { ContentModel.TYPE_CONTENT.toString() });
    }

    /**
     * Test that a failing embedder does not destroy the original content
     */
    @Test
    public void testFailingEmbedder()
    {
        AbstractMappingMetadataExtracter embedder = new MockFailingEmbedder();
        setupEmbedderActionAndThenExecute(embedder);

        assertEquals("The original content should remain unchanged on embed failures", origSize, newSize);
    }

    /**
     * Test that a successful embedder does change the original content
     */
    @Test
    public void testSuccessfulEmbedder()
    {
        AbstractMappingMetadataExtracter embedder = new MockSuccessfulEmbedder();
        setupEmbedderActionAndThenExecute(embedder);

        assertNotSame("The original content should remain unchanged on embed failures", origSize, newSize);
    }

    private void setupEmbedderActionAndThenExecute(AbstractMappingMetadataExtracter embedder)
    {
        embedder.setRegistry(metadataExtracterRegistry);
        embedder.setDictionaryService(dictionaryService);
        embedder.setMimetypeService(mimetypeService);
        embedder.register();

        String myCreator = "Embedded creator";

        // Get the old props
        Map<QName, Serializable> origProps = nodeService.getProperties(nodeRef);
        origProps.put(ContentModel.PROP_AUTHOR, myCreator);
        nodeService.setProperties(nodeRef, origProps);

        // Create the action
        ActionImpl action = new ActionImpl(null, ID, SetPropertyValueActionExecuter.NAME, null);

        ContentReader origReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        origSize = origReader.getSize();
        assertTrue(origSize > 0);

        // Execute the action
        executer.execute(action, nodeRef);

        ContentReader embeddedReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        newSize = embeddedReader.getSize();
    }

    private static class MockFailingEmbedder extends MockEmbedder
    {
        @Override
        protected void embedInternal(Map<String, Serializable> metadata, ContentReader reader, ContentWriter writer) throws Throwable
        {
            throw new IOException("Forced test failure");
        }
    }

    private static class MockSuccessfulEmbedder extends MockEmbedder
    {
        @Override
        protected void embedInternal(Map<String, Serializable> metadata, ContentReader reader, ContentWriter writer) throws Throwable
        {
            // Just set the content as this is testing embedding in the repo rather than via the AsynchronousExtractor and T-Engines.
            File htmlFile = AbstractContentTransformerTest.loadQuickTestFile("html");
            writer.putContent(htmlFile);
        }
    }

    private static class MockEmbedder extends AbstractMappingMetadataExtracter
    {
        private static final Collection<String> MIMETYPES = Arrays.asList(MimetypeMap.MIMETYPE_PDF);

        public MockEmbedder()
        {
            super(new HashSet<String>(MIMETYPES), new HashSet<String>(MIMETYPES));
        }

        @Override
        protected Map<String, Set<QName>> readMappingProperties(String propertiesUrl)
        {
            return null;
        }
        
        @Override
        protected Map<String, Set<QName>> getDefaultMapping()
        {
            Map<String, Set<QName>> mapping = new HashMap<String, Set<QName>>(1);
            Set<QName> qnames = new HashSet<QName>(1);
            qnames.add(ContentModel.PROP_AUTHOR);
            mapping.put("author", qnames);
            return mapping;
        }

        @Override
        protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
        {
            return null;
        }
    }
}
