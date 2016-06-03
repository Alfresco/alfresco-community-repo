
package org.alfresco.repo.nodelocator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.when;

import javax.annotation.Resource;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.site.SiteServiceInternal;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
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
@ContextConfiguration(locations={"classpath:alfresco/node-locator-context.xml", "classpath:test-nodeLocatorServiceImpl-context.xml"})
public class NodeLocatorServiceImplTest
{
    private static final NodeRef companyHome = new NodeRef("alfresco://company/home");
    private static final NodeRef sitesHome = new NodeRef("alfresco://sites/home");

    @Resource
    private NodeLocatorService nodeLocatorService;
    
    @Autowired
    private Repository repositoryHelper;
    
    @Autowired
    private SiteServiceInternal siteService;
    
    @Test
    public void testUnknownNodeLocator() throws Exception
    {
        try 
        {
            nodeLocatorService.getNode(null, null, null);
            fail("An exception should have been thrown!");
        }
        catch(IllegalArgumentException e)
        {
            //NOOP
        }
        try 
        {
            nodeLocatorService.getNode("some unknown name", null, null);
            fail("An exception should have been thrown!");
        }
        catch(IllegalArgumentException e)
        {
            //NOOP
        }
    }
    
    @Test
    public void testCompanyHomeNodeLocator() throws Exception
    {
        NodeRef result = nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);
        assertEquals(companyHome, result);
    }
    
    @Test
    public void testSitesHomeNodeLocator() throws Exception
    {
        NodeRef result = nodeLocatorService.getNode(SitesHomeNodeLocator.NAME, null, null);
        assertEquals(sitesHome, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegister()
    {
        nodeLocatorService.register(SitesHomeNodeLocator.NAME, new SelfNodeLocator());
    }

    @Before
    public void setUpClass()
    {
        when(repositoryHelper.getCompanyHome()).thenReturn(companyHome);
        when(siteService.getSiteRoot()).thenReturn(sitesHome);
    }
}
