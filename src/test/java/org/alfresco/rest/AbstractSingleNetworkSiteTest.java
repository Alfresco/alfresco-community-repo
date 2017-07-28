/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.rest;

import org.alfresco.rest.api.tests.AbstractBaseApiTest;
import org.junit.After;
import org.junit.Before;

/**
 * Overrides AbstractBaseApiTest so that only a single network & site is created per test 
 * (instead of pre-creating multiple networks & sites)
 * 
 * Can also be optionally tweaked locally to:
 * 
 * - use the default network (ie. super tenant) => instead of creating a new tenant
 * 
 * - re-use a single setup across test methods => although this does mean that each individual test method must either rely on uniquely created test data and/or cleanup
 * 
 * Note: For now, these can be explicitly tweaked by a dev (do not commit) 
 * but in the future we could consider making these runtime options.
 * 
 * @author Gethin James
 * @author janv
 */
public class AbstractSingleNetworkSiteTest extends AbstractBaseApiTest
{
    // note: experimental - for local/dev-use only (YMMV) ;-)
    // - setting both to true should make the related tests run faster
    // - if singleSetupNoTearDown=true then each individual test method should create unique data (or cleanup) to avoid interdependent test/run failures
    // - if useDefaultNetwork=true then no tenant will be created (ie. will use default/super tenant)
    protected static boolean singleSetupNoTearDown = false;
    protected static boolean useDefaultNetwork = false;
    
    private static boolean isSetup = false;
    
    @Override
    public String getScope()
    {
        return "public";
    }
    
    @Override
    @Before
    public void setup() throws Exception
    {
        if ((! isSetup) || (! singleSetupNoTearDown))
        {
            if (! useDefaultNetwork)
            {
                networkOne = getRepoService().createNetwork(this.getClass().getName().toLowerCase(), true);
                networkOne.create();
            }
            else
            {
                networkOne = getRepoService().getSystemNetwork();
            }
            
            super.setup();
            isSetup = true;
        }
    }

    @Override
    @After
    public void tearDown() throws Exception
    {
        if (! singleSetupNoTearDown)
        {
            super.tearDown();
        }
    }
}
