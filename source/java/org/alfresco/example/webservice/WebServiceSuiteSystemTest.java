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
package org.alfresco.example.webservice;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.example.webservice.authentication.AuthenticationServiceSystemTest;
import org.alfresco.example.webservice.authoring.AuthoringServiceSystemTest;
import org.alfresco.example.webservice.classification.ClassificationServiceSystemTest;
import org.alfresco.example.webservice.content.ContentServiceSystemTest;
import org.alfresco.example.webservice.repository.RepositoryServiceSystemTest;
import org.alfresco.repo.webservice.CMLUtilTest;

public class WebServiceSuiteSystemTest extends TestSuite
{
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(CMLUtilTest.class);
        suite.addTestSuite(AuthenticationServiceSystemTest.class);
        suite.addTestSuite(AuthoringServiceSystemTest.class);
        suite.addTestSuite(ClassificationServiceSystemTest.class);
        suite.addTestSuite(ContentServiceSystemTest.class);
        suite.addTestSuite(RepositoryServiceSystemTest.class);
        return suite;
    }
}
