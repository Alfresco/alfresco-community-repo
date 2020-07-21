/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.management.subsystems;

import java.io.IOException;
import java.util.Arrays;
import org.alfresco.util.PortUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * "Early" checker for Port property value (checks for "null/empty", "out-of-bounds", "unable-to-parse", "already-in-use" problems).
 * Also see the implemented interface {@link SubsystemEarlyPropertyChecker}
 * 
 * @author abalmus
 */
public class PortEarlyPropertyChecker implements SubsystemEarlyPropertyChecker
{
    private static final String PORT_CANT_BE_EMPTY_MESSAGE = "system.portcheck.err.empty_port";
    private static final String UNABLE_TO_PARSE_PORT_MESSAGE = "system.portcheck.err.parse_port";
    private static final String PORT_OUT_OF_BOUNDS_MESSAGE = "system.portcheck.err.port_out_of_bounds";
    private static final String PORTS_OUT_OF_BOUNDS_MESSAGE = "system.portcheck.err.ports_out_of_bounds";
    private static final String PORT_IN_USE_MESSAGE = "system.portcheck.err.port_in_use";
    private static final String HOST_PORT_IN_USE_MESSAGE = "system.portcheck.err.host_port_in_use";
    private static final String PORTS_IN_USE_MESSAGE = "system.portcheck.err.ports_in_use";
    private static final String UNKNOWN_OR_WRONG_HOST_MESSAGE = "system.portcheck.err.unknown_or_wrong_host";
    private static final String NETWORKING_ERROR_MESSAGE = "system.portcheck.err.networking_error";
    private static final String PORTS_WITH_NETWORKING_ERRORS_MESSAGE = "system.portcheck.err.ports_with_networking_errors";
    
    private static final Log logger = LogFactory.getLog(PortEarlyPropertyChecker.class);

    private final String subsystemName;
    private final String requiredPairedPropertyName;
    
    private final boolean hasMultiplePorts;
    private final boolean shouldCheckForBlockedPort;

    /**
     * Create a new {@link PortEarlyPropertyChecker} w/o a paired property name.
     * @param subsystemName Name of the subsystem; used for custom error messages.
     * @param hasMultiplePorts Specify if the property value that will be checked is a list of ports (they must be separated by ",").
     * @param shouldCheckForBlockedPort Enable/disable checking for port-already-in-use (i.e.: disable this for remote ports).
     */
    public PortEarlyPropertyChecker(String subsystemName, boolean hasMultiplePorts, boolean shouldCheckForBlockedPort)
    {
        this(subsystemName, null, hasMultiplePorts, shouldCheckForBlockedPort);
    }
    
    /**
     * Create a new {@link PortEarlyPropertyChecker}.
     * @param subsystemName Name of the subsystem; used for custom error messages.
     * @param requiredPairedPropertyName Name of the required paired property (see {@link SubsystemEarlyPropertyChecker#getPairedPropertyName()}).
     * @param hasMultiplePorts Specify if the property value that will be checked is a list of ports (they must be separated by ",").
     * @param shouldCheckForBlockedPort Enable/disable checking for port-already-in-use (i.e.: disable this for remote ports).
     */
    public PortEarlyPropertyChecker(String subsystemName, String requiredPairedPropertyName, boolean hasMultiplePorts, boolean shouldCheckForBlockedPort)
    {
        this.subsystemName = subsystemName;
        this.requiredPairedPropertyName = requiredPairedPropertyName;
        this.hasMultiplePorts = hasMultiplePorts;
        this.shouldCheckForBlockedPort = shouldCheckForBlockedPort;
    }

