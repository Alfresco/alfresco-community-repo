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
package org.alfresco.repo.version;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.version.common.VersionHistoryImplTest;
import org.alfresco.repo.version.common.VersionImplTest;
import org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicyTest;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Version test suite
 * 
 * @author Roy Wetherall, janv
 */
public class VersionTestSuite extends TestSuite
{
    public static ApplicationContext getContext() 
    {
        ApplicationContextHelper.setUseLazyLoading(false);
        ApplicationContextHelper.setNoAutoStart(true);
        return ApplicationContextHelper.getApplicationContext(
             new String[] { "classpath:alfresco/minimal-context.xml" }
        );
    }
        
    /**
     * Creates the test suite
     * 
     * @return  the test suite
     */
    public static Test suite() 
    {
        // Setup the context
        getContext();
        
        TestSuite suite = new TestSuite();
        suite.addTestSuite(VersionImplTest.class);
        suite.addTestSuite(VersionHistoryImplTest.class);
        suite.addTestSuite(SerialVersionLabelPolicyTest.class);
        suite.addTestSuite(VersionServiceImplTest.class);
        suite.addTestSuite(NodeServiceImplTest.class);
        suite.addTestSuite(ContentServiceImplTest.class);
        suite.addTestSuite(VersionMigratorTest.class);
        return suite;
    }
}
