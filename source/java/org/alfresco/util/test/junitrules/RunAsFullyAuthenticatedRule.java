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
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import junit.framework.Test;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * This JUnit rule can be used to make test methods run as a particular user.
 * A username can be provided on construction to the rule and then all <code>@Test</code> methods will be
 * run as that user.
 * <p/>
 * Furthermore, if an individual test method is annotated like this <code>@RunAsUser(userName="John")</code> than that
 * method (and only that method) will be run as "John".
 * <p/>
 * Example usage:
 * <pre>
 * public class YourTestClass
 * {
 *     &#64;ClassRule public static final ApplicationContextInit APP_CONTEXT_RULE = new ApplicationContextInit();
 *     &#64;Rule public RunAsFullyAuthenticatedRule runAsGuidPerson = new RunAsFullyAuthenticatedRule("NeilM");
 *     
 *     &#64;Test public void doSomething() throws Exception
 *     {
 *         // This will run as NeilM
 *     }
 *     
 *     &#64;Test &#64;RunAsUser(userName="DaveC") public void doSomething() throws Exception
 *     {
 *         // This will run as DaveC
 *     }
 * }
 * </pre>
 * 
 * @author Neil Mc Erlean
 * @since Odin
 */
public class RunAsFullyAuthenticatedRule implements TestRule
{
    /**
     * This annotation can be used to mark an individual {@link Test} method for running as a named user.
     * 
     * @author Neil Mc Erlean
     * @since Odin
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface RunAsUser
    {
        String userName() default "";
    }
    
    
    private static final Log log = LogFactory.getLog(RunAsFullyAuthenticatedRule.class);
    
    /**
     * A fixed username to run as.
     */
    private final String fixedUserName;
    
    /**
     * A rule which will provide a username to run as
     */
    private final AlfrescoPerson personRule;
    
    
    /**
     * This constructs a rule where there is no specified user to run as.
     * For this to be useful (or legal) a user must be specified on every test method using {@link RunAsUser}.
     */
    public RunAsFullyAuthenticatedRule()
    {
        this.fixedUserName = null;
        this.personRule = null;
    }
    
    /**
     * @param userName the username which all test methods should run as.
     */
    public RunAsFullyAuthenticatedRule(String userName)
    {
        ParameterCheck.mandatory("userName", userName);
        
        this.fixedUserName = userName;
        this.personRule = null;
    }
    
    /**
     * @param personRule the rule which will provide the username which all test methods should run as.
     */
    public RunAsFullyAuthenticatedRule(AlfrescoPerson personRule)
    {
        ParameterCheck.mandatory("personRule", personRule);
        
        this.fixedUserName = null;
        this.personRule = personRule;
    }
    
    /**
     * Get the username which test methods will run as.
     */
    public String getUsername()
    {
        return this.fixedUserName;
    }
    
    
    @Override public Statement apply(final Statement base, final Description description)
    {
        return new Statement()
        {
            @Override public void evaluate() throws Throwable
            {
                // Store the current authentication
                AuthenticationUtil.pushAuthentication();
                
                // First, try for a username provided on the @Test method itself.
                String runAsUser = getMethodAnnotatedUserName(description);
                
                if (runAsUser != null)
                {
                    // There is a @Test method username.
                    log.debug("Running as method annotation-provided user: " + runAsUser);
                    log.debug("   See " + description.getClassName() + "." + description.getMethodName());
                }
                else
                {
                    // There is no @Test method username, so fall back to rule-provided person.
                    if (fixedUserName != null)
                    {
                        runAsUser = fixedUserName;
                        log.debug("Running as username defined in this rule: " + runAsUser);
                    }
                    else if (personRule != null)
                    {
                        runAsUser = personRule.getUsername();
                        log.debug("Running as username provided by another rule: " + runAsUser);
                    }
                    else
                    {
                        throw new Exception("Illegal rule: must provide username or " +
                                                                        AlfrescoPerson.class.getSimpleName() + " at rule construction or else a " +
                                                                        RunAsUser.class.getSimpleName() + " annotation.");
                    }
                }
                AuthenticationUtil.setFullyAuthenticatedUser(runAsUser);
                
                
                try
                {
                    // Execute the test method or whatever other rules are configured further down the stack.
                    base.evaluate();
                }
                finally
                {
                    // After - ensure that pass or fail, the authentication is restored.
                    AuthenticationUtil.popAuthentication();
                }
            }
        };
    }
    
    /**
     * 
     * @param description the description object from JUnit
     * @return the username specified in the {@link RunAsUser} annotation, if there was one, else <code>null</code>.
     */
    private String getMethodAnnotatedUserName(Description description) throws IllegalArgumentException,
                                                                              SecurityException, IllegalAccessException,
                                                                              InvocationTargetException, NoSuchMethodException
    {
        String result = null;
        
        Collection<Annotation> annotations = description.getAnnotations();
        for (Annotation anno : annotations)
        {
            if (anno.annotationType().equals(RunAsUser.class))
            {
                result = (String) anno.annotationType().getMethod("userName").invoke(anno);
            }
        }
        
        return result;
    }
}
