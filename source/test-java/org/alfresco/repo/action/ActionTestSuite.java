/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluatorTest;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluatorTest;
import org.alfresco.repo.action.evaluator.HasAspectEvaluatorTest;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluatorTest;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuterTest;
import org.alfresco.repo.action.executer.ContentMetadataEmbedderTest;
import org.alfresco.repo.action.executer.ContentMetadataExtracterTest;
import org.alfresco.repo.action.executer.MailActionExecuterTest;
import org.alfresco.repo.action.executer.RemoveFeaturesActionExecuterTest;
import org.alfresco.repo.action.executer.SetPropertyValueActionExecuterTest;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * Action test suite
 * 
 * @author Roy Wetherall
 * @author Alex Miller
 */
@RunWith(Suite.class)
@SuiteClasses({
    ParameterDefinitionImplTest.class,
    ActionDefinitionImplTest.class,
    ActionConditionDefinitionImplTest.class,
    ActionImplTest.class,
    ActionConditionImplTest.class,
    CompositeActionImplTest.class,
    ActionServiceImplTest.class,
    CompositeActionConditionImplTest.class,
    
    // Test evaluators
    IsSubTypeEvaluatorTest.class,
    ComparePropertyValueEvaluatorTest.class,
    CompareMimeTypeEvaluatorTest.class,
    HasAspectEvaluatorTest.class,
    
    // Test executors
    SetPropertyValueActionExecuterTest.class,
    AddFeaturesActionExecuterTest.class,
    ContentMetadataExtracterTest.class,
    ContentMetadataEmbedderTest.class,
    SpecialiseTypeActionExecuterTest.class,
    RemoveFeaturesActionExecuterTest.class,
    ActionTrackingServiceImplTest.class, // intermittent - pending ALF-9773 & ALF-9774
	MailActionExecuterTest.class	
})
public class ActionTestSuite
{
}
