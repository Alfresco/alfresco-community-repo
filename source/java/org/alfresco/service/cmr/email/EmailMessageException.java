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
package org.alfresco.service.cmr.email;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A checked and handled exception indicating a specific and well-known
 * email error condition.
 * 
 * @since 2.2
 */
public class EmailMessageException extends RuntimeException
{
    private static final long serialVersionUID = 5039365329619219256L;

    /**
     * @param message       exception message.
     * @param params        message arguments for I18N
     * 
     * @see I18NUtil#getMessage(String, Object[])
     */
    public EmailMessageException(String message, Object ... params)
    {
        super(I18NUtil.getMessage(message, params));
    }
}
