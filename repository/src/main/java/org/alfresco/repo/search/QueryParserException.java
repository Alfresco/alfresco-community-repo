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

    /**
     *
     */
    private static final long serialVersionUID = 4886993838297301968L;

    // List of SOLR Exceptions that should be returning HTTP 501 status code in Remote API
    public static final List<String> STATUS_CODE_501_EXCEPTIONS =
        List.of("java.lang.UnsupportedOperationException");
    // Http Status Code that should be returned by Remote API
    private int httpStatusCode;

    /**
     * @param msgId
     */
    public QueryParserException(String msgId)
    {
        super(msgId);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param msgId
     * @param msgParams
     */
    public QueryParserException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param msgId
     * @param cause
     */
    public QueryParserException(String msgId, Throwable cause)
    {
        super(msgId, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param msgId
     * @param msgParams
     * @param cause
     */
    public QueryParserException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * Extracts exception class from trace string in order to map not implemented features
     * to Http Status Code 501
     * @param msgId Message for the exception
     * @param httpStatusCode Original Http Status Code returned by SOLR
     * @param trace Original stack trace returned by SOLR in string format
     */
    public QueryParserException(String msgId, int httpStatusCode, String trace)
    {
        super(msgId);
        this.httpStatusCode = httpStatusCode;
        String traceException = trace.substring(0, trace.indexOf(":")).trim();
        if (STATUS_CODE_501_EXCEPTIONS.contains(traceException)) {
            this.httpStatusCode = HttpStatus.SC_NOT_IMPLEMENTED;
        }
    }

    public int getHttpStatusCode()
    {
        return httpStatusCode;
    }

}