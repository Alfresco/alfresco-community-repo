/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.tenant;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Base64;

/**
 * Simple test of Tenant REST API - eg. create tenant
 * 
 * @author janv
 * @since 4.2
 */
public class TenantAdminSystemTest extends TestCase
{
    private static Log logger = LogFactory.getLog(TenantAdminSystemTest.class);
    
    // TODO - use test property file
    private static final String REPO = "http://localhost:8080/alfresco";
    
    // web script (REST)
    private static final String WEBSCRIPT_ENDPOINT  = REPO + "/service";
    
    // Tenant Admin Service part-URLs
    private static final String URL_TENANTS = "/api/tenants";
    
    // Test users & passwords
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PW = "admin";
    
    private static final String TENANT_PREFIX = "t"+System.currentTimeMillis()+"-";
    
    private static final int T_CNT = 5;
    
    public enum Op {CREATE_TENANT};
    
    public TenantAdminSystemTest()
    {
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testLogin() throws Exception
    {
        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
        assertNotNull(ticket);
    }
    
    public void testCreateTenants() throws Exception
    {
        runWorkers(1, T_CNT, Op.CREATE_TENANT);
    }
    
    protected static void createTenant(String tenantDomain, String ticket) throws Exception
    {
        JSONObject tenant = new JSONObject();
        tenant.put("tenantDomain", tenantDomain);
        tenant.put("tenantAdminPassword", tenantDomain);
        
        String url = WEBSCRIPT_ENDPOINT + URL_TENANTS;
        String response = callPostWebScript(url, ticket, tenant.toString());
        
        if (logger.isDebugEnabled())
        {
            logger.debug("createTenant: " + tenantDomain);
            logger.debug("----------");
            logger.debug(url);
            logger.debug(response);
        }
    }
    
    public void testGetTenants() throws Exception
    {
        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
        
        getTenants(ticket);
    }
    
    protected void getTenants(String ticket) throws Exception
    {
        String url = WEBSCRIPT_ENDPOINT + URL_TENANTS;
        String response = callGetWebScript(url, ticket);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("getTenants:");
            logger.debug("-------");
            logger.debug(url);
            logger.debug(response);
        }
    }
    
    protected static String callGetWebScript(String urlString, String ticket) throws MalformedURLException, URISyntaxException, IOException
    {
        return callOutWebScript(urlString, "GET", ticket);
    }
    
    protected static String callDeleteWebScript(String urlString, String ticket) throws MalformedURLException, URISyntaxException, IOException
    {
        return callOutWebScript(urlString, "DELETE", ticket);
    }
    
    protected static String callPostWebScript(String urlString, String ticket, String data) throws MalformedURLException, URISyntaxException, IOException
    {
        return callInOutWebScript(urlString, "POST", ticket, data);
    }
    
    protected static String callPutWebScript(String urlString, String ticket, String data) throws MalformedURLException, URISyntaxException, IOException
    {
        return callInOutWebScript(urlString, "PUT", ticket, data);
    }
    
    private static String callOutWebScript(String urlString, String method, String ticket) throws MalformedURLException, URISyntaxException, IOException
    {
        URL url = new URL(urlString);
        
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(method);
        
        if (ticket != null)
        {
            // add Base64 encoded authorization header
            // refer to: http://wiki.alfresco.com/wiki/Web_Scripts_Framework#HTTP_Basic_Authentication
            conn.addRequestProperty("Authorization", "Basic " + Base64.encodeBytes(ticket.getBytes()));
        }
        
        String result = null;
        InputStream is = null;
        BufferedReader br = null;
        
        try
        {
            is = conn.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            
            String line = null;
            StringBuffer sb = new StringBuffer();
            while(((line = br.readLine()) !=null))  {
                sb.append(line);
            }
            
            result = sb.toString();
        }
        finally
        {
            if (br != null) { br.close(); };
            if (is != null) { is.close(); };
        }
        
        return result;
    }
    
    private static String callInOutWebScript(String urlString, String method, String ticket, String data) throws MalformedURLException, URISyntaxException, IOException
    {
        return callInOutWeb(urlString, method, ticket, data, "application/json", null);
    }
    
