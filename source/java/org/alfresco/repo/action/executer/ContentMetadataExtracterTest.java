/*
 * Copyright (C) 2005 Jesper Steen M�ller
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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Test of the ActionExecuter for extracting metadata. Note: This test makes
 * assumptions about the PDF test data for PdfBoxExtracter.
 * 
 * @author Jesper Steen Møller
 */
public class ContentMetadataExtracterTest extends BaseSpringTest
{
    protected static final String QUICK_TITLE = "The quick brown fox jumps over the lazy dog";
    protected static final String QUICK_DESCRIPTION = "Gym class featuring a brown fox and lazy dog";
    protected static final String QUICK_CREATOR = "Nevin Nollop";

    private NodeService nodeService;
    private ContentService contentService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;

    private ContentMetadataExtracter executer;

    private final static String ID = GUID.generate();

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");
        
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
        this.executer = (ContentMetadataExtracter) this.applicationContext.getBean("extract-metadata");
    }

    /**
     * Test execution of the extraction itself
     */
    public void testFromBlanks()
    {
        // Test that the action writes properties when they don't exist or are
        // unset

        // Get the old props
        Map<QName, Serializable> props = this.nodeService.getProperties(this.nodeRef);
        props.remove(ContentModel.PROP_AUTHOR);
        props.put(ContentModel.PROP_TITLE, "");
        props.put(ContentModel.PROP_DESCRIPTION, null); // Wonder how this will
                                                        // be handled
        this.nodeService.setProperties(this.nodeRef, props);

        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, SetPropertyValueActionExecuter.NAME, null);

        this.executer.execute(action, this.nodeRef);

        // Check that the properties have been set
        assertEquals(QUICK_TITLE, this.nodeService.getProperty(this.nodeRef, ContentModel.PROP_TITLE));
        assertEquals(QUICK_DESCRIPTION, this.nodeService.getProperty(this.nodeRef, ContentModel.PROP_DESCRIPTION));
        assertEquals(QUICK_CREATOR, this.nodeService.getProperty(this.nodeRef, ContentModel.PROP_AUTHOR));
    }
    
    private static final QName PROP_UNKNOWN_1 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "unkown1");
    private static final QName PROP_UNKNOWN_2 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "unkown2");
    private static class UnknownMetadataExtracter extends AbstractMappingMetadataExtracter
    {
        public UnknownMetadataExtracter()
        {
            Properties mappingProperties = new Properties();
            mappingProperties.put("unknown1", PROP_UNKNOWN_1.toString());
            mappingProperties.put("unknown2", PROP_UNKNOWN_2.toString());
            setMappingProperties(mappingProperties);
        }
        @Override
        protected Map<String, Set<QName>> getDefaultMapping()
        {
            // No need to give anything back as we have explicitly set the mapping already
            return new HashMap<String, Set<QName>>(0);
        }
        @Override
        public boolean isSupported(String sourceMimetype)
        {
            return sourceMimetype.equals(MimetypeMap.MIMETYPE_BINARY);
        }

        public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
        {
            Map<String, Serializable> rawMap = newRawMap();
            rawMap.put("unknown1", new Integer(1));
            rawMap.put("unknown2", "TWO");
            return rawMap;
        }
    }
    
    public void testUnknownProperties()
    {
        MetadataExtracterRegistry registry = (MetadataExtracterRegistry) applicationContext.getBean("metadataExtracterRegistry");
        UnknownMetadataExtracter extracterUnknown = new UnknownMetadataExtracter();
        extracterUnknown.setRegistry(registry);
        extracterUnknown.register();
        // Now add some content with a binary mimetype
        ContentWriter cw = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        cw.setMimetype(MimetypeMap.MIMETYPE_BINARY);
        cw.putContent("Content for " + getName());
        
        ActionImpl action = new ActionImpl(null, ID, SetPropertyValueActionExecuter.NAME, null);
        executer.execute(action, this.nodeRef);
        
        // The unkown properties should be present
        Serializable prop1 = nodeService.getProperty(nodeRef, PROP_UNKNOWN_1);
        Serializable prop2 = nodeService.getProperty(nodeRef, PROP_UNKNOWN_2);
        
        assertNotNull("Unknown property is null", prop1);
        assertNotNull("Unknown property is null", prop2);
    }

    /**
     * Test execution of the pragmatic approach
     */
    public void testFromPartial()
    {
        // Test that the action does not overwrite properties that are already
        // set
        String myCreator = "Null-op";
        String myTitle = "The hot dog is eaten by the city fox";

        // Get the old props
        Map<QName, Serializable> props = this.nodeService.getProperties(this.nodeRef);
        props.put(ContentModel.PROP_AUTHOR, myCreator);
        props.put(ContentModel.PROP_TITLE, myTitle);
        props.remove(ContentModel.PROP_DESCRIPTION); // Allow this baby
        this.nodeService.setProperties(this.nodeRef, props);

        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, SetPropertyValueActionExecuter.NAME, null);

        this.executer.execute(action, this.nodeRef);

        // Check that the properties have been preserved
        assertEquals(myTitle, this.nodeService.getProperty(this.nodeRef, ContentModel.PROP_TITLE));
        assertEquals(myCreator, this.nodeService.getProperty(this.nodeRef, ContentModel.PROP_AUTHOR));

        // But this one should have been set
        assertEquals(QUICK_DESCRIPTION, this.nodeService.getProperty(this.nodeRef, ContentModel.PROP_DESCRIPTION));
    }
}
