/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.search;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.http.HttpStatus;

import java.util.List;

/**
 * @author Andy
 *
 */
public class QueryParserException extends AlfrescoRuntimeException
{
    /** Serial version UUID. */
    private static final long serialVersionUID = 4886993838297301968L;
    /** Http Status Code that should be returned by Remote API. */
    private int httpStatusCode;

    /**
     * @param msgId
     */
    public QueryParserException(String msgId)
    {
        super(msgId);
    }

    /**
     * @param msgId
     * @param msgParams
     */
    public QueryParserException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * @param msgId
     * @param cause
     */
    public QueryParserException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * @param msgId
     * @param msgParams
     * @param cause
     */
    public QueryParserException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    /**
     * Constructor for exception that allows setting an HTTP status code.
     *
     * @param msgId Message for the exception
     * @param httpStatusCode Status code to return for exception
     */
    public QueryParserException(String msgId, int httpStatusCode)
    {
        super(msgId);
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode()
    {
        return httpStatusCode;
    }
}