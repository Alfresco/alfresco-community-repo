/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
