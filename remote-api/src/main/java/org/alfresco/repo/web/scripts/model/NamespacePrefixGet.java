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
import java.util.TreeMap;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.PropertyCheck;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Returns a map of namespace {@code URI -> prefix} for every namespace registered in the repository's
 * {@link NamespaceService}. This is the supported, product replacement for the unofficial prefix-mapping
 * extension the Search Enterprise Re-Indexer previously relied on (see ACS-12299 / PRODMAN-840).
 * <p>
 * Unlike the {@code /types} and {@code /aspects} public REST endpoints, this sources directly from
 * {@link NamespaceService}, so it returns the complete registered set — including platform namespaces such as
 * {@code sys} and {@code module} that have no enumerable type/aspect.
 * <p>
 * The response deliberately matches the shape of the legacy prefixes file so callers (and the re-indexer's
 * on-disk cache) can consume it unchanged.
 *
 * Authentication follows the same model as the data-model API (an ordinary authenticated user; no admin role
 * required) — enforced by the {@code user} authentication declared in the web script descriptor.
 */
public class NamespacePrefixGet extends AbstractWebScript implements InitializingBean
{
    private static final String KEY_PREFIX_URI_MAP = "prefixUriMap";

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
        // Sorted for deterministic, cache/diff-friendly output.
        TreeMap<String, String> prefixUriMap = new TreeMap<>();
        for (String prefix : namespaceService.getPrefixes())
        {
            prefixUriMap.put(namespaceService.getNamespaceURI(prefix), prefix);
        }

        res.setContentType("application/json");
        res.setContentEncoding(StandardCharsets.UTF_8.name());

        try (OutputStream os = res.getOutputStream())
        {
            JSONObject map = new JSONObject();
            for (var entry : prefixUriMap.entrySet())
            {
                map.put(entry.getKey(), entry.getValue());
            }
            JSONObject json = new JSONObject();
            json.put(KEY_PREFIX_URI_MAP, map);
            os.write(json.toString(2).getBytes(StandardCharsets.UTF_8));
        }
        catch (JSONException e)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Failed to write namespace prefix map JSON", e);
        }
    }
}
