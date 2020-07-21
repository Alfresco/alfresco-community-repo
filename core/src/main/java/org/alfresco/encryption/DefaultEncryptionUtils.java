/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.encryption;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AlgorithmParameters;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.encryption.MACUtils.MACInput;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.IPUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.util.FileCopyUtils;

/**
 * Various encryption utility methods.
 * 
 * @since 4.0
 */
public class DefaultEncryptionUtils implements EncryptionUtils
{
    // Logger
    protected static Log logger = LogFactory.getLog(Encryptor.class);

    protected static String HEADER_ALGORITHM_PARAMETERS = "XAlfresco-algorithmParameters";
    protected static String HEADER_MAC = "XAlfresco-mac";
    protected static String HEADER_TIMESTAMP = "XAlfresco-timestamp";

    protected Encryptor encryptor;
    protected MACUtils macUtils;
    protected long messageTimeout; // ms
    protected String remoteIP;
    protected String localIP;
    
    public DefaultEncryptionUtils()
    {
        try
        {
            this.localIP = InetAddress.getLocalHost().getHostAddress();
        }
        catch(Exception e)
        {
            throw new AlfrescoRuntimeException("Unable to initialise EncryptionUtils", e);
        }
    }

    public String getRemoteIP()
    {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP)
    {
        try
        {
            this.remoteIP = IPUtils.getRealIPAddress(remoteIP);
        }
        catch (UnknownHostException e)
        {
            throw new AlfrescoRuntimeException("Failed to get server IP address", e);
        }
    }

    /**
     * Get the local registered IP address for authentication purposes
     * 
     * @return String
     */
    protected String getLocalIPAddress()
    {
        return localIP;
    }
    
    public void setMessageTimeout(long messageTimeout)
    {
        this.messageTimeout = messageTimeout;
    }

    public void setEncryptor(Encryptor encryptor)
    {
        this.encryptor = encryptor;
    }
    
    public void setMacUtils(MACUtils macUtils)
    {
        this.macUtils = macUtils;
    }

    protected void setRequestMac(HttpMethod method, byte[] mac)
    {
        if(mac == null)
        {
            throw new AlfrescoRuntimeException("Mac cannot be null");
        }
        method.setRequestHeader(HEADER_MAC, Base64.encodeBytes(mac));    
    }

    /**
     * Set the MAC on the HTTP response
     * 
     * @param response HttpServletResponse
     * @param mac byte[]
     */
    protected void setMac(HttpServletResponse response, byte[] mac)
    {
        if(mac == null)
        {
            throw new AlfrescoRuntimeException("Mac cannot be null");
        }

        response.setHeader(HEADER_MAC, Base64.encodeBytes(mac));    
    }
    
