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
package org.alfresco.cmis;

/**
 * A base class for all exceptions that map to CMIS SOAP faults / HTTP error codes. Intentionally a checked
 * non-RuntimeException so that the faults codes are propagated and signalled correctly.
 * 
 * @author dward
 */
public abstract class CMISServiceException extends Exception
{
    private static final long serialVersionUID = 8220732267294509499L;

    /** The fault name, as per the CMIS spec. */
    private final String faultName;

    /** The status code, as per the CMIS REST binding. */
    private final int statusCode;

    /**
     * Instantiates a new CMIS Service exception.
     * 
     * @param message
     *            the message
     * @param faultName
     *            the fault name, as per the CMIS spec
     * @param statusCode
     *            the status code, as per the CMIS REST binding
     */
    public CMISServiceException(String message, String faultName, int statusCode)
    {
        super(message);
        this.faultName = faultName;
        this.statusCode = statusCode;
    }

    /**
     * Instantiates a new CMIS Service exception.
     * 
     * @param cause
     *            the cause
     * @param faultName
     *            the fault name, as per the CMIS spec
     * @param statusCode
     *            the status code, as per the CMIS REST binding
     */
    public CMISServiceException(Throwable cause, String faultName, int errorCode)
    {
        this(cause.getMessage(), cause, faultName, errorCode);
    }

    /**
     * Instantiates a new CMIS Service exception.
     * 
     * @param message
     *            the message
     * @param cause
     *            the cause
     * @param faultName
     *            the fault name, as per the CMIS spec
     * @param statusCode
     *            the status code, as per the CMIS REST binding
     */
    public CMISServiceException(String message, Throwable cause, String faultName, int errorCode)
    {
        super(message, cause);
        this.faultName = faultName;
        this.statusCode = errorCode;
    }

    /**
     * Gets the fault name.
     * 
     * @return the fault name
     */
    public String getFaultName()
    {
        return faultName;
    }

    /**
     * Gets the status code.
     * 
     * @return the status code
     */
    public int getStatusCode()
    {
        return this.statusCode;
    }
}
