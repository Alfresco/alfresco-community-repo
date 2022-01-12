/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.test.util.bdt;

import static org.junit.Assert.fail;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.module.org_alfresco_module_rm.test.util.bdt.BehaviourTest.Work;

/**
 * Expected failure.
 * 
 * @author Roy Wetherall
 * @since 2.5
 */
public class ExpectedFailure
{
    private static final String MESSAGE = "Expected failure \"{0}\" was not observed.";
    
    private BehaviourTest test;
    private Set<Class<? extends Exception>> exceptionClasses;
    private Work work;
    
    @SafeVarargs
    public ExpectedFailure(BehaviourTest test, Class<? extends Exception> ...exceptionClasses)
    {
        this.test = test;
        this.exceptionClasses = Arrays.stream(exceptionClasses).collect(Collectors.toSet());
    }
    
    public ExpectedFailure from(Work work)
    {
        this.work = work;
        return this;
    }
    
    public BehaviourTest because(String message)
    {
        try
        {
            test.perform(work);
        }
        catch(Exception actualException)
        {
            boolean found = false;
            
            for (Class<? extends Exception> exceptionClass : exceptionClasses)
            {   
                if (exceptionClass.isAssignableFrom(actualException.getClass()))
                {
                    found = true;
                }
            }
            
            if (!found)
            {
                fail(MessageFormat.format(MESSAGE, message));
            }
        }
        
        return test;
    }
    
}