    /**
     * Get the MAC (Message Authentication Code) on the HTTP request
     * 
     * @param req HttpServletRequest
     * @return the MAC
     * @throws IOException
     */
    protected byte[] getMac(HttpServletRequest req) throws IOException
    {
        String header = req.getHeader(HEADER_MAC);
        if(header != null)
        {
            return Base64.decode(header);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Get the MAC (Message Authentication Code) on the HTTP response
     * 
     * @param res HttpMethod
     * @return the MAC
     * @throws IOException
     */
    protected byte[] getResponseMac(HttpMethod res) throws IOException
    {
        Header header = res.getResponseHeader(HEADER_MAC);
        if(header != null)
        {
            return Base64.decode(header.getValue());
        }
        else
        {
            return null;
        }
    }

    /**
     * Set the timestamp on the HTTP request
     * @param method HttpMethod
     * @param timestamp (ms, in UNIX time)
     */
    protected void setRequestTimestamp(HttpMethod method, long timestamp)
    {
        method.setRequestHeader(HEADER_TIMESTAMP, String.valueOf(timestamp));        
    }

    /**
     * Set the timestamp on the HTTP response
     * @param res HttpServletResponse
     * @param timestamp (ms, in UNIX time)
     */
    protected void setTimestamp(HttpServletResponse res, long timestamp)
    {
        res.setHeader(HEADER_TIMESTAMP, String.valueOf(timestamp));        
    }

    /**
     * Get the timestamp on the HTTP response
     * 
     * @param method HttpMethod
     * @return timestamp (ms, in UNIX time)
     * @throws IOException
     */
    protected Long getResponseTimestamp(HttpMethod method) throws IOException
    {
        Header header = method.getResponseHeader(HEADER_TIMESTAMP);
        if(header != null)
        {
            return Long.valueOf(header.getValue());
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Get the timestamp on the HTTP request
     * 
     * @param method HttpServletRequest
     * @return timestamp (ms, in UNIX time)
     * @throws IOException
     */
    protected Long getTimestamp(HttpServletRequest method) throws IOException
    {
        String header = method.getHeader(HEADER_TIMESTAMP);
        if(header != null)
        {
            return Long.valueOf(header);
        }
        else
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRequestAlgorithmParameters(HttpMethod method, AlgorithmParameters params) throws IOException
    {
        if(params != null)
        {
            method.setRequestHeader(HEADER_ALGORITHM_PARAMETERS, Base64.encodeBytes(params.getEncoded()));
        }
    }
    
    /**
     * Set the algorithm parameters header on the HTTP response
     * 
     * @param response HttpServletResponse
     * @param params AlgorithmParameters
     * @throws IOException
     */
    protected void setAlgorithmParameters(HttpServletResponse response, AlgorithmParameters params) throws IOException
    {
        if(params != null)
        {
            response.setHeader(HEADER_ALGORITHM_PARAMETERS, Base64.encodeBytes(params.getEncoded()));
        }
    }
    
    /**
     * Decode cipher algorithm parameters from the HTTP method
     * 
     * @param method HttpMethod
     * @return decoded algorithm parameters
     * @throws IOException
     */
    protected AlgorithmParameters decodeAlgorithmParameters(HttpMethod method) throws IOException
    {
        Header header = method.getResponseHeader(HEADER_ALGORITHM_PARAMETERS);
        if(header != null)
        {
            byte[] algorithmParams = Base64.decode(header.getValue());
            AlgorithmParameters algorithmParameters  = encryptor.decodeAlgorithmParameters(algorithmParams);
            return algorithmParameters;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Decode cipher algorithm parameters from the HTTP method
     * 
     * @param req
     * @return decoded algorithm parameters
     * @throws IOException
     */
    protected AlgorithmParameters decodeAlgorithmParameters(HttpServletRequest req) throws IOException
    {
        String header = req.getHeader(HEADER_ALGORITHM_PARAMETERS);
        if(header != null)
        {
            byte[] algorithmParams = Base64.decode(header);
            AlgorithmParameters algorithmParameters  = encryptor.decodeAlgorithmParameters(algorithmParams);
            return algorithmParameters;
        }
        else
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] decryptResponseBody(HttpMethod method) throws IOException
    {
        // TODO fileoutputstream if content is especially large?
        InputStream body = method.getResponseBodyAsStream();
        if(body != null)
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            FileCopyUtils.copy(body, out);

            AlgorithmParameters params = decodeAlgorithmParameters(method);
            if(params != null)
            {
                byte[] decrypted = encryptor.decrypt(KeyProvider.ALIAS_SOLR, params, out.toByteArray());
                return decrypted;
            }
            else
            {
                throw new AlfrescoRuntimeException("Unable to decrypt response body, missing encryption algorithm parameters");
            }
        }
        else
        {
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] decryptBody(HttpServletRequest req) throws IOException
    {
        if(req.getMethod().equals("POST"))
        {
            InputStream bodyStream = req.getInputStream();
            if(bodyStream != null)
            {
                // expect algorithParameters header
                AlgorithmParameters p = decodeAlgorithmParameters(req);

                // decrypt the body
                InputStream in = encryptor.decrypt(KeyProvider.ALIAS_SOLR, p, bodyStream);
                return IOUtils.toByteArray(in);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authenticateResponse(HttpMethod method, String remoteIP, byte[] decryptedBody)
    {
        try
        {
            byte[] expectedMAC = getResponseMac(method);
            Long timestamp = getResponseTimestamp(method);
            if(timestamp == null)
            {
                return false;
            }
            remoteIP = IPUtils.getRealIPAddress(remoteIP);
            return authenticate(expectedMAC, new MACInput(decryptedBody, timestamp.longValue(), remoteIP));
        }
        catch(Exception e)
        {
            throw new RuntimeException("Unable to authenticate HTTP response", e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authenticate(HttpServletRequest req, byte[] decryptedBody)
    {
        try
        {
            byte[] expectedMAC = getMac(req);
            Long timestamp = getTimestamp(req);
            if(timestamp == null)
            {
                return false;
            }
            String ipAddress = IPUtils.getRealIPAddress(req.getRemoteAddr());
            return authenticate(expectedMAC, new MACInput(decryptedBody, timestamp.longValue(), ipAddress));
        }
        catch(Exception e)
        {
            throw new AlfrescoRuntimeException("Unable to authenticate HTTP request", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRequestAuthentication(HttpMethod method, byte[] message) throws IOException
    {
        long requestTimestamp = System.currentTimeMillis();
    
        // add MAC header
        byte[] mac = macUtils.generateMAC(KeyProvider.ALIAS_SOLR, new MACInput(message, requestTimestamp, getLocalIPAddress()));
    
        if(logger.isDebugEnabled())
        {
            logger.debug("Setting MAC " + Arrays.toString(mac) + " on HTTP request " + method.getPath());
            logger.debug("Setting timestamp " + requestTimestamp + " on HTTP request " + method.getPath());
        }
        
        setRequestMac(method, mac);

        // prevent replays
        setRequestTimestamp(method, requestTimestamp);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setResponseAuthentication(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            byte[] responseBody, AlgorithmParameters params) throws IOException
    {
        long responseTimestamp = System.currentTimeMillis();
        byte[] mac = macUtils.generateMAC(KeyProvider.ALIAS_SOLR,
                new MACInput(responseBody, responseTimestamp, getLocalIPAddress()));

        if(logger.isDebugEnabled())
        {
            logger.debug("Setting MAC " + Arrays.toString(mac) + " on HTTP response to request " + httpRequest.getRequestURI());
            logger.debug("Setting timestamp " + responseTimestamp + " on HTTP response to request " + httpRequest.getRequestURI());
        }

        setAlgorithmParameters(httpResponse, params);
        setMac(httpResponse, mac);

        // prevent replays
        setTimestamp(httpResponse, responseTimestamp);
    }

    protected boolean authenticate(byte[] expectedMAC, MACInput macInput)
    {
        // check the MAC and, if valid, check that the timestamp is under the threshold and that the remote IP is
        // the expected IP
        boolean authorized = macUtils.validateMAC(KeyProvider.ALIAS_SOLR, expectedMAC, macInput) &&
            validateTimestamp(macInput.getTimestamp());
        return authorized;
    }
    
    protected boolean validateTimestamp(long timestamp)
    {
        long currentTime = System.currentTimeMillis();
        return((currentTime - timestamp) < messageTimeout);
    }

}
