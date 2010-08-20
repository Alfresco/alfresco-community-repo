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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.control;

import java.sql.Savepoint;

/**
 * Abstract implementation for connection controlling DAO.
 * <p>
 * Provides any basic logic.
 * 
 * @author Derek Hulley
 * @since 3.2SP1
 */
public abstract class AbstractControlDAOImpl implements ControlDAO
{
    /**
     * @return              Returns <tt>null</tt> by default i.e. not supported
     */
    @Override
    public Savepoint createSavepoint(String savepoint)
    {
        return null;
    }

    /** No-op */
    @Override
    public void rollbackToSavepoint(Savepoint savepoint)
    {
    }

    /** No-op */
    @Override
    public void releaseSavepoint(Savepoint savepoint)
    {
    }

    @Override
    public int setTransactionIsolationLevel(int isolationLevel)
    {
        throw new UnsupportedOperationException("Method not implemented by the DAO");
    }
}
