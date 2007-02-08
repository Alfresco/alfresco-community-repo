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
package org.alfresco.service.cmr.version;

import java.text.MessageFormat;

/**
 * @author Roy Wetherall
 */
public class ReservedVersionNameException extends RuntimeException
{
    /**
     * Serial verison UID
     */
    private static final long serialVersionUID = 3690478030330015795L;

    /**
     * Error message
     */
    private static final String MESSAGE = "The version property name {0} clashes with a reserved verison property name.";
    
    /**
     * Constructor
     * 
     * @param propertyName  the name of the property that clashes with
     *                      a reserved property name
     */
    public ReservedVersionNameException(String propertyName)
    {
        super(MessageFormat.format(MESSAGE, new Object[]{propertyName}));
    }
}
