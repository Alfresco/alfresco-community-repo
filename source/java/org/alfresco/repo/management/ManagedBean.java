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
package org.alfresco.repo.management;

import org.springframework.beans.factory.DisposableBean;

/**
 * An interface for beans that can be reconfigured using an administration UI or JMX console. A bean in use must be
 * 'stopped' before it can be configured with {@link #destroy}. After reconfiguration, it can then be put back into use
 * with {@link #onStart} and if this is successful, further tests can be carried out with {@link #onTest}.
 * 
 * @author dward
 */
public interface ManagedBean extends DisposableBean
{
    /**
     * Puts the bean into use after its properties have been set.
     */
    public void onStart();

    /**
     * Carries out tests on the bean after it has been started.
     */
    public void onTest();
}
