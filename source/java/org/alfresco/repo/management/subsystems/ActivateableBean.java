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
package org.alfresco.repo.management.subsystems;

/**
 * An interface to be implemented by beans that can be 'turned off' by some configuration setting. When such beans are
 * inactive, they will not perform any validation checks on initialization and will remain in a state where their
 * {@link #isActive()} method always returns <code>false</code>. {@link ChainingSubsystemProxyFactory} will ignore any
 * <code>ActivatableBean</code>s whose {@link #isActive()} method returns <code>false</code>. This allows certain
 * functions of a chained subsystem (e.g. CIFS authentication, SSO) to be targeted to specific members of the chain.
 * 
 * @author dward
 */
public interface ActivateableBean
{
    /**
     * Determines whether this bean is active.
     * 
     * @return <code>true</code> if this bean is active
     */
    public boolean isActive();
}
