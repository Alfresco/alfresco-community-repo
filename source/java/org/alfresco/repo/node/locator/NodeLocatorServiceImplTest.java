/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.repo.node.locator;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.when;

import javax.annotation.Resource;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.site.SiteServiceInternal;
import org.alfresco.service.cmr.repository.NodeLocatorService;
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
    
    @Before
    public void setUpClass()
    {
        when(repositoryHelper.getCompanyHome()).thenReturn(companyHome);
        when(siteService.getSiteRoot()).thenReturn(sitesHome);
    }
}
