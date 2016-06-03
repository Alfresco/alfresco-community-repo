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

import org.alfresco.repo.web.scripts.BufferedRequest;
import org.alfresco.repo.web.scripts.BufferedResponse;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.exceptions.DefaultExceptionResolver;
import org.alfresco.rest.framework.core.exceptions.ErrorResponse;
import org.alfresco.rest.framework.core.exceptions.ExceptionResolver;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper.Writer;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.content.ContentInfoImpl;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStreamFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.Description.RequiredCache;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

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
    protected ThresholdOutputStreamFactory streamFactory = null;
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

    public void setStreamFactory(ThresholdOutputStreamFactory streamFactory)
    {
        this.streamFactory = streamFactory;
    }

    public void init()
    {
        File tempDirectory = TempFileProvider.getTempDir(tempDirectoryName);
        this.streamFactory = ThresholdOutputStreamFactory.newInstance(tempDirectory, memoryThreshold, maxContentSize, encryptTempFiles);
    }

    @Override
    public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException
    {
		Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
		Api api = assistant.determineApi(templateVars);
		
		final BufferedRequest bufferedReq = getRequest(req);
		final BufferedResponse bufferedRes = getResponse(res);

		try
		{
		    execute(api, bufferedReq, bufferedRes);
		}
		finally
		{
            // Get rid of any temporary files
            if (bufferedReq != null)
            {
                bufferedReq.close();
            }
		}

        // Ensure a response is always flushed after successful execution
        if (bufferedRes != null)
        {
            bufferedRes.writeResponse();
        }
    }

    protected BufferedRequest getRequest(final WebScriptRequest req)
    {
        // create buffered request and response that allow transaction retrying
        final BufferedRequest bufferedReq = new BufferedRequest(req, streamFactory);
        return bufferedReq;
    }

    protected BufferedResponse getResponse(final WebScriptResponse resp)
    {
        // create buffered request and response that allow transaction retrying
        final BufferedResponse bufferedRes = new BufferedResponse(resp, memoryThreshold);
        return bufferedRes;
    }

    public abstract void execute(final Api api, WebScriptRequest req, WebScriptResponse res) throws IOException;

}
