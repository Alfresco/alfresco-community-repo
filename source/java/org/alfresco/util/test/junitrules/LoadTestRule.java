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
 * TODO
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
    
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface LoadTest
    {
        // Intentionally empty
    }
}
