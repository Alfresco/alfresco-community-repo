/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.model;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.PropertyCheck;

/**
 * Returns a map of namespace {@code URI -> prefix} for every namespace registered in the repository's {@link NamespaceService}.
 */
public class NamespacePrefixGet extends AbstractWebScript implements InitializingBean
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NamespacePrefixGet.class);

    private static final String KEY_PREFIX_URI_MAP = "prefixUriMap";

    // Thread-safe once configured, so shared as a single instance across concurrent requests.
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private NamespaceService namespaceService;

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        byte[] payload;
        try
        {
            Map<String, String> prefixUriMap = new HashMap<>();
            for (String prefix : namespaceService.getPrefixes())
            {
                String uri = namespaceService.getNamespaceURI(prefix);
                if (uri == null)
                {
                    continue;
                }
                String existing = prefixUriMap.put(uri, prefix);
                if (existing != null && !existing.equals(prefix))
                {
                    LOGGER.warn(String.format(
                            "Namespace URI '%s' is registered under multiple prefixes; replacing '%s' with '%s'.",
                            uri, existing, prefix));
                }
            }

            payload = OBJECT_MAPPER.writeValueAsBytes(Map.of(KEY_PREFIX_URI_MAP, prefixUriMap));
        }
        catch (JsonProcessingException e)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Failed to write namespace prefix map JSON", e);
        }

        res.setContentType("application/json");
        res.setContentEncoding(StandardCharsets.UTF_8.name());
        try (OutputStream os = res.getOutputStream())
        {
            os.write(payload);
        }
    }
}
