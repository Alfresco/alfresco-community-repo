/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.heartbeat;

import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import javax.sql.DataSource;

import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Base64;
import org.alfresco.util.security.EncryptingOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;

import de.schlichtherle.util.ObfuscatedString;

/**
 * This class communicates some very basic repository statistics to Alfresco on a regular basis.
 * 
 * @author dward
 */
public class HeartBeat
{

    /** The logger. */
    private static final Log logger = LogFactory.getLog(HeartBeat.class);

    /** The relative path to the public keystore resource. */
    static final String PUBLIC_STORE = "heartbeatpublic.keystore";

    /** The password protecting this store. */
    static final char[] PUBLIC_STORE_PWD = new ObfuscatedString(new long[]
    {
        0x7D47AC5E71B3B560L, 0xD6F1405DC20AE70AL
    }).toString().toCharArray();

    /**
     * Are we running in test mode? If so we send data to local port 9999 rather than an alfresco server. We also use a
     * special test encryption certificate and ping on a more frequent basis.
     */
    private final boolean testMode;

    /** The transaction service. */
    private final TransactionService transactionService;

    /** DAO for current repository descriptor. */
    private final DescriptorDAO currentRepoDescriptorDAO;

    /** The person service. */
    private final PersonService personService;

    /** The data source. */
    private final DataSource dataSource;

    /**
     * The parameters that we expect to remain static throughout the lifetime of the repository. There is no need to
     * continuously update these.
     */
    private final Map<String, String> staticParameters;

    /** A secure source of random numbers used for encryption. */
    private final SecureRandom random;

    /** The public key used for encryption. */
    private final PublicKey publicKey;

    /**
     * Initialises the heart beat service. Note that dependencies are intentionally 'pulled' rather than injected
     * because we don't want these to be reconfigured.
     * 
     * @param context
     *            the context
     */
    public HeartBeat(final ApplicationContext context)
    {
        this(context, false);
    }

