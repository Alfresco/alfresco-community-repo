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
package org.alfresco.rest.framework.webscripts;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import org.alfresco.repo.web.scripts.BufferedRequest;
import org.alfresco.repo.web.scripts.BufferedResponse;
import org.alfresco.repo.web.scripts.TempOutputStream;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Entry point for API webscript.  Supports version/scope as well
 * as discovery.
 *
 * @author Gethin James
 */
public abstract class ApiWebScript extends AbstractWebScript
{
    private static Log logger = LogFactory.getLog(ApiWebScript.class);
    protected ApiAssistant assistant;
    protected boolean encryptTempFiles = false;
    protected String tempDirectoryName = null;
    protected int memoryThreshold = 4 * 1024 * 1024; // 4mb
    protected long maxContentSize = (long) 4 * 1024 * 1024 * 1024; // 4gb
    protected Supplier<TempOutputStream> streamFactory = null;
    protected TransactionService transactionService;

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setAssistant(ApiAssistant assistant) {
        this.assistant = assistant;
    }

    public void setTempDirectoryName(String tempDirectoryName)
    {
        this.tempDirectoryName = tempDirectoryName;
    }

    public void setEncryptTempFiles(boolean encryptTempFiles)
    {
        this.encryptTempFiles = encryptTempFiles;
    }

    public void setMemoryThreshold(int memoryThreshold)
    {
        this.memoryThreshold = memoryThreshold;
    }

    public void setMaxContentSize(long maxContentSize)
    {
        this.maxContentSize = maxContentSize;
    }

    public void setStreamFactory(Supplier<TempOutputStream> streamFactory)
    {
        this.streamFactory = streamFactory;
    }

    public void init()
    {
        File tempDirectory = TempFileProvider.getTempDir(tempDirectoryName);
        streamFactory = TempOutputStream.factory(tempDirectory, memoryThreshold, maxContentSize, false);
    }

    @Override
    public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException
    {
        final Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        final Api api = ApiAssistant.determineApi(templateVars);

        try (final BufferedRequest bufferedReq = getRequest(req);
             final BufferedResponse bufferedRes = getResponse(res))
        {
            execute(api, bufferedReq, bufferedRes);

            // Ensure a response is always flushed after successful execution
            if (bufferedRes != null)
            {
                bufferedRes.writeResponse();
            }
        }
    }

    protected BufferedRequest getRequest(final WebScriptRequest req)
    {
        // create buffered request and response that allow transaction retrying
        return new BufferedRequest(req, streamFactory);
    }

    protected BufferedResponse getResponse(final WebScriptResponse resp)
    {
        // create buffered request and response that allow transaction retrying
        return new BufferedResponse(resp, memoryThreshold, streamFactory);
    }

    public abstract void execute(final Api api, WebScriptRequest req, WebScriptResponse res) throws IOException;

}
