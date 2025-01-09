/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 Alfresco Software Limited
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
package org.alfresco.repo.security.permissions;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.framework.ProxyFactory;

class ProxyFactoryUtils
{
    private ProxyFactoryUtils()
    {}

    /**
     * Delegate creation of {@link ProxyFactory} and proxy to have control over it in one place.
     *
     * @param collection
     *            given collection for ProxyFactory.
     * @param advisor
     *            given advisor for ProxyFactory.
     * @return the proxy object.
     */
    protected static Object createProxy(Collection<?> collection, IntroductionAdvisor advisor)
    {
        ProxyFactory pf = new ProxyFactory(collection);
        pf.addAdvisor(advisor);
        if (pf.isInterfaceProxied(List.class) && pf.isInterfaceProxied(Deque.class))
        {
            pf.removeInterface(Deque.class);
        }
        return pf.getProxy();
    }
}