    private static String callInOutWeb(String urlString, String method, String ticket, String data, String contentType, String soapAction) throws MalformedURLException, URISyntaxException, IOException
    {
        URL url = new URL(urlString);
        
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod(method);
        
        conn.setRequestProperty("Content-type", contentType);
        
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches (false);
        
        if (soapAction != null)
        {
            conn.setRequestProperty("SOAPAction", soapAction);
        }
        
        if (ticket != null)
        {
            // add Base64 encoded authorization header
            // refer to: http://wiki.alfresco.com/wiki/Web_Scripts_Framework#HTTP_Basic_Authentication
            conn.addRequestProperty("Authorization", "Basic " + Base64.encodeBytes(ticket.getBytes()));
        }
        
        String result = null;
        BufferedReader br = null;
        DataOutputStream wr = null;
        OutputStream os = null;
        InputStream is = null;
        
        try
        {
            os = conn.getOutputStream();
            wr = new DataOutputStream(os);
            wr.write(data.getBytes());
            wr.flush();
        }
        finally
        {
            if (wr != null) { wr.close(); };
            if (os != null) { os.close(); };
        }
        
        try
        {
            is = conn.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            
            String line = null;
            StringBuffer sb = new StringBuffer();
            while(((line = br.readLine()) !=null)) 
            {
                sb.append(line);
            }
            
            result = sb.toString();
        }
        finally
        {
            if (br != null) { br.close(); };
            if (is != null) { is.close(); };
        }
        
        return result;
    }
    
    protected static String callLoginWebScript(String serviceUrl, String username, String password) throws MalformedURLException, URISyntaxException, IOException
    {
        // Refer to: http://wiki.alfresco.com/wiki/Web_Scripts_Framework#HTTP_Basic_Authentication  
        String ticketResult = callGetWebScript(serviceUrl+"/api/login?u="+username+"&pw="+password, null);
        
        if (ticketResult != null)
        {
            int startTag = ticketResult.indexOf("<ticket>");
            int endTag = ticketResult.indexOf("</ticket>");
            if ((startTag != -1) && (endTag != -1))
            {
                ticketResult = ticketResult.substring(startTag+("<ticket>".length()), endTag);
            }
        }
        
        return ticketResult;
    }
    
    private static void runWorkers(int threadCount, int threadBatch, Op mode)
    {
        logger.info("Start: Mode "+mode+" ["+threadCount+"]");
        
        long start = System.currentTimeMillis();
        
        Thread[] threads = new Thread[threadCount];
        
        Worker[] nesters = new Worker[threadCount];
        
        for (int i = 0; i < threadCount; i++)
        {
            int startId = (i*threadBatch)+1;
            int endId = (i+1)*threadBatch;
            
            Worker nester = new Worker(mode, startId, endId);
            nesters[i] = nester;
            
            threads[i] = new Thread(nester);
            threads[i].start();
        }
        
        int totalCnt = 0;
        long totalTime = 0;
        
        // join each thread so that we wait for them all to finish
        for (int i = 0; i < threadCount; i++)
        {
            try
            {
                threads[i].join();
                
                if (nesters[i].getErrorStackTrace() != null)
                {
                    throw new RuntimeException(nesters[i].getErrorStackTrace());
                }
                
                if (nesters[i].getOpTime() != null)
                {
                    totalTime = totalTime + nesters[i].getOpTime();
                    totalCnt++;
                }
            }
            catch (InterruptedException e)
            {
                // ignore
            }
        }
        
        logger.info("Finish: Mode "+mode+" [threadCount="+threadCount+", threadBatch="+threadBatch+"] in "+(System.currentTimeMillis()-start)+" ms (avg per thread = "+totalTime/totalCnt+" ms)");
    }
    
    private static class Worker implements Runnable
    {
        private Op op;
        private int startId;
        private int endId;
        
        private String errorStackTrace = null;
        
        private Long opTime;
        
        Worker(Op mode, int startId, int endId)
        {
            this.op = mode;
            this.startId = startId;
            this.endId = endId;
        }
        
        public String getErrorStackTrace()
        {
            return errorStackTrace;
        }
        
        public Long getOpTime()
        {
            return opTime;
        }
        
        public void run()
        {
            String tenantDomain = null;
            
            try
            {
                long start = System.currentTimeMillis();
                
                logger.info("Start: Mode "+op+" ("+startId+" to "+endId+") [ThreadId="+Thread.currentThread().getId()+"]");
                
                for (int i = startId; i <= endId; i++)
                {
                    tenantDomain = TENANT_PREFIX+String.format("%05d", i);
                    
                    switch(op)
                    {
                    case CREATE_TENANT:
                        String ticket = callLoginWebScript(WEBSCRIPT_ENDPOINT, ADMIN_USER, ADMIN_PW);
                        createTenant(TENANT_PREFIX+"--"+String.format("%05d", i), ticket);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported op type: "+op);
                    }
                }
                
                opTime = (System.currentTimeMillis()-start);
                
                logger.info("Finish: Mode "+op+" ("+startId+" to "+endId+") [ThreadId="+Thread.currentThread().getId()+"] in "+opTime+" ms");
            }
            catch (Throwable t)
            {
                logger.error("End " + tenantDomain + " with error " + t.getMessage());
                
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                
                errorStackTrace = sw.toString();
            }
        }
    }
}
