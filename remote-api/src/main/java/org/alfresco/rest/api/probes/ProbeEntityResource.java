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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An implementation of an Entity Resource for Probes. */
@EntityResource(name = "probes", title = "Probes")
public class ProbeEntityResource implements EntityResourceAction.ReadById<Probe> {
    public static final long CHECK_PERIOD =
            10 * 1000; // Maximum of only one checkResult every 10 seconds.

    private static final Logger logger = LoggerFactory.getLogger(ProbeEntityResource.class);
    private final Object lock = new Object();
    private final Probe liveProbe = new Probe("liveProbe: Success - Tested");
    private long lastCheckTime = 0;
    private Boolean checkResult;
    private DiscoveryApiWebscript discovery;

    private RepoHealthChecker repoHealthChecker;

    public DiscoveryApiWebscript setDiscovery(DiscoveryApiWebscript discovery) {
        DiscoveryApiWebscript result = this.discovery;
        this.discovery = discovery;
        return result;
    }

    public void setRepoHealthChecker(RepoHealthChecker repoHealthChecker) {
        this.repoHealthChecker = repoHealthChecker;
    }

    /**
     * Returns a status code of 200 for okay. The probe contains little information for security
     * reasons. Note: does *not* require authenticated access, so limits the amount of work
     * performed to avoid a DDOS.
     */
    @Override
    @WebApiDescription(title = "Get probe status", description = "Returns 200 if valid")
    @WebApiParam(name = "probeName", title = "The probe's name")
    @WebApiNoAuth
    public Probe readById(String name, Parameters parameters) {
        ProbeType probeType = ProbeType.fromString(name);
        Probe probeResponse = null;

        switch (probeType) {
            case LIVE:
                probeResponse = liveProbe;
                break;
            case READY:
                String message = doReadyCheck();
                probeResponse = new Probe(message);
                break;
            case UNKNOWN:
                throw new InvalidArgumentException("Bad probe name");
        }

        return probeResponse;
    }

    // We don't want to be doing checks all the time or holding monitors for a long time to avoid a
    // DDOS.
    public String doReadyCheck() {

        synchronized (lock) {
            String message;
            long now = System.currentTimeMillis();

            if (checkResult == null || isAfterCheckPeriod(now)) {
                try {
                    performReadinessCheck();
                    checkResult = true;
                } catch (Exception e) {
                    checkResult = false;
                    logger.debug("Exception during readiness check", e);
                } finally {

                    setLastCheckTime(now);
                    message = getMessage(checkResult, "Tested");
                    logger.info(message);
                }
            } else {
                // if no check is performed, use previous check result
                message = getMessage(checkResult, "No test");
                logger.debug(message);
            }
            if (checkResult) {
                return message;
            }

            throw new ServiceUnavailableException(message);
        }
    }

    private String getMessage(boolean result, String message) {

        return "readyProbe: " + (result ? "Success" : "Failure") + " - " + message;
    }

    private void performReadinessCheck() {

        discovery.getRepositoryInfo();
        repoHealthChecker.checkDatabase();
        logger.debug("All checks complete");
    }

    private void setLastCheckTime(long time) {
        this.lastCheckTime = time;
        long nextCheckTime = lastCheckTime + CHECK_PERIOD;

        logger.trace("nextCheckTime: {} (+{} secs)", nextCheckTime, ((CHECK_PERIOD) / 1000));
    }

    private boolean isAfterCheckPeriod(long currentTime) {
        return ((currentTime - lastCheckTime) >= CHECK_PERIOD);
    }

    public enum ProbeType {
        LIVE("-live-"),
        READY("-ready-"),
        UNKNOWN("");

        String value;

        ProbeType(String strValue) {
            value = strValue;
        }

        public static ProbeType fromString(String text) {
            for (ProbeType p : ProbeType.values()) {
                if (p.value.equalsIgnoreCase(text)) {
                    return p;
                }
            }
            return UNKNOWN;
        }

        public String getValue() {
            return value;
        }
    }
}
