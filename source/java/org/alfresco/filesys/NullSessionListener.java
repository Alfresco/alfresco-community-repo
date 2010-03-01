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
package org.alfresco.filesys;

import org.alfresco.jlan.server.SessionListener;
import org.alfresco.jlan.server.SrvSession;

/**
 * A benign implementation of the SessionListener interface. Allows the authentication subsystems to share a uniform
 * interface. Those without a need for a live session listeners can use this bean.
 * 
 * @author dward
 */
public class NullSessionListener implements SessionListener
{

    public void sessionClosed(SrvSession sess)
    {
    }

    public void sessionCreated(SrvSession sess)
    {
    }

    public void sessionLoggedOn(SrvSession sess)
    {
    }

}
