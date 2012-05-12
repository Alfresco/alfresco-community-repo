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
package org.alfresco.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.DefaultHttpParamsFactory;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.extensions.webscripts.connector.RemoteClient;

/**
 * Helper class to provide access to Thread Local instances of HttpClient.
 * These instances will have been set up in a way that optimises the
 *  performance for one thread doing a fetch and then using the result.
 * You must call releaseConnection() when you're done with the request,
 *  otherwise things will break for the next request in this thread!
 * 
 * TODO Merge me back to Spring Surf, which is where this code has been
 *  pulled out from (was in {@link RemoteClient} but not available externally)
 */
public class HttpClientHelper
{   
    private static Log logger = LogFactory.getLog(HttpClientHelper.class);
            
    // HTTP Client instance - per thread
    private static ThreadLocal<HttpClient> httpClient = new ThreadLocal<HttpClient>()
    {
        @Override
        protected HttpClient initialValue()
        {
            logger.debug("Creating HttpClient instance for thread: " + Thread.currentThread().getName());
            return new HttpClient(new NonBlockingHttpParams());
        }
    };
    
    /**
     * Returns an initialised HttpClient instance for the current thread, which
     *  will have been configured for optimal settings
     */
    public static HttpClient getHttpClient()
    {
        return httpClient.get();
    }
    
    
    /////////////////////////////////////////////////////////////////
    // Helper classes 
    
    /**
     * An extension of the DefaultHttpParamsFactory that uses a RRW lock pattern rather than
     * full synchronization around the parameter CRUD - to avoid locking on many reads. 
     * 
     * @author Kevin Roast
     */
    public static class NonBlockingHttpParamsFactory extends DefaultHttpParamsFactory
    {
        private volatile HttpParams httpParams;
        
        /* (non-Javadoc)
         * @see org.apache.commons.httpclient.params.DefaultHttpParamsFactory#getDefaultParams()
         */
        @Override
        public HttpParams getDefaultParams()
        {
            if (httpParams == null)
            {
                synchronized (this)
                {
                    if (httpParams == null)
                    {
                        httpParams = createParams();
                    }
                }
            }
            
            return httpParams;
        }
        
        /**
         * NOTE: This is a copy of the code in {@link DefaultHttpParamsFactory}
         *       Unfortunately this is required because although the factory pattern allows the 
         *       override of the default param creation, it does not allow the class of the actual
         *       HttpParam implementation to be changed.
         */
        @Override
        protected HttpParams createParams()
        {
            HttpClientParams params = new NonBlockingHttpParams(null);
            
            params.setParameter(HttpMethodParams.USER_AGENT, "Spring Surf via Apache HttpClient/3.1");
            params.setVersion(HttpVersion.HTTP_1_1);
            params.setConnectionManagerClass(SimpleHttpConnectionManager.class);
            params.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            params.setHttpElementCharset("US-ASCII");
            params.setContentCharset("ISO-8859-1");
            params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            
            List<String> datePatterns = Arrays.asList(
                    new String[] {
                            DateUtil.PATTERN_RFC1123,
                            DateUtil.PATTERN_RFC1036,
                            DateUtil.PATTERN_ASCTIME,
                            "EEE, dd-MMM-yyyy HH:mm:ss z",
                            "EEE, dd-MMM-yyyy HH-mm-ss z",
                            "EEE, dd MMM yy HH:mm:ss z",
                            "EEE dd-MMM-yyyy HH:mm:ss z",
                            "EEE dd MMM yyyy HH:mm:ss z",
                            "EEE dd-MMM-yyyy HH-mm-ss z",
                            "EEE dd-MMM-yy HH:mm:ss z",
                            "EEE dd MMM yy HH:mm:ss z",
                            "EEE,dd-MMM-yy HH:mm:ss z",
                            "EEE,dd-MMM-yyyy HH:mm:ss z",
                            "EEE, dd-MM-yyyy HH:mm:ss z",                
                    }
            );
            params.setParameter(HttpMethodParams.DATE_PATTERNS, datePatterns);
            
            String agent = null;
            try
            {
                agent = System.getProperty("httpclient.useragent");
            }
            catch (SecurityException ignore)
            {
            }
            if (agent != null)
            {
                params.setParameter(HttpMethodParams.USER_AGENT, agent);
            }
            
            String preemptiveDefault = null;
            try
            {
                preemptiveDefault = System.getProperty("httpclient.authentication.preemptive");
            }
            catch (SecurityException ignore)
            {
            }
            if (preemptiveDefault != null)
            {
                preemptiveDefault = preemptiveDefault.trim().toLowerCase();
                if (preemptiveDefault.equals("true"))
                {
                    params.setParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, Boolean.TRUE);
                }
                else if (preemptiveDefault.equals("false"))
                {
                    params.setParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, Boolean.FALSE);
                }
            }
            
            String defaultCookiePolicy = null;
            try
            {
                defaultCookiePolicy = System.getProperty("apache.commons.httpclient.cookiespec");
            }
            catch (SecurityException ignore)
            {
            }
            if (defaultCookiePolicy != null)
            {
                if ("COMPATIBILITY".equalsIgnoreCase(defaultCookiePolicy))
                {
                    params.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
                }
                else if ("NETSCAPE_DRAFT".equalsIgnoreCase(defaultCookiePolicy))
                {
                    params.setCookiePolicy(CookiePolicy.NETSCAPE);
                }
                else if ("RFC2109".equalsIgnoreCase(defaultCookiePolicy))
                {
                    params.setCookiePolicy(CookiePolicy.RFC_2109);
                }
            }
            
            return params;
        }
    }
    
    /**
     * @author Kevin Roast
     */
    public static class NonBlockingHttpParams extends HttpClientParams
    {
        private HashMap<String, Object> parameters = new HashMap<String, Object>(8);
        private ReadWriteLock paramLock = new ReentrantReadWriteLock();
        
        public NonBlockingHttpParams()
        {
            super();
        }
        
        public NonBlockingHttpParams(HttpParams defaults)
        {
            super(defaults);
        }
        
        @Override
        public Object getParameter(final String name)
        {
            // See if the parameter has been explicitly defined
            Object param = null;
            paramLock.readLock().lock();
            try
            {
                param = this.parameters.get(name);
            }
            finally
            {
                paramLock.readLock().unlock();
            }
            if (param == null)
            {
                // If not, see if defaults are available
                HttpParams defaults = getDefaults();
                if (defaults != null)
                {
                    // Return default parameter value
                    param = defaults.getParameter(name);
                }
            }
            return param;
        }
        
        @Override
        public void setParameter(final String name, final Object value)
        {
            paramLock.writeLock().lock();
            try
            {
                this.parameters.put(name, value);
            }
            finally
            {
                paramLock.writeLock().unlock();
            }
        }
        
        @Override
        public boolean isParameterSetLocally(final String name)
        {
            paramLock.readLock().lock();
            try
            {
                return (this.parameters.get(name) != null);
            }
            finally
            {
                paramLock.readLock().unlock();
            }
        }
        
        @Override
        public void clear()
        {
            paramLock.writeLock().lock();
            try
            {
                this.parameters.clear();
            }
            finally
            {
                paramLock.writeLock().unlock();
            }
        }
        
        @Override
        public Object clone() throws CloneNotSupportedException
        {
            NonBlockingHttpParams clone = (NonBlockingHttpParams)super.clone();
            paramLock.readLock().lock();
            try
            {
                clone.parameters = (HashMap) this.parameters.clone();
            }
            finally
            {
                paramLock.readLock().unlock();
            }
            clone.setDefaults(getDefaults());
            return clone;
        }
    }
}
