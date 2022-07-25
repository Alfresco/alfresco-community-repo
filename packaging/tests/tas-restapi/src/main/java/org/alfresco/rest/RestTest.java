/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest;

import java.lang.reflect.Method;

import org.alfresco.dataprep.WorkflowService;
import org.alfresco.rest.core.RestProperties;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.LogFactory;
import org.alfresco.utility.TasProperties;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataGroup;
import org.alfresco.utility.data.DataLink;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUserAIS;
import org.alfresco.utility.data.DataWorkflow;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.network.ServerHealth;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

@ContextConfiguration("classpath:alfresco-restapi-context.xml")
public abstract class RestTest extends AbstractTestNGSpringContextTests
{
    private static Logger LOG = LogFactory.getLogger();
    
    @Autowired
    protected RestProperties restProperties;

    @Autowired
    protected TasProperties properties;

    @Autowired
    protected ServerHealth serverHealth;

    @Autowired
    protected RestWrapper restClient;
    
    @Autowired
    protected DataUserAIS dataUser;

    @Autowired
    protected DataSite dataSite;
    
    @Autowired
    protected DataContent dataContent;
    
    @Autowired
    protected DataGroup dataGroup;

    @Autowired
    protected DataWorkflow dataWorkflow;

    @Autowired
    protected DataLink dataLink;
    
    @Autowired
    protected WorkflowService workflow;

    protected SiteModel testSite;

    @BeforeSuite(alwaysRun = true)
    public void checkServerHealth()
    {
        try
        {
            super.springTestContextPrepareTestInstance();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Error while preparing for test execution", e);
        }
        serverHealth.assertServerIsOnline();
        testSite = dataSite.createPublicRandomSite();
    }
    
    @BeforeMethod(alwaysRun=true)
    public void showStartTestInfo(Method method)
    {      
      LOG.info(String.format("*** STARTING Test: [%s] ***",method.getName()));      
    }
    
    @AfterMethod(alwaysRun=true)
    public void showEndTestInfo(Method method)
    {      
      LOG.info(String.format("*** ENDING Test: [%s] ***", method.getName()));
    }
}
