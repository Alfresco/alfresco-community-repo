/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco, but is derived from a file 
 * Copyright 2008 The JSlideShare Team 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 */

package org.alfresco.repo.publishing.slideshare;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;

import com.benfante.jslideshare.SlideShareConnector;
import com.benfante.jslideshare.SlideShareErrorException;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class SlideShareConnectorImpl implements SlideShareConnector
{

    private static final Logger logger = Logger.getLogger(SlideShareConnectorImpl.class);

    private String apiKey;
    private String sharedSecret;
    private HttpClient httpClient;

    public SlideShareConnectorImpl()
    {
        httpClient = new HttpClient();
        httpClient.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
    }

    public SlideShareConnectorImpl(String apiKey, String sharedSecret)
    {
        this();
        this.apiKey = apiKey;
        this.sharedSecret = sharedSecret;
    }
    
    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getSharedSecret()
    {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret)
    {
        this.sharedSecret = sharedSecret;
    }


    public InputStream sendMessage(String url, Map<String, String> parameters) throws IOException,
            SlideShareErrorException
    {
        PostMethod method = new PostMethod(url);
        method.addParameter("api_key", this.apiKey);
        Iterator<Map.Entry<String, String>> entryIt = parameters.entrySet().iterator();
        while (entryIt.hasNext())
        {
            Map.Entry<String, String> entry = entryIt.next();
            method.addParameter(entry.getKey(), entry.getValue());
        }
        Date now = new Date();
        String ts = Long.toString(now.getTime() / 1000);
        String hash = DigestUtils.shaHex(this.sharedSecret + ts).toLowerCase();
        method.addParameter("ts", ts);
        method.addParameter("hash", hash);
        logger.debug("Sending POST message to " + method.getURI().getURI() + " with parameters "
                + Arrays.toString(method.getParameters()));
        int statusCode = httpClient.executeMethod(method);
        if (statusCode != HttpStatus.SC_OK)
        {
            logger.debug("Server replied with a " + statusCode + " HTTP status code ("
                    + HttpStatus.getStatusText(statusCode) + ")");
            throw new SlideShareErrorException(statusCode, HttpStatus.getStatusText(statusCode));
        }
        if (logger.isDebugEnabled())
        {
            logger.debug(method.getResponseBodyAsString());
        }
        InputStream result = new ByteArrayInputStream(method.getResponseBody());
        method.releaseConnection();
        return result;
    }

    public InputStream sendMultiPartMessage(String url, Map<String, String> parameters, Map<String, File> files)
            throws IOException, SlideShareErrorException
    {
        PostMethod method = new PostMethod(url);
        List<Part> partList = new ArrayList<Part>();
        partList.add(createStringPart("api_key", this.apiKey));
        Date now = new Date();
        String ts = Long.toString(now.getTime() / 1000);
        String hash = DigestUtils.shaHex(this.sharedSecret + ts).toLowerCase();
        partList.add(createStringPart("ts", ts));
        partList.add(createStringPart("hash", hash));
        Iterator<Map.Entry<String, String>> entryIt = parameters.entrySet().iterator();
        while (entryIt.hasNext())
        {
            Map.Entry<String, String> entry = entryIt.next();
            partList.add(createStringPart(entry.getKey(), entry.getValue()));
        }
        Iterator<Map.Entry<String, File>> entryFileIt = files.entrySet().iterator();
        while (entryFileIt.hasNext())
        {
            Map.Entry<String, File> entry = entryFileIt.next();
            partList.add(createFilePart(entry.getKey(), entry.getValue()));
        }
        MultipartRequestEntity requestEntity = new MultipartRequestEntity(partList.toArray(new Part[partList.size()]),
                method.getParams());
        method.setRequestEntity(requestEntity);
        logger.debug("Sending multipart POST message to " + method.getURI().getURI() + " with parts " + partList);
        int statusCode = httpClient.executeMethod(method);
        if (statusCode != HttpStatus.SC_OK)
        {
            logger.debug("Server replied with a " + statusCode + " HTTP status code ("
                    + HttpStatus.getStatusText(statusCode) + ")");
            throw new SlideShareErrorException(statusCode, HttpStatus.getStatusText(statusCode));
        }
        if (logger.isDebugEnabled())
        {
            logger.debug(method.getResponseBodyAsString());
        }
        InputStream result = new ByteArrayInputStream(method.getResponseBody());
        method.releaseConnection();
        return result;
    }

    public InputStream sendGetMessage(String url, Map<String, String> parameters) throws IOException,
            SlideShareErrorException
    {
        GetMethod method = new GetMethod(url);
        NameValuePair[] params = new NameValuePair[parameters.size() + 3];
        int i = 0;
        params[i++] = new NameValuePair("api_key", this.apiKey);
        Iterator<Map.Entry<String, String>> entryIt = parameters.entrySet().iterator();
        while (entryIt.hasNext())
        {
            Map.Entry<String, String> entry = entryIt.next();
            params[i++] = new NameValuePair(entry.getKey(), entry.getValue());
        }
        Date now = new Date();
        String ts = Long.toString(now.getTime() / 1000);
        String hash = DigestUtils.shaHex(this.sharedSecret + ts).toLowerCase();
        params[i++] = new NameValuePair("ts", ts);
        params[i++] = new NameValuePair("hash", hash);
        method.setQueryString(params);
        logger.debug("Sending GET message to " + method.getURI().getURI() + " with parameters "
                + Arrays.toString(params));
        int statusCode = httpClient.executeMethod(method);
        if (statusCode != HttpStatus.SC_OK)
        {
            logger.debug("Server replied with a " + statusCode + " HTTP status code ("
                    + HttpStatus.getStatusText(statusCode) + ")");
            throw new SlideShareErrorException(statusCode, HttpStatus.getStatusText(statusCode));
        }
        if (logger.isDebugEnabled())
        {
            logger.debug(method.getResponseBodyAsString());
        }
        InputStream result = new ByteArrayInputStream(method.getResponseBody());
        method.releaseConnection();
        return result;
    }

    private StringPart createStringPart(String name, String value)
    {
        StringPart stringPart = new StringPart(name, value);
        stringPart.setContentType(null);
        stringPart.setTransferEncoding(null);
        stringPart.setCharSet("UTF-8");
        return stringPart;
    }

    private FilePart createFilePart(String name, File value) throws FileNotFoundException
    {
        FilePart filePart = new FilePart(name, value);
        filePart.setTransferEncoding(null);
        filePart.setCharSet(null);
        return filePart;
    }

}
