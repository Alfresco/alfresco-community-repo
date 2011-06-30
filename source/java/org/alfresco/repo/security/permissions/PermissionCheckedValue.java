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
package org.alfresco.repo.security.permissions;

import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * Marker interface for objects that have already passed permission checking.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public interface PermissionCheckedValue
{
    /**
     * Helper 'introduction' to allow simple addition of the {@link PermissionCheckedValue} interface to
     * existing objects.
     *
     * @author Derek Hulley
     * @since 4.0
     */
    @SuppressWarnings("serial")
    public static class PermissionCheckedValueMixin extends DelegatingIntroductionInterceptor implements PermissionCheckedValue
    {
        private PermissionCheckedValueMixin()
        {
            super();
        }
        /**
         * Helper method to create a {@link PermissionCheckedValue} from an existing <code>Object</code>.
         * 
         * @param collection        the <code>Object</code> to proxy
         * @return                  a <code>Object</code> of the same type but including the
         *                          {@link PermissionCheckedValue} interface
         */
        @SuppressWarnings("unchecked")
        public static final <T extends Object> T create(T object)
        {
            // Create the mixin
            DelegatingIntroductionInterceptor mixin = new PermissionCheckedValueMixin();
            // Create the advisor
            IntroductionAdvisor advisor = new DefaultIntroductionAdvisor(mixin, PermissionCheckedValue.class);
            // Proxy
            ProxyFactory pf = new ProxyFactory(object);
            pf.addAdvisor(advisor);
            Object proxiedObject = pf.getProxy();
            
            // Done
            return (T) proxiedObject;
        }
    }
}
