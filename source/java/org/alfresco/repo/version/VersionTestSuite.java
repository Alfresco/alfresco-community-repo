/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.version;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.version.common.VersionHistoryImplTest;
import org.alfresco.repo.version.common.VersionImplTest;
import org.alfresco.repo.version.common.counter.VersionCounterServiceTest;
import org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicyTest;

/**
 * Version test suite
 * 
 * @author Roy Wetherall
 */
public class VersionTestSuite extends TestSuite
{
    /**
     * Creates the test suite
     * 
     * @return  the test suite
     */
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(VersionImplTest.class);
        suite.addTestSuite(VersionHistoryImplTest.class);
        suite.addTestSuite(SerialVersionLabelPolicyTest.class);
        suite.addTestSuite(VersionCounterServiceTest.class);
        suite.addTestSuite(VersionServiceImplTest.class);
        suite.addTestSuite(NodeServiceImplTest.class);
        suite.addTestSuite(ContentServiceImplTest.class);
        return suite;
    }
}
