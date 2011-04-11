/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.usage;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Derek Hulley
 * @since V3.4 Team
 */
public class UsageTestSuite extends TestSuite
{
    public static ApplicationContext getContext() 
    {
        ApplicationContextHelper.setUseLazyLoading(false);
        ApplicationContextHelper.setNoAutoStart(true);
        return ApplicationContextHelper.getApplicationContext(
             new String[] { "classpath:alfresco/minimal-context.xml" }
        );
    }
        
    public static Test suite() 
    {
        // Setup the context
        getContext();
        
        TestSuite suite = new TestSuite();
        suite.addTestSuite(RepoUsageComponentTest.class);
        suite.addTestSuite(UserUsageTrackingComponentTest.class);
        return suite;
    }
}