    /**
     * Initialises the heart beat service, potentially in test mode. Note that dependencies are intentionally 'pulled'
     * rather than injected because we don't want these to be reconfigured.
     * 
     * @param context
     *            the context
     * @param testMode
     *            are we running in test mode? If so we send data to local port 9999 rather than an alfresco server. We
     *            also use a special test encryption certificate and ping on a more frequent basis.
     */
    public HeartBeat(final ApplicationContext context, final boolean testMode)
    {
        this.testMode = testMode;
        this.transactionService = (TransactionService) context.getBean("transactionService");
        this.currentRepoDescriptorDAO = (DescriptorDAO) context.getBean("currentRepoDescriptorDAO");
        this.personService = (PersonService) context.getBean("personService");
        this.dataSource = (DataSource) context.getBean("dataSource");
        this.staticParameters = new TreeMap<String, String>();
        try
        {
            // Load up the static parameters
            final String ip = getLocalIps();
            this.staticParameters.put("ip", ip);
            final String uid;
            final Descriptor currentRepoDescriptor = this.currentRepoDescriptorDAO.getDescriptor();
            if (currentRepoDescriptor != null)
            {
                uid = currentRepoDescriptor.getId();
                this.staticParameters.put("uid", uid);
            }
            else
            {
                uid = "Unknown";
            }            
            final Descriptor serverDescriptor = ((DescriptorDAO) context.getBean("serverDescriptorDAO"))
                    .getDescriptor();
            this.staticParameters.put("edition", serverDescriptor.getEdition());
            this.staticParameters.put("versionMajor", serverDescriptor.getVersionMajor());
            this.staticParameters.put("versionMinor", serverDescriptor.getVersionMinor());
            this.staticParameters.put("schema", String.valueOf(serverDescriptor.getSchema()));

            // Use some of the unique parameters to seed the random number generator used for encryption
            this.random = SecureRandom.getInstance("SHA1PRNG");
            this.random.setSeed((uid + ip + System.currentTimeMillis()).getBytes("UTF-8"));

            // Load the public key from the key store (use the trial one if this is a unit test)
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            final InputStream in = getClass().getResourceAsStream(HeartBeat.PUBLIC_STORE);
            keyStore.load(in, HeartBeat.PUBLIC_STORE_PWD);
            in.close();
            final String jobName = testMode ? "test" : "heartbeat";
            final Certificate cert = keyStore.getCertificate(jobName);
            this.publicKey = cert.getPublicKey();

            // Schedule the heart beat to run regularly
            final Scheduler scheduler = (Scheduler) context.getBean("schedulerFactory");
            final JobDetail jobDetail = new JobDetail(jobName, Scheduler.DEFAULT_GROUP, HeartBeatJob.class);
            jobDetail.getJobDataMap().put("heartBeat", this);
            final Trigger trigger = new SimpleTrigger(jobName + "Trigger", Scheduler.DEFAULT_GROUP, new Date(), null,
                    SimpleTrigger.REPEAT_INDEFINITELY, testMode ? 1000 : 4 * 60 * 60 * 1000);
            scheduler.scheduleJob(jobDetail, trigger);
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
        final HttpURLConnection req = (HttpURLConnection) new URL(this.testMode ? "http://localhost:9999/heartbeat/"
                : "http://DAVIDW01.activiti.local:8080/heartbeat/" /*"http://heartbeat.alfresco.com/heartbeat/"*/).openConnection();
        try
        {
            req.setRequestMethod("POST");
            req.setRequestProperty("Content-Type", "application/octet-stream");
            req.setChunkedStreamingMode(1024);
            req.setConnectTimeout(2000);
            req.setDoOutput(true);
            req.connect();
            sendData(req.getOutputStream());
            if (req.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                throw new IOException(req.getResponseMessage());
            }
        }
        finally
        {
            try
            {
                req.disconnect();
            }
            catch (final Exception e)
            {
            }

        }
    }

    /**
     * Writes the heartbeat data to a given output stream. Parameters are serialized in XML format for maximum forward
     * compatibility.
     * 
     * @param dest
     *            the stream to write to
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws GeneralSecurityException
     *             an encryption related exception
     */
    public void sendData(final OutputStream dest) throws IOException, GeneralSecurityException
    {
        // Complement the static parameters with some dynamic ones
        final Map<String, String> params = this.transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Map<String, String>>()
                {
                    public Map<String, String> execute()
                    {
                        final Map<String, String> params = new TreeMap<String, String>(HeartBeat.this.staticParameters);
                        params.put("numUsers", String.valueOf(HeartBeat.this.personService.getAllPeople().size()));
                        params.put("maxNodeId", String.valueOf(getMaxNodeId()));
                        final byte[] licenseKey = HeartBeat.this.currentRepoDescriptorDAO.getLicenseKey();
                        if (licenseKey != null)
                        {
                            params.put("licenseKey", Base64.encodeBytes(licenseKey, Base64.DONT_BREAK_LINES));
                        }
                        return params;
                    }
                }, true /* readOnly */, false /* requiresNew */);

        // Compress and encrypt the output stream
        OutputStream out = new GZIPOutputStream(new EncryptingOutputStream(dest, this.publicKey, this.random), 1024);

        // Encode the parameters to XML
        XMLEncoder encoder = null;
        try
        {
            encoder = new XMLEncoder(out);
            encoder.writeObject(params);
        }
        finally
        {
            if (encoder != null)
            {
                try
                {
                    encoder.close();
                    out = null;
                }
                catch (final Exception e)
                {
                }
            }
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (final Exception e)
                {
                }
            }
        }
    }

    /**
     * The scheduler job responsible for triggering a heartbeat on a regular basis.
     */
    public static class HeartBeatJob implements Job
    {
        /*
         * (non-Javadoc)
         * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
         */
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
                // Heartbeat errors are non-fatal and will show as single line warnings
                HeartBeat.logger.warn(e.toString());
                throw new JobExecutionException(e);
            }
        }
    }

    /**
     * Attempts to get all the local IP addresses of this machine in order to distinguish it from other nodes in the
     * same network. The machine may use a static IP address in conjunction with a loopback adapter (e.g. to support
     * Oracle on Windows), so the IP of the default network interface may not be enough to uniquely identify this
     * machine.
     * 
     * @return the local IP addresses, separated by the '/' character
     */
    private String getLocalIps()
    {
        final StringBuilder ip = new StringBuilder(1024);
        boolean first = true;
        try
        {
            final Enumeration<NetworkInterface> i = NetworkInterface.getNetworkInterfaces();
            while (i.hasMoreElements())
            {
                final NetworkInterface n = i.nextElement();
                final Enumeration<InetAddress> j = n.getInetAddresses();
                while (j.hasMoreElements())
                {
                    InetAddress a = j.nextElement();
                    if (a.isLoopbackAddress())
                    {
                        continue;
                    }
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        ip.append('/');
                    }
                    ip.append(a.getHostAddress());
                }
            }
        }
        catch (final Exception e)
        {
            // Ignore
        }
        return first ? "127.0.0.1" : ip.toString();
    }

    /**
     * Gets the maximum repository node id. Note that this isn't the best indication of size, because on oracle, all
     * unique IDs are generated from the same sequence. A count(*) would result in an index scan.
     * 
     * @return the max node id
     */
    private int getMaxNodeId()
    {
        Connection connection = null;
        Statement stmt = null;
        try
        {
            connection = this.dataSource.getConnection();
            connection.setAutoCommit(true);
            stmt = connection.createStatement();
            final ResultSet rs = stmt.executeQuery("select max(id) from alf_node");
            if (!rs.next())
            {
                return 0;
            }
            return rs.getInt(1);
        }
        catch (final SQLException e)
        {
            return 0;
        }
        finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch (final Exception e)
                {
                }
            }
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (final Exception e)
                {
                }
            }
        }
    }
}
