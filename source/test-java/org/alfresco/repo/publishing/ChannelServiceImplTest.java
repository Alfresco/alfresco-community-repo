
package org.alfresco.repo.publishing;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.List;

import javax.annotation.Resource;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Nick Smith
 * @since 4.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test/alfresco/test-web-publishing-context.xml"})
public class ChannelServiceImplTest
{
    @Resource
    ChannelServiceImpl channelService;

    @Resource
    NodeService nodeService;
    
    @Resource
    ServiceRegistry serviceRegistry;
    
    @Resource
    MockChannelType mockChannelType;
    
    @Test
    public void testRegister()
    {
        List<ChannelType> types = channelService.getChannelTypes();
        
        // Check the mock channel type is registered through Spring.
        assertTrue(types.contains(mockChannelType));
        channelService.getChannelType(MockChannelType.ID);
        try
        {
            channelService.register(null);
            fail("Exception expected when calling register(null)");
        }
        catch(IllegalArgumentException e)
        {
            //NOOP
        }
        try
        {
            channelService.register(mockChannelType);
            fail("Exception expected when trying to register the same ChannelType twice");
        }
        catch(IllegalArgumentException e)
        {
            //NOOP
        }
    }
    
}
