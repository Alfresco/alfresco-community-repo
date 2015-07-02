/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification.interceptor;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.getFullyAuthenticatedUser;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.isRunAsUserTheSystemUser;
import static org.alfresco.util.ParameterCheck.mandatory;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.PostMethodInvocationProcessor;
import org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.PreMethodInvocationProcessor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Classification method interceptor
 *
 * @author Roy Wetherall
 * @author Tuna Aksoy
 * @since 3.0
 */
public class ClassificationMethodInterceptor implements MethodInterceptor
{
    /** Pre method invocation processor */
    private PreMethodInvocationProcessor preMethodInvocationProcessor;

    /** Post method invocation processor */
    private PostMethodInvocationProcessor postMethodInvocationProcessor;

    /**
     * @return the preMethodInvocationProcessor
     */
    protected PreMethodInvocationProcessor getPreMethodInvocationProcessor()
    {
        return this.preMethodInvocationProcessor;
    }

    /**
     * @return the postMethodInvocationProcessor
     */
    protected PostMethodInvocationProcessor getPostMethodInvocationProcessor()
    {
        return this.postMethodInvocationProcessor;
    }

    /**
     * @param postMethodInvocationProcessor the postMethodInvocationProcessor to set
     */
    public void setPostMethodInvocationProcessor(PostMethodInvocationProcessor postMethodInvocationProcessor)
    {
        this.postMethodInvocationProcessor = postMethodInvocationProcessor;
    }

    /**
     * @param preMethodInvocationProcessor the preMethodInvocationProcessor to set
     */
    public void setPreMethodInvocationProcessor(PreMethodInvocationProcessor preMethodInvocationProcessor)
    {
        this.preMethodInvocationProcessor = preMethodInvocationProcessor;
    }

    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        mandatory("invocation", invocation);

        boolean isUserValid = isUserValid();

        // Pre method invocation processing
        if (isUserValid)
        {
            getPreMethodInvocationProcessor().process(invocation);
        }

        // Method invocation
        Object result = invocation.proceed();

        // Post method invocation processing
        if (isUserValid && result != null)
        {
            result = getPostMethodInvocationProcessor().process(result);
        }

        return result;
    }

    /**
     * Checks if we have an authenticated user and that they aren't "System"
     *
     * @return <code>true</code> if we have an authenticated user and that they aren't "System", <code>false</code> otherwise.
     */
    private boolean isUserValid()
    {
        boolean result = false;

        if (isNotBlank(getFullyAuthenticatedUser()) && !isRunAsUserTheSystemUser())
        {
            result = true;
        }

        return result;
    }
}
