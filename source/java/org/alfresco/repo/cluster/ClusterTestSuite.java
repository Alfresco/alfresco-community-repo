/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.cluster;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for the org.alfresco.repo.cluster package.
 * <p>
 * This includes tests which will <strong>fail</strong> on the build servers -
 * do not include this suite in the CI build targets.
 * 
 * @author Matt Ward
 */
@RunWith(Suite.class)
@SuiteClasses({
    // Run the standard tests
    org.alfresco.repo.cluster.BuildSafeTestSuite.class,
    
    // Additionally run these tests that cannot be run on the build servers.
    org.alfresco.repo.cluster.HazelcastTest.class
})
public class ClusterTestSuite
{
}
