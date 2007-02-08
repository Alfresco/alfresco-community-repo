/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.security.person;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * All exceptions related to the person service.
 * 
 * @author Andy Hind
 */
public class PersonException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2802163127696444600L;

    public PersonException(String msgId)
    {
        super(msgId);
    }

    public PersonException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public PersonException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public PersonException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
