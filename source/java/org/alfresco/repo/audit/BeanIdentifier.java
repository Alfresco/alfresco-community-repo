/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import org.aopalliance.intercept.MethodInvocation;

/**
 * Identify a bean upon which a method invocation has been made. Originally
 * this was only public services but has been relaxed to be any bean.
 *  
 * @author Andy Hind, David Ward, Alan Davis
 */
public interface BeanIdentifier
{
    /**
     * Get the name of the bean (normally a service) for the method invocation.
     * 
     * @param mi                the method invocation 
     * @return                  Returns the name of the bean or <tt>null</tt> if it is
     *                          not recognized
     */
    public String getBeanName(MethodInvocation mi);
}
