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
package org.alfresco.repo.action;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluatorTest;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluatorTest;
import org.alfresco.repo.action.evaluator.HasAspectEvaluatorTest;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluatorTest;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuterTest;
import org.alfresco.repo.action.executer.ContentMetadataExtracterTest;
import org.alfresco.repo.action.executer.RemoveFeaturesActionExecuterTest;
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
        suite.addTestSuite(CompositeActionConditionImplTest.class);
        
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
        suite.addTestSuite(RemoveFeaturesActionExecuterTest.class);
        suite.addTestSuite(ActionTrackingServiceImplTest.class);
        
        return suite;
    }
}
