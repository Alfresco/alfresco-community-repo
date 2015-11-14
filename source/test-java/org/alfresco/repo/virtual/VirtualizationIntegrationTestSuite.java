/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.virtual.bundle.VirtualCheckOutCheckInServiceExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualFileFolderServiceExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualLockableAspectInterceptorExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualNodeServiceExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualPermissionServiceExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualRatingServiceExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualVersionServiceExtensionTest;
import org.alfresco.repo.virtual.config.NodeRefPathExpressionTest;
import org.alfresco.repo.virtual.model.SystemTemplateLocationsConstraintTest;
import org.alfresco.repo.virtual.store.SystemVirtualizationMethodTest;
import org.alfresco.repo.virtual.store.TypeVirtualizationMethodTest;
import org.alfresco.repo.virtual.store.VirtualStoreImplTest;
import org.alfresco.repo.virtual.template.ApplyTemplateMethodTest;
import org.alfresco.repo.virtual.template.TemplateFilingRuleTest;
import org.alfresco.repo.virtual.template.TemplateResourceProcessorTest;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * 
 *
 * @author Bogdan Horje
 */
public class VirtualizationIntegrationTestSuite extends TestSuite implements VirtualizationTest
{
    protected static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);

    public static Test suite()
    {
        VirtualizationConfigTestBootstrap virtualizationConfigTestBootstrap = ctx
                    .getBean(VIRTUALIZATION_CONFIG_TEST_BOOTSTRAP_BEAN_ID,
                             VirtualizationConfigTestBootstrap.class);

        TestSuite suite = new TestSuite();

        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.VirtualizationConfigurationTest.class));

        if (virtualizationConfigTestBootstrap.areVirtualFoldersEnabled())
        {
            suite.addTest(new JUnit4TestAdapter(VirtualLockableAspectInterceptorExtensionTest.class));
            suite.addTest(new JUnit4TestAdapter(VirtualVersionServiceExtensionTest.class));
            suite.addTest(new JUnit4TestAdapter(VirtualRatingServiceExtensionTest.class));
            suite.addTest(new JUnit4TestAdapter(VirtualCheckOutCheckInServiceExtensionTest.class));
            suite.addTest(new JUnit4TestAdapter(VirtualPermissionServiceExtensionTest.class));
            suite.addTest(new JUnit4TestAdapter(VirtualNodeServiceExtensionTest.class));
            suite.addTest(new JUnit4TestAdapter(VirtualFileFolderServiceExtensionTest.class));
            suite.addTest(new JUnit4TestAdapter(ApplyTemplateMethodTest.class));    
            suite.addTest(new JUnit4TestAdapter(SystemTemplateLocationsConstraintTest.class));
            suite.addTest(new JUnit4TestAdapter(SystemVirtualizationMethodTest.class));
            suite.addTest(new JUnit4TestAdapter(TypeVirtualizationMethodTest.class));
            suite.addTest(new JUnit4TestAdapter(TemplateResourceProcessorTest.class));
            suite.addTest(new JUnit4TestAdapter(VirtualStoreImplTest.class));
            suite.addTest(new JUnit4TestAdapter(NodeRefPathExpressionTest.class));
            suite.addTest(new JUnit4TestAdapter(TemplateFilingRuleTest.class));
        }

        return suite;

    }
}
