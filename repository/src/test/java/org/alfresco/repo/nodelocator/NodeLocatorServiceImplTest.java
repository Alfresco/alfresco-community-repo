/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
