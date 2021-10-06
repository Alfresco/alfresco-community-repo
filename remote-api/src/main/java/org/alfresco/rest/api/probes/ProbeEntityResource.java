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

import org.alfresco.repo.admin.RepoHealthChecker;
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
@EntityResource(name = "probes", title = "Probes") public class ProbeEntityResource
            implements EntityResourceAction.ReadById<Probe>
{
    public static final long CHECK_PERIOD = 10 * 1000; // Maximum of only one checkResult every 10 seconds.

    protected static Log logger = LogFactory.getLog(ProbeEntityResource.class);
    private final Object lock = new Object();
    private final Probe liveProbe = new Probe("liveProbe: Success - Tested");
    private long lastCheckTime = 0;
    private Boolean checkResult;
    private DiscoveryApiWebscript discovery;

    private RepoHealthChecker repoHealthChecker;

    public DiscoveryApiWebscript setDiscovery(DiscoveryApiWebscript discovery)
    {
        DiscoveryApiWebscript result = this.discovery;
        this.discovery = discovery;
        return result;
    }

    public void setRepoHealthChecker(RepoHealthChecker repoHealthChecker)
    {
        this.repoHealthChecker = repoHealthChecker;
    }

    /**
     * Returns a status code of 200 for okay. The probe contains little information for security reasons.
     * Note: does *not* require authenticated access, so limits the amount of work performed to avoid a DDOS.
     */
    @Override @WebApiDescription(title = "Get probe status", description = "Returns 200 if valid") @WebApiParam(name = "probeName", title = "The probe's name") @WebApiNoAuth public Probe readById(
                  String name, Parameters parameters)
    {
        switch (ProbeType.valueOf(name.toUpperCase()))
        {
            case READY:
                String message = doReadyCheck();
                return new Probe(message);
            case LIVE:
                return liveProbe;
            default:
                throw new InvalidArgumentException("Bad probe name: " + name);
        }
    }

    // We don't want to be doing checks all the time or holding monitors for a long time to avoid a DDOS.
    public String doReadyCheck()
    {
        long now;
        boolean result = false;
        String message = "No test";
        boolean logInfo = false;
        synchronized (lock)
        {

            now = System.currentTimeMillis();

            if (checkResult == null || isAfterCheckPeriod(now))
            {
                try
                {
                    message = "Tested";
                    performReadinessCheck();
                    result = true;
                }
                catch (Exception e)
                {
                    result = false;
                    logger.debug(e);
                }
                finally
                {

                    checkResult = result;
                    setLastCheckTime(now);
                    logInfo = true;

                }
            }
            else
            {
                // if no check is performed, use previous check result
                result = checkResult;

            }
        }

        message = getMessage(result, message);

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

    private String getMessage(boolean result, String message)
    {

        return "readyProbe: " + (result ? "Success" : "Failure") + " - " + message;


    }

    private void performReadinessCheck()
    {

        discovery.getRepositoryInfo();
        repoHealthChecker.checkDatabase();
        if(logger.isDebugEnabled())
        {
            logger.debug("All checks complete");
        }

    }

    private void setLastCheckTime(long time)
    {
        this.lastCheckTime = time;
        long nextCheckTime = lastCheckTime + CHECK_PERIOD;
        if (logger.isTraceEnabled())
        {
            logger.trace("nextCheckTime: " + nextCheckTime + " (+" + ((nextCheckTime) / 1000) + " secs)");
        }
    }

    private boolean isAfterCheckPeriod(long currentTime)
    {
        return ((currentTime - lastCheckTime) >= CHECK_PERIOD);
    }

    public enum ProbeType
    {
        LIVE("-live-"),READY("-ready");
        ProbeType(String strValue)
        {
            value = strValue;
        }
        String value;

        public String getValue()
        {
            return value;
        }
    }

}
