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
package org.alfresco.repo.action;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluatorTest;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluatorTest;
import org.alfresco.repo.action.evaluator.HasAspectEvaluatorTest;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluatorTest;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuterTest;
import org.alfresco.repo.action.executer.ContentMetadataEmbedderTest;
import org.alfresco.repo.action.executer.ContentMetadataExtracterTagMappingTest;
import org.alfresco.repo.action.executer.ContentMetadataExtracterTest;
import org.alfresco.repo.action.executer.ImporterActionExecuterTest;
import org.alfresco.repo.action.executer.MailActionExecuterTest;
import org.alfresco.repo.action.executer.RemoveFeaturesActionExecuterTest;
import org.alfresco.repo.action.executer.SetPropertyValueActionExecuterTest;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuterTest;

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
        ContentMetadataExtracterTagMappingTest.class,
        ContentMetadataEmbedderTest.class,
        SpecialiseTypeActionExecuterTest.class,
        RemoveFeaturesActionExecuterTest.class,
        ActionTrackingServiceImplTest.class, // intermittent - pending ALF-9773 & ALF-9774
        MailActionExecuterTest.class,
        ActionServiceImpl2Test.class,
        ImporterActionExecuterTest.class
})
public class ActionTestSuite
{}
