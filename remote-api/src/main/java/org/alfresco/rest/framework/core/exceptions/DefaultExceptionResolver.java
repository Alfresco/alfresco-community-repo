/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.framework.core.exceptions;

import javax.servlet.http.HttpServletResponse;

/**
 * Default exception resolver for cases when no other exception resolver will do.
 *
 * @author Gethin James
 */
public class DefaultExceptionResolver implements ExceptionResolver<Exception>
{
    public static final String STACK_MESSAGE_ID = "framework.no.stacktrace";
    public static final String ERROR_URL = "https://api-explorer.alfresco.com";
    public static final String DEFAULT_MESSAGE_ID = "framework.exception.ApiDefault";
    
    @Override
    public ErrorResponse resolveException(Exception ex)
    {
        return new ErrorResponse(DEFAULT_MESSAGE_ID, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), ex.getStackTrace(), null);
    }

}
