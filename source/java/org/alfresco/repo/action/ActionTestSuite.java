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
package org.alfresco.repo.action;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluatorTest;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluatorTest;
import org.alfresco.repo.action.evaluator.HasAspectEvaluatorTest;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluatorTest;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuterTest;
import org.alfresco.repo.action.executer.ContentMetadataExtracterTest;
import org.alfresco.repo.action.executer.SetPropertyValueActionExecuterTest;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuterTest;


/**
 * Version test suite
 * 
 * @author Roy Wetherall
 */
public class ActionTestSuite extends TestSuite
{
    /**
     * Creates the test suite
     * 
     * @return  the test suite
     */
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ParameterDefinitionImplTest.class);
        suite.addTestSuite(ActionDefinitionImplTest.class);
        suite.addTestSuite(ActionConditionDefinitionImplTest.class);
        suite.addTestSuite(ActionImplTest.class);
        suite.addTestSuite(ActionConditionImplTest.class);
        suite.addTestSuite(CompositeActionImplTest.class);
        suite.addTestSuite(ActionServiceImplTest.class);
        
        // Test evaluators
        suite.addTestSuite(IsSubTypeEvaluatorTest.class);
        suite.addTestSuite(ComparePropertyValueEvaluatorTest.class);
        suite.addTestSuite(CompareMimeTypeEvaluatorTest.class);
        suite.addTestSuite(HasAspectEvaluatorTest.class);
        
        // Test executors
        suite.addTestSuite(SetPropertyValueActionExecuterTest.class);
        suite.addTestSuite(AddFeaturesActionExecuterTest.class);
        suite.addTestSuite(ContentMetadataExtracterTest.class);
        suite.addTestSuite(SpecialiseTypeActionExecuterTest.class);
        
        return suite;
    }
}
