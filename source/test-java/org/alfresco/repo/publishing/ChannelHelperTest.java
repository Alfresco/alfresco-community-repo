
package org.alfresco.repo.publishing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test/alfresco/test-web-publishing-context.xml"})
public class ChannelHelperTest
{
    @Autowired
    private ChannelHelper helper;
    
    @Test
    public void testMapNodeRef() throws Exception
    {
//        String guid = GUID.generate();
//        NodeRef testNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, guid);
//        NodeRef liveEnvironmentNode = environmentHelper.getEnvironment(siteId, PublishingService.LIVE_ENVIRONMENT_NAME);
//        NodeRef mappedNodeRef = environmentHelper.mapEditorialToEnvironment(liveEnvironmentNode, testNodeRef);
//        assertNotSame(mappedNodeRef, testNodeRef);
//        NodeRef unmappedNodeRef = environmentHelper.mapEnvironmentToEditorial(liveEnvironmentNode, mappedNodeRef);
//        assertEquals(testNodeRef, unmappedNodeRef);
    }
}
