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
package org.alfresco.repo.audit;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.audit.access.AccessAuditorTest;

/**
 * Suite for audit-related tests.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class AuditTestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(AuditableAnnotationTest.class);
        suite.addTestSuite(AuditableAspectTest.class);
        suite.addTestSuite(AuditBootstrapTest.class);
        suite.addTestSuite(AuditComponentTest.class);
        suite.addTestSuite(UserAuditFilterTest.class);
        suite.addTestSuite(AuditMethodInterceptorTest.class);

        suite.addTest(new JUnit4TestAdapter(PropertyAuditFilterTest.class));
        suite.addTest(new JUnit4TestAdapter(AccessAuditorTest.class));

        return suite;
    }
}
