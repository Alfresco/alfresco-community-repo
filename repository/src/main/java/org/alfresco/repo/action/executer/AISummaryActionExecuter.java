/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.repo.action.executer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.rendition2.SynchronousTransformClient;
import org.alfresco.repo.rendition2.TransformationOptionsConverter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.namespace.QName;

public class AISummaryActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "ai-action";
    private static final String TARGET_MIMETYPE = "text/plain";
    private String AI_ENDPOINT_URL;

    private DictionaryService dictionaryService;
    private ContentService contentService;
    private NodeService nodeService;
    private SynchronousTransformClient synchronousTransformClient;
    private TransformationOptionsConverter converter;

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSynchronousTransformClient(SynchronousTransformClient synchronousTransformClient)
    {
        this.synchronousTransformClient = synchronousTransformClient;
    }

    public void setConverter(TransformationOptionsConverter converter)
    {
        this.converter = converter;
    }

    public void setAI_ENDPOINT_URL(String aiUrl)
    {
        this.AI_ENDPOINT_URL = aiUrl;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // No extra parameters for this action
        paramList.add(new ParameterDefinitionImpl("Prompt", DataTypeDefinition.TEXT,
                true, getParamDisplayLabel("Prompt")));
    }

    @Override
    protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        if (!this.nodeService.exists(actionedUponNodeRef))
        {
            // node doesn't exist - can't do anything
            return;
        }
        // First check that the node is a sub-type of content
        QName typeQName = this.nodeService.getType(actionedUponNodeRef);
        if (!this.dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        {
            // it is not content, so can't transform
            return;
        }

        ContentReader contentReader = this.contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
        if (contentReader == null || !contentReader.exists())
        {
            throw new RuleServiceException("Content Reader not found.");
        }

        String sourceMimetype = contentReader.getMimetype();
        long sourceSizeInBytes = contentReader.getSize();
        String contentUrl = contentReader.getContentUrl();

        TransformationOptions transformationOptions = new TransformationOptions(
                actionedUponNodeRef, ContentModel.PROP_NAME, null, ContentModel.PROP_NAME);
        transformationOptions.setUse(Thread.currentThread().getName().contains("Async") ? "asyncRule" : "syncRule");

        Map<String, String> options = converter.getOptions(transformationOptions, sourceMimetype, TARGET_MIMETYPE);

        if (!synchronousTransformClient.isSupported(sourceMimetype, sourceSizeInBytes,
                contentUrl, TARGET_MIMETYPE, options, null, actionedUponNodeRef))
        {
            throw new RuleServiceException("No transformer for " + sourceMimetype + " -> " + TARGET_MIMETYPE);
        }

        // Write transformed content to a temp writer
        ContentWriter tempWriter = contentService.getTempWriter();
        tempWriter.setMimetype(TARGET_MIMETYPE);
        tempWriter.setEncoding(contentReader.getEncoding());

        synchronousTransformClient.transform(contentReader, tempWriter, options, null, actionedUponNodeRef);

        // Get the AI prompt from action parameters
        String aiPrompt = (String) ruleAction.getParameterValue("Prompt");
        // Read transformed content as plain text
        ContentReader txtReader = tempWriter.getReader();
        try (InputStream is = txtReader.getContentInputStream())
        {
            String textString = new String(is.readAllBytes());
            String aiResult = sendToAIEndpoint(textString, aiPrompt);
            nodeService.setProperty(actionedUponNodeRef, ContentModel.PROP_AI_RESPONSE, aiResult);
            if (!nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_AI))
            {
                nodeService.addAspect(actionedUponNodeRef, ContentModel.ASPECT_AI, null);
            }
            // Optionally, store or log the result
        }
        catch (Exception e)
        {
            throw new RuleServiceException("AI endpoint call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Placeholder for sending content to an AI endpoint. Implement actual HTTP call or integration as needed.
     */
    private String sendToAIEndpoint(String txtContent, String prompt) throws Exception
    {
        // Read input stream to string

        // Build JSON payload
        String payload = "{"
                + "\"context\": " + escapeJson(txtContent.trim()) + ","
                + "\"prompt\": " + escapeJson(prompt.trim())
                + "}";

        // Create connection
        URL url = new URL(AI_ENDPOINT_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Send request
        try (OutputStream os = conn.getOutputStream())
        {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        // Read response
        int status = conn.getResponseCode();
        InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        String jsonResponse = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);

        // Parse JSON and extract the "response" field
        org.json.JSONObject obj = new org.json.JSONObject(jsonResponse);
        String summary = obj.optString("response", "");
        return summary.trim();
    }

    // Helper to escape JSON string
    private String escapeJson(String text)
    {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
