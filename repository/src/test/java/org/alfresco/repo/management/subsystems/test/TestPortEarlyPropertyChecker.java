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
package org.alfresco.repo.management.subsystems.test;

import java.io.IOException;
import org.alfresco.repo.management.subsystems.PortEarlyPropertyChecker;

public class TestPortEarlyPropertyChecker extends PortEarlyPropertyChecker
{
    private int blockedPort = -1;
    private String wrongHost = "";

    public TestPortEarlyPropertyChecker(String subsystemName, boolean hasMultiplePorts,
            boolean shouldCheckForBlockedPort)
    {
        super(subsystemName, hasMultiplePorts, shouldCheckForBlockedPort);
    }

    public TestPortEarlyPropertyChecker(String subsystemName, String requiredPairedPropertyName,
            boolean hasMultiplePorts, boolean shouldCheckForBlockedPort)
    {
        super(subsystemName, requiredPairedPropertyName, hasMultiplePorts,
                shouldCheckForBlockedPort);
    }

    public void setBlockedPort(int blockedPort)
    {
        this.blockedPort = blockedPort;
    }

    public void setWrongHost(String wrongHost)
    {
        this.wrongHost = wrongHost;
    }

    @Override
    public void checkPort(int portNumber, String host) throws IOException
    {
        if (portNumber == blockedPort)
        {
            throw new java.net.BindException();
        }

        if (wrongHost.equals(host))
        {
            throw new java.net.UnknownHostException();
        }
    }
}
