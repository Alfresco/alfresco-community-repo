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
package org.alfresco.heartbeat;

import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.dictionary.CustomModelsInfo;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.usage.RepoUsageComponent;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.license.LicenseException;
import org.alfresco.service.license.LicenseService;
import org.alfresco.service.license.LicenseService.LicenseChangeHandler;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.traitextender.SpringExtensionBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.*;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.Base64;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class communicates some very basic repository statistics to Alfresco on a regular basis.
 * 
 * @author dward
 */
public class HeartBeat implements LicenseChangeHandler
{

    private static final String LAMBDA_INGEST_URL = "https://0s910f9ijc.execute-api.eu-west-1.amazonaws.com/Stage/ingest";

    /** The default enable state */
    private static final boolean DEFAULT_HEARTBEAT_ENABLED = true;

    /** The logger. */
    private static final Log logger = LogFactory.getLog(HeartBeat.class);

    private LicenseService licenseService;

    private Scheduler scheduler;

    /** URL to post heartbeat to. */
    private String heartBeatUrl;

    private boolean testMode = true;

    private final String JOB_NAME = "heartbeat";

    /** Is the heartbeat enabled */
    private boolean enabled = DEFAULT_HEARTBEAT_ENABLED;

    private HBDataCollectorService dataCollectorService;



    /**
     * Initialises the heart beat service. Note that dependencies are intentionally 'pulled' rather than injected
     * because we don't want these to be reconfigured.
     *
     * @param context
     *            the context
     */
    public HeartBeat(final ApplicationContext context)
    {
        this(context, true);
    }

    /**
     * Initialises the heart beat service, potentially in test mode. Note that dependencies are intentionally 'pulled'
     * rather than injected because we don't want these to be reconfigured.
     *
     * -@param context
     *            the context
     * -@param testMode
     *            are we running in test mode? If so we send data to local port 9999 rather than an alfresco server. We
     *            also use a special test encryption certificate and ping on a more frequent basis.
     */
    public HeartBeat(final ApplicationContext context, final Boolean testModel)
    {
        logger.debug("Initialising HeartBeat");


        // I think these should be wired by spring instead for proper ioc..
        this.dataCollectorService = (HBDataCollectorService) context.getBean("hbDataCollectorService");
        this.scheduler = (Scheduler) context.getBean("schedulerFactory");

        this.testMode = testModel;

        try
        {
            LicenseService licenseService = null;
            try
            {
                licenseService = (LicenseService) context.getBean("licenseService");
                licenseService.registerOnLicenseChange(this);
            }
            catch (NoSuchBeanDefinitionException e)
            {
                logger.error("licenseService not found", e);
            }
            this.licenseService = licenseService;

            // We force the job to be scheduled regardless of the potential state of the licenses
            scheduleJob();
        }
        catch (final RuntimeException e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private synchronized void setHeartBeatUrl(String heartBeatUrl)
    {
        this.heartBeatUrl = heartBeatUrl;
    }

    // Determine the URL to send the heartbeat to from the license if not set
    private synchronized String getHeartBeatUrl()
    {
        if (heartBeatUrl == null)
        {
            // GC: Ignore the standard heartbeat URL and always use the AWS/Lambda URL
//            LicenseDescriptor licenseDescriptor = licenseService.getLicense();
//            String url = (licenseDescriptor == null) ? null : licenseDescriptor.getHeartBeatUrl();
//            setHeartBeatUrl(url == null ? HeartBeat.DEFAULT_URL : url);
            setHeartBeatUrl(LAMBDA_INGEST_URL);
        }

        logger.debug("Returning heartBeatUrl: " + heartBeatUrl);

        return heartBeatUrl;
    }

    /**
     * @return          <tt>true</tt> if the heartbeat is currently enabled
     */
    public synchronized boolean isEnabled()
    {
        return enabled;
    }



    /**
     * Sends encrypted data over HTTP.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws GeneralSecurityException
     *             an encryption related exception
     */
    public void sendData() throws IOException, GeneralSecurityException
    {
        this.dataCollectorService.collectAndSendData();
    }

    /**
     * Listens for license changes.  If a license is change or removed, the heartbeat job is resheduled.
     */
    public synchronized void onLicenseChange(LicenseDescriptor licenseDescriptor)
    {
        logger.debug("Update license called");

        setHeartBeatUrl(licenseDescriptor.getHeartBeatUrl());
        boolean newEnabled = !licenseDescriptor.isHeartBeatDisabled();

        if (newEnabled != enabled)
        {
            logger.debug("State change of heartbeat");
            this.enabled = newEnabled;
            try
            {
                scheduleJob();
            }
            catch (Exception e)
            {
                logger.error("Unable to schedule heart beat", e);
            }
        }
    }

    /**
     * License load failure resets the heartbeat back to the default state
     */
    @Override
    public synchronized void onLicenseFail()
    {
        boolean newEnabled = DEFAULT_HEARTBEAT_ENABLED;

        if (newEnabled != enabled)
        {
            logger.debug("State change of heartbeat");
            this.enabled = newEnabled;
            try
            {
                scheduleJob();
            }
            catch (Exception e)
            {
                logger.error("Unable to schedule heart beat", e);
            }
        }
    }

    /**
     * Start or stop the hertbeat job depending on whether the heartbeat is enabled or not
     * @throws SchedulerException
     */
    private synchronized void scheduleJob() throws SchedulerException
    {
        // Schedule the heart beat to run regularly
        if(enabled)
        {
            logger.debug("heartbeat job scheduled");
            final JobDetail jobDetail = new JobDetail(JOB_NAME, Scheduler.DEFAULT_GROUP, HeartBeatJob.class);
            jobDetail.getJobDataMap().put("heartBeat", this);
            // Ensure the job wasn't already scheduled in an earlier retry of this transaction
            final String triggerName = JOB_NAME + "Trigger";
            scheduler.unscheduleJob(triggerName, Scheduler.DEFAULT_GROUP);
            final Trigger trigger = new SimpleTrigger(triggerName, Scheduler.DEFAULT_GROUP, new Date(), null,
                    //SimpleTrigger.REPEAT_INDEFINITELY, testMode ? 1000 : 4 * 60 * 60 * 1000);
                    SimpleTrigger.REPEAT_INDEFINITELY, testMode ? 1000 : 2 * 60 * 1000);
            scheduler.scheduleJob(jobDetail, trigger);
        }
        else
        {
            logger.debug("heartbeat job unscheduled");
            final String triggerName = JOB_NAME + "Trigger";
            scheduler.unscheduleJob(triggerName, Scheduler.DEFAULT_GROUP);
        }
    }

    /**
     * The scheduler job responsible for triggering a heartbeat on a regular basis.
     */
    public static class HeartBeatJob implements Job
    {
        public void execute(final JobExecutionContext jobexecutioncontext) throws JobExecutionException
        {
            final JobDataMap dataMap = jobexecutioncontext.getJobDetail().getJobDataMap();
            final HeartBeat heartBeat = (HeartBeat) dataMap.get("heartBeat");
            try
            {
                heartBeat.sendData();
            }
            catch (final Exception e)
            {
                if (logger.isDebugEnabled())
                {
                    // Verbose logging
                    HeartBeat.logger.debug("Heartbeat job failure", e);
                }
                else
                {
                    // Heartbeat errors are non-fatal and will show as single line warnings
                    HeartBeat.logger.warn(e.toString());
                    throw new JobExecutionException(e);
                }
            }
        }
    }


}
