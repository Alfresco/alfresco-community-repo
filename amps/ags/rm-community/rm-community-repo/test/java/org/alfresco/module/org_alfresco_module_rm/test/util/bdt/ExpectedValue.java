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

import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;

import org.alfresco.repo.security.authentication.AuthenticationUtil;

/**
 * Expected value.
 * 
 * @author Roy Wetherall
 * @since 2.5
 */
public class ExpectedValue<T>
{
    private static final String MESSAGE = "Expected value outcome \"{0}\" was not observed.";
    
    private T expectedValue;
    private Evaluation<T> evaluation;
    private BehaviourTest test;
    
    public ExpectedValue(BehaviourTest test, T value)
    {
        this.expectedValue = value;
        this.test = test;
    }
    
    public ExpectedValue<T> from(Evaluation<T> evaluation)
    {
        this.evaluation = evaluation;
        return this;
    }
    
    public BehaviourTest because(String message)
    {
        T actualValue = (T)AuthenticationUtil.runAs(() -> 
        {
            return test.getRetryingTransactionHelper().doInTransaction(() -> 
            {
                return evaluation.eval();
            });
        }, 
        test.getAsUser());        
        
        if (message != null)
        {
            message = MessageFormat.format(MESSAGE, message);
        }
        
        assertEquals(message, expectedValue, actualValue);
        
        return test;
    }

    public interface Evaluation<T>
    {
        T eval() throws Exception;
    }
}
