package org.alfresco.repo.action;

import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluatorTest;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluatorTest;
import org.alfresco.repo.action.evaluator.HasAspectEvaluatorTest;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluatorTest;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuterTest;
import org.alfresco.repo.action.executer.ContentMetadataEmbedderTest;
import org.alfresco.repo.action.executer.ContentMetadataExtracterTest;
import org.alfresco.repo.action.executer.ImporterActionExecuterTest;
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
	MailActionExecuterTest.class,
    ActionServiceImpl2Test.class,
    ImporterActionExecuterTest.class
})
public class ActionTestSuite
{
}
