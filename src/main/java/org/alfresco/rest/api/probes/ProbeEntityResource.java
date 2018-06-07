/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.rest.api.probes;

import org.alfresco.rest.api.discovery.DiscoveryApiWebscript;
import org.alfresco.rest.api.model.Probe;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiNoAuth;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.ServiceUnavailableException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of an Entity Resource for Probes.
 */
@EntityResource(name="probes", title = "Probes")
public class ProbeEntityResource implements EntityResourceAction.ReadById<Probe>
{
    public static final String LIVE = "-live-";
    public static final String READY = "-ready-";

    public static final long CHECK_PERIOD = 10 * 1000; // Maximum of only one checkResult every 10 seconds.

    protected static Log logger = LogFactory.getLog(ProbeEntityResource.class);;

    private long nextCheckTime = 0;
    private Boolean checkResult;
    private Boolean checking = false;
    private boolean readySent;

    private DiscoveryApiWebscript discovery;

    public DiscoveryApiWebscript setDiscovery(DiscoveryApiWebscript discovery)
    {
        DiscoveryApiWebscript result = this.discovery;
        this.discovery = discovery;
        return result;
    }

    /**
     * Returns a status code of 200 for okay. The probe contains little information for security reasons.
     *
     * Note: does *not* require authenticated access, so limits the amount of work performed to avoid a DDOS.
     */
    @Override
    @WebApiDescription(title="Get probe status", description = "Returns 200 if valid")
    @WebApiParam(name = "probeName", title = "The probe's name")
    @WebApiNoAuth
    public Probe readById(String name, Parameters parameters)
    {
        boolean isLiveProbe = LIVE.equalsIgnoreCase(name);
        if (!isLiveProbe && !READY.equalsIgnoreCase(name))
        {
            throw new InvalidArgumentException("Bad probe name");
        }

        String message = doCheckOrNothing(isLiveProbe);
        return new Probe(message);
    }

    // We don't want to be doing checks all the time or holding monitors for a long time to avoid a DDOS.
    public String doCheckOrNothing(boolean isLiveProbe)
    {
        boolean doCheck = false;
        long now = 0;
        boolean result;
        String message = "No test";
        boolean logInfo = false;
        synchronized(checking)
        {
            // Initially ready needs to be false so we don't get requests and live true so the pod is not killed.
            if (checkResult == null)
            {
                result = isLiveProbe;
            }
            else
            {
                result = checkResult;
            }

            if (checking) // Is another thread is checking?
            {
                if (!readySent && result && !isLiveProbe)
                {
                    readySent = true;
                    logInfo = true;
                }
            }
            else // This thread will do a check
            {
                now = System.currentTimeMillis();
                if (checkResult == null || nextCheckTime <= now)
                {
                    doCheck = true;
                    checking = true;
                }
            }
        }

        if (doCheck)
        {
            try
            {
                message = "Tested";
                doCheck(isLiveProbe);
                result = true;
            }
            catch (Exception e)
            {
                result = false;
            }
            finally
            {
                synchronized (checking)
                {
                    checking = false;
                    checkResult = result;
                    setNextCheckTime(now);
                    if (result && !readySent && !isLiveProbe) // Are we initially ready
                    {
                        readySent = true;
                        logInfo = true;
                    }
                    else if (!result && (isLiveProbe || readySent)) // Are we sick
                    {
                        logInfo = true;
                    }
                }
            }
        }

        message = getMessage(isLiveProbe, result, message);

        if (logInfo)
        {
            logger.info(message);
        }
        else
        {
            logger.debug(message);
        }

        if (result)
        {
            return message;
        }
        throw new ServiceUnavailableException(message);
    }

    private String getMessage(boolean isLiveProbe, boolean result, String message)
    {
        return (isLiveProbe ? "liveProbe" : "readyProbe")+": "+
        (result ? "Success" : "Failure") +
        " - "+message;
    }

    private void doCheck(boolean isLiveProbe)
    {
        discovery.getRepositoryInfo();
    }

    private void setNextCheckTime(long now)
    {
        long oldValue = nextCheckTime;
        if (nextCheckTime == 0)
        {
            nextCheckTime = (now / 60000) * 60000;
        }

        do
        {
            nextCheckTime += CHECK_PERIOD;
        }
        while (nextCheckTime <= now);

        if (logger.isTraceEnabled())
        {
            logger.trace("nextCheckTime: " + nextCheckTime + " (+" + ((nextCheckTime - oldValue) / 1000) + " secs)");
        }
    }
}
