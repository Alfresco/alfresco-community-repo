/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
        
        return suite;
    }
}
