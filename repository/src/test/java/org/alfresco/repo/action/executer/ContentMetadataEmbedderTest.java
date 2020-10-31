/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
import org.apache.tika.embedder.Embedder;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private final static String ID = GUID.generate();

    @Before
    public void before() throws Exception
    {
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");
        this.dictionaryService = (DictionaryService) this.applicationContext.getBean("dictionaryService");
        this.mimetypeService = (MimetypeService) this.applicationContext.getBean("mimetypeService");
        this.metadataExtracterRegistry  = (MetadataExtracterRegistry) this.applicationContext.getBean("metadataExtracterRegistry");
        metadataExtracterRegistry.setAsyncExtractEnabled(false);
        metadataExtracterRegistry.setAsyncEmbedEnabled(false);

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

    @After
    public void after()
    {
        metadataExtracterRegistry.setAsyncExtractEnabled(true);
        metadataExtracterRegistry.setAsyncEmbedEnabled(true);
    }

    /**
     * Test that a failing embedder does not destroy the original content
     */
    @Test
    public void testFailingEmbedder()
    {
        AbstractMappingMetadataExtracter embedder = new FailingMappingMetadataEmbedder(Arrays.asList(MimetypeMap.MIMETYPE_PDF));
        embedder.setRegistry(metadataExtracterRegistry);
        embedder.setDictionaryService(this.dictionaryService);
        embedder.setMimetypeService(this.mimetypeService);
        embedder.register();
        
        String myCreator = "Embedded creator";

        // Get the old props
        Map<QName, Serializable> props = this.nodeService.getProperties(this.nodeRef);
        props.put(ContentModel.PROP_AUTHOR, myCreator);
        this.nodeService.setProperties(this.nodeRef, props);

        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, SetPropertyValueActionExecuter.NAME, null);
        
        ContentReader origReader = this.contentService.getReader(this.nodeRef, ContentModel.PROP_CONTENT);
        long origSize = origReader.getSize();
        assertTrue(origSize > 0);

        this.executer.execute(action, this.nodeRef);
        
        ContentReader embeddedReader = this.contentService.getReader(this.nodeRef, ContentModel.PROP_CONTENT);
        
        assertEquals("The original content should remain unchanged on embed failures", origSize, embeddedReader.getSize());
    }
    
    /**
     * Embedder which fails upon calling embed on its {@link FailingEmbedder}
     */
    private class FailingMappingMetadataEmbedder extends AbstractMappingMetadataExtracter
    {
        /**
         * Constructor for setting supported extract and embed mimetypes
         * 
         * @param mimetypes the supported extract and embed mimetypes
         */
        public FailingMappingMetadataEmbedder(Collection<String> mimetypes)
        {
            super(
                    new HashSet<String>(mimetypes), 
                    new HashSet<String>(mimetypes));
        }

        @Override
        protected void embedInternal(Map<String, Serializable> metadata, ContentReader reader, ContentWriter writer) throws Throwable
        {
            Embedder embedder = getEmbedder();
            if (embedder == null)
            {
                return;
            }

            Map<String, String> metadataAsStrings = convertMetadataToStrings(metadata);
            Metadata metadataToEmbed = new Metadata();
            metadataAsStrings.forEach((k,v)->metadataToEmbed.add(k, v));

            InputStream inputStream = reader.getContentInputStream();
            OutputStream outputStream = writer.getContentOutputStream();
            embedder.embed(metadataToEmbed, null, outputStream, null);
        }

        protected Embedder getEmbedder()
        {
            return new FailingEmbedder();
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
    
    /**
     * Metadata embedder which fails on a call to embed.
     */
    private class FailingEmbedder implements Embedder
    {
        private static final long serialVersionUID = -4954679684941467571L;

        @Override
        public Set<MediaType> getSupportedEmbedTypes(ParseContext context)
        {
            return null;
        }

        @Override
        public void embed(Metadata metadata, InputStream originalStream, OutputStream outputStream, ParseContext context)
                throws IOException
        {
            throw new IOException("Forced failure");
        }
    }
    
}
