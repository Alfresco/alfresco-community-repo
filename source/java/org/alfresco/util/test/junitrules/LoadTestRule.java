/*
 * Copyright (C) 2005-2012
 Alfresco Software Limited.
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
package org.alfresco.util.test.junitrules;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ErrorCollector;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * This JUnit rule can be used to turn existing test code into Load Tests.
 * It does this in conjunction with the {@link AlfrescoPeople} JUnit rule.
 * That rule is used to {@link AlfrescoPeople#AlfrescoPeople(ApplicationContextInit, int) create} a
 * fixed number of Alfresco users. Then {@link LoadTestRule} will do the following for each of your JUnit 4 &#64;Test methods:
 * <ul>
 * <li>if they are annotated with the {@link LoadTest} marker annotation:
 *    <ol>
 *    <li>one Java thread for each of the users created by the {@link AlfrescoPeople} rule will be created and started.</li>
 *    <li>each of those threads will start by authenticating as a different user from the above set.</li>
 *    <li>each of those threads will concurrently execute the same JUnit &#64;Test method.</li>
 *    <li>if all the concurrent threads complete execution successfully, the &#64;Test method is passed.</li>
 *    <li>but if one or more of those concurrent threads fail, the error messages will be aggregated together into a single error for that method.</li>
 *    </ol>
 * <li>else they will be executed as normal and will pass or fail as normal.</li>
 * </ul>
 * <p/>
 * Example usage, where we have a 'normal' feature test and a load test for the same feature.:
 * <pre>
 * public class YourTestClass
 * {
 *     // We need to ensure that JUnit Rules in the same 'group' (in this case the 'static' group) execute in the correct
 *     // order. To do this we do not annotate the JUnit Rule fields themselves, but instead wrap them up in a RuleChain.
 *     
 *     // Initialise the spring application context with a rule.
 *     public static final ApplicationContextInit APP_CONTEXT_RULE = new ApplicationContextInit();
 *     public static final AlfrescoPeople         TEST_USERS       = new AlfrescoPeople(APP_CONTEXT_RULE, 32);
 *     
 *     &#64;ClassRule public static RuleChain STATIC_RULE_CHAIN = RuleChain.outerRule(APP_CONTEXT_RULE)
 *                                                                     .around(TEST_USERS);
 *                                                              
 *     &#64;Rule public LoadTestRule loadTestRule = new LoadTestRule(TEST_USERS);
 *     
 *     &#64;Test public void aNormalTestMethod()
 *     {
 *         ensureFeatureFooWorks()
 *     }
 *     
 *     &#64;LoadTest &#64;Test public void aLoadTestMethod()
 *     {
 *         ensureFeatureFooWorks()
 *     }
 *     
 *     public void ensureFeatureFooWorks() {}
 * }
 * </pre>
 * 
 * @author Neil Mc Erlean
 */
public class LoadTestRule extends ErrorCollector
{
    private static final Log log = LogFactory.getLog(LoadTestRule.class);
    
    private final AlfrescoPeople people;
    
    public LoadTestRule(AlfrescoPeople people)
    {
        this.people = people;
    }
    
    /**
     * Gets the number of users/concurrent threads that this Rule has been configured to use.
     * @return the number of users/threads.
     */
    public int getCount()
    {
        return this.people.getUsernames().size();
    }
    
    @Override public Statement apply(final Statement base, final Description description)
    {
        boolean loadTestingRequestedForThisMethod = false;
        
        Collection<Annotation> annotations = description.getAnnotations();
        for (Annotation anno : annotations)
        {
            if (anno.annotationType().equals(LoadTest.class))
            {
                loadTestingRequestedForThisMethod = true;
            }
        }
        
        if (loadTestingRequestedForThisMethod)
        {
            log.debug(LoadTest.class.getSimpleName() + "-based testing configured for method " + description.getMethodName());
            
            return new Statement()
            {
                @Override public void evaluate() throws Throwable
                {
                    int executionCount = getCount();
                    int currentIndex = 1;
                    
                    final CountDownLatch latch = new CountDownLatch(executionCount);
                    
                    for (String username: people.getUsernames())
                    {
                        log.debug("About to start " + description.getMethodName() + ". " + currentIndex + "/" + executionCount + " as " + username);
                        new Thread(new StatementEvaluatorRunnable(username, base, latch)).start();
                        
                        currentIndex++;
                    }
                    
                    latch.await();
                    
                    verify();
                }
            };
        }
        else
        {
            log.debug(LoadTest.class.getSimpleName() + "-based testing NOT configured for this method.");
            
            return base;
        }
    }
    
    private class StatementEvaluatorRunnable implements Runnable
    {
        private final String username;
        private final CountDownLatch latch;
        private final Statement base;
        public StatementEvaluatorRunnable(String username, Statement base, CountDownLatch latch)
        {
            this.username = username;
            this.latch = latch;
            this.base = base;
        }
        
        @Override public void run()
        {
            try
            {
                log.debug("Setting fully auth'd user to " + username);
                AuthenticationUtil.setFullyAuthenticatedUser(username);
                
                base.evaluate();
            }
            catch (Throwable t)
            {
                addError(t);
            }
            
            latch.countDown();
        }
    }
    
    /**
     * This annotation is a marker used to identify a JUnit &#64;Test method as a "load test".
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface LoadTest
    {
        // Intentionally empty
    }
}