    /**
     * Implementation of checkPropertyValue() method for port checking.
     * @param propertyName Name of the property.
     * @param propertyValue Port value; if this contains multiple ports they must be separated by ",".
     * @param pairedPropertyValue - Value of the paired property
     * @throws InvalidPropertyValueException Raised if any of the checks fail.
     */
    @Override
    public void checkPropertyValue(String propertyName, String propertyValue, String pairedPropertyValue) throws InvalidPropertyValueException
    {
        if (propertyValue == null || propertyValue.isEmpty())
        {
            createLogAndThrowAnInvalidPropertyValueException(PORT_CANT_BE_EMPTY_MESSAGE, new String[] { subsystemName });
        }

        String host = pairedPropertyValue;

        try
        {
            if (!hasMultiplePorts)
            {
                int portNumber = Integer.parseInt(propertyValue.trim());

                if (portNumber < 1 || portNumber > 65535)
                {
                    createLogAndThrowAnInvalidPropertyValueException(PORT_OUT_OF_BOUNDS_MESSAGE, new String[] { subsystemName, "" + portNumber });
                }
                else if (shouldCheckForBlockedPort)
                {
                    try
                    {
                        checkPort(portNumber, host);
                    }
                    catch (IOException ioe)
                    {
                        if (ioe instanceof java.net.BindException)
                        {
                            if (host == null || "0.0.0.0".equals(host))
                            {
                                createLogAndThrowAnInvalidPropertyValueException(PORT_IN_USE_MESSAGE, new String[] { subsystemName, "" + portNumber });
                            }
                            else
                            {
                                createLogAndThrowAnInvalidPropertyValueException(HOST_PORT_IN_USE_MESSAGE, new String[] { subsystemName, host,
                                        "" + portNumber });
                            }
                        }
                        else if (host != null && ioe instanceof java.net.UnknownHostException)
                        {
                            createLogAndThrowAnInvalidPropertyValueException(UNKNOWN_OR_WRONG_HOST_MESSAGE, new String[] { subsystemName, "" + host });
                        }
                        else
                        {
                            createLogAndThrowAnInvalidPropertyValueException(NETWORKING_ERROR_MESSAGE,
                                    new String[] { subsystemName, ioe.getLocalizedMessage() });
                        }
                    }
                }
            }
            else
            {
                String[] ports = propertyValue.trim().split(",");

                String portsInUse = "";
                String portsOutOfBounds = "";
                String portsWithNetworkingErrors = "";

                for (String portStr : ports)
                {
                    int portNumber = Integer.parseInt(portStr.trim());

                    if (portNumber < 1 || portNumber > 65535)
                    {
                        portsOutOfBounds = appendToErrorString(portsOutOfBounds, ", ", "" + portNumber);
                    }
                    else if (shouldCheckForBlockedPort)
                    {
                        try
                        {
                            checkPort(portNumber, host);
                        }
                        catch (IOException ioe)
                        {
                            if (ioe instanceof java.net.BindException)
                            {
                                portsInUse = appendToErrorString(portsInUse, ", ", "" + portNumber);
                            }
                            else
                            {
                                portsWithNetworkingErrors = appendToErrorString(portsWithNetworkingErrors, " ; ",
                                        portNumber + ": " + ioe.getLocalizedMessage());
                            }
                        }
                    }
                }

                String completeErrorDisplayMessage = "";

                if (!portsOutOfBounds.equals(""))
                {
                    String portsOutOfBoundsDisplayMessage = resolveMessage(PORTS_OUT_OF_BOUNDS_MESSAGE, new String[] { subsystemName,
                            portsOutOfBounds });

                    completeErrorDisplayMessage += portsOutOfBoundsDisplayMessage;
                }

                if (!portsInUse.equals(""))
                {
                    String portsInUseDisplayMessage = resolveMessage(PORTS_IN_USE_MESSAGE, new String[] { subsystemName, portsInUse });

                    completeErrorDisplayMessage = appendToErrorString(completeErrorDisplayMessage, " | ", portsInUseDisplayMessage);
                }

                if (!portsWithNetworkingErrors.equals(""))
                {
                    String portsWithNetworkingErrorsDisplayMessage = resolveMessage(PORTS_WITH_NETWORKING_ERRORS_MESSAGE, new String[] {
                            subsystemName, portsWithNetworkingErrors });

                    completeErrorDisplayMessage = appendToErrorString(completeErrorDisplayMessage, " | ", portsWithNetworkingErrorsDisplayMessage);
                }

                if (!completeErrorDisplayMessage.equals(""))
                {
                    if (logger.isErrorEnabled())
                    {
                        logger.error(completeErrorDisplayMessage);
                    }

                    throw new InvalidPropertyValueException(completeErrorDisplayMessage);
                }
            }
        }
        catch (NumberFormatException nfe)
        {
            createLogAndThrowAnInvalidPropertyValueException(UNABLE_TO_PARSE_PORT_MESSAGE, new String[] { subsystemName, propertyValue });
        }
    }

    protected void checkPort(int portNumber, String host) throws IOException
    {
        PortUtil.checkPort(portNumber, host);
    }

    private String appendToErrorString(String stringToAppendTo, String separator, String valueToAppend)
    {
        if (!stringToAppendTo.equals(""))
        {
            stringToAppendTo = stringToAppendTo + separator;
        }

        stringToAppendTo = stringToAppendTo + valueToAppend;

        return stringToAppendTo;
    }

    private void createLogAndThrowAnInvalidPropertyValueException(String message, String[] messageParams)
    {
        InvalidPropertyValueException invalidPropertyValueException = new InvalidPropertyValueException(message, messageParams);

        if (logger.isErrorEnabled())
        {
            logger.error(invalidPropertyValueException.getLocalizedMessage());
        }

        throw invalidPropertyValueException;
    }

    private String resolveMessage(String messageId, Object[] params)
    {
        String message = I18NUtil.getMessage(messageId, params);

        if (message == null)
        {
            message = messageId;

            if (params != null)
            {
                message += " - " + Arrays.toString(params);
            }
        }

        return message;
    }

    @Override
    public String getPairedPropertyName()
    {
        return requiredPairedPropertyName;
    }
}
