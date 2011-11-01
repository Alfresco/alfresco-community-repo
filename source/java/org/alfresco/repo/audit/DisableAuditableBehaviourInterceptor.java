/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.audit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * An interceptor that disables and then enables ASPECT_AUDITABLE behaviours
 * around method calls.
 * <ul>
 * <li>The name of the method must match a supplied list (See
 *     {@link #setMethodNames(List)}).</li>
 * <li>For this interceptor to disable and enable policy behaviour, the first
 *     argument to the method must be a NodeRef or a Collection of NodeRefs.
 *     The behaviour is disabled on each NodeRef.</li>
 * <li>The second argument to the method must optionally match a supplied list of
 *     values (See {@link #setArgumentValues(List)}. The second argument must be
 *     a QName. If a list is not supplied the second argument is not checked.</li>
 * <li>The BehaviourFilter to be enabled or disabled must be set (See
 *     {@link #setBehaviourFilter(BehaviourFilter)}).</li>
 * </ul>
 *     
 * @author Stas Sokolovsky
 */
public class DisableAuditableBehaviourInterceptor implements MethodInterceptor
{
    private BehaviourFilter behaviourFilter;
    private Set<String> methodNames = new HashSet<String>(0);
    private Set<QName> argumentQNameValues = new HashSet<QName>(0);

    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation methodInvocation) throws Throwable
    {
        String methodName = methodInvocation.getMethod().getName();

        Object[] args = methodInvocation.getArguments();
        ArrayList<NodeRef> nodes = new ArrayList<NodeRef>();
        if (args.length > 0)
        {
            if (args[0] instanceof NodeRef)
            {
                nodes.add((NodeRef) args[0]);
            }
            else if (args[0] instanceof Collection)
            {
                nodes.addAll((Collection<? extends NodeRef>) args[0]);
            }
        }
        QName arg1 = null;
        if (args.length > 1 && args[1] instanceof QName)
        {
            arg1 = (QName) args[1];
        }

        if (behaviourFilter != null &&
            methodNames.contains(methodName) &&
            (arg1 == null || argumentQNameValues.contains(arg1)))
        {
            for (NodeRef nodeRef : nodes)
            {
                behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            }
            try
            {
                return methodInvocation.proceed();
            }
            finally
            {
                for (NodeRef nodeRef : nodes)
                {
                    behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                }
            }
        }
        else
        {
            return methodInvocation.proceed();
        }
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setMethodNames(List<String> methodNames)
    {
        this.methodNames = new HashSet<String>(methodNames);
    }

    public void setArgumentValues(List<String> argumentValues)
    {
        this.argumentQNameValues = new HashSet<QName>(argumentValues.size()*2+1);
        for (String argumentValue : argumentValues)
        {
            QName argumentQNameValue = QName.createQName(argumentValue);
            argumentQNameValues.add(argumentQNameValue);
        }
    }
}
