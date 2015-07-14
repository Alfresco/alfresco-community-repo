/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.management.subsystems;

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
    private static final String PORTS_IN_USE_MESSAGE = "system.portcheck.err.ports_in_use";

    private static final Log logger = LogFactory.getLog(PortEarlyPropertyChecker.class);

    private final String subsystemName;
    private final boolean hasMultiplePorts;
    private final boolean shouldCheckForBlockedPort;

    /**
     * Create a new {@link PortEarlyPropertyChecker}.
     * @param subsystemName Name of the subsystem; used for custom error messages.
     * @param hasMultiplePorts Specify if the property value that will be checked is a list of ports (they must be separated by ",").
     * @param shouldCheckForBlockedPort Enable/disable checking for port-already-in-use (i.e.: disable this for remote ports).
     */
    public PortEarlyPropertyChecker(String subsystemName, boolean hasMultiplePorts, boolean shouldCheckForBlockedPort)
    {
        this.subsystemName = subsystemName;
        this.hasMultiplePorts = hasMultiplePorts;
        this.shouldCheckForBlockedPort = shouldCheckForBlockedPort;
    }

    /**
     * Implementation of checkPropertyValue() method for port checking.
     * @param propertyName Name of the property.
     * @param propertyValue Port value; if this contains multiple ports they must be separated by ",".
     * @throws InvalidPropertyValueException Raised if any of the checks fail.
     */
    @Override
    public void checkPropertyValue(String propertyName, String propertyValue) throws InvalidPropertyValueException
    {
        if (propertyValue == null || propertyValue.isEmpty())
        {
            throw new InvalidPropertyValueException(PORT_CANT_BE_EMPTY_MESSAGE, new String[] { subsystemName });
        }

        try
        {
            if (!hasMultiplePorts)
            {
                int portNumber = Integer.parseInt(propertyValue.trim());

                if (portNumber < 1 || portNumber > 65535)
                {
                    InvalidPropertyValueException portOutOfBoundsException = 
                        new InvalidPropertyValueException(PORT_OUT_OF_BOUNDS_MESSAGE, new String[] { subsystemName, "" + portNumber });

                    if (logger.isErrorEnabled())
                    {
                        logger.error(portOutOfBoundsException.getLocalizedMessage());
                    }

                    throw portOutOfBoundsException;
                }
                else if (shouldCheckForBlockedPort && !PortUtil.isPortFree(portNumber))
                {
                    InvalidPropertyValueException portInUseException = 
                        new InvalidPropertyValueException(PORT_IN_USE_MESSAGE, new String[] { subsystemName, "" + portNumber });

                    if (logger.isErrorEnabled())
                    {
                        logger.error(portInUseException.getLocalizedMessage());
                    }

                    throw portInUseException;
                }
            }
            else
            {
                String[] ports = propertyValue.trim().split(",");

                String portsInUse = "";
                String portsOutOfBounds = "";

                for (String portStr : ports)
                {
                    int portNumber = Integer.parseInt(portStr.trim());

                    if (portNumber < 1 || portNumber > 65535)
                    {
                        if (!portsOutOfBounds.equals(""))
                        {
                            portsOutOfBounds += ", ";
                        }

                        portsOutOfBounds += portNumber;
                    }
                    else if (shouldCheckForBlockedPort && !PortUtil.isPortFree(portNumber))
                    {
                        if (!portsInUse.equals(""))
                        {
                            portsInUse += ", ";
                        }

                        portsInUse += portNumber;
                    }
                }

                String completeErrorDisplayMessage = "";

                if (!portsOutOfBounds.equals(""))
                {
                    String portsOutOfBoundsDisplayMessage = 
                        resolveMessage(PORTS_OUT_OF_BOUNDS_MESSAGE, new String[] { subsystemName, portsOutOfBounds });

                    completeErrorDisplayMessage += portsOutOfBoundsDisplayMessage;
                }

                if (!portsInUse.equals(""))
                {
                    String portsInUseDisplayMessage = resolveMessage(PORTS_IN_USE_MESSAGE, new String[] { subsystemName, portsInUse });

                    if (!completeErrorDisplayMessage.equals(""))
                    {
                        completeErrorDisplayMessage += " | ";
                    }

                    completeErrorDisplayMessage += portsInUseDisplayMessage;
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
            InvalidPropertyValueException unableToParseException = 
                new InvalidPropertyValueException(UNABLE_TO_PARSE_PORT_MESSAGE, new String[] { subsystemName, propertyValue });

            if (logger.isErrorEnabled())
            {
                logger.error(unableToParseException.getLocalizedMessage());
            }

            throw unableToParseException;
        }
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
}
