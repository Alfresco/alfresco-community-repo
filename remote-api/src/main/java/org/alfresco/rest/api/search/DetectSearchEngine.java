/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.rest.api.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DetectSearchEngine extends AbstractLifecycleBean {
    private static final Log logger = LogFactory.getLog(DetectSearchEngine.class);
    private static final String[] searchEngineUrls = {
            "http://elasticsearch:9200",
            "http://elasticsearch:9200/_license"
    };

    @Override
    protected void onBootstrap(ApplicationEvent event) {
        findSearchEngineInfos();
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
        // No-op
    }

    private static SearchEngineInfo searchEngineInfo(){
        // make http request to find search engine info
        return new SearchEngineInfo();
    }

    private static void findSearchEngineInfos(){
        for(int i = 0; i < searchEngineUrls.length; i++){
            HttpResponse<String> response = makeHttpRequest(searchEngineUrls[i]);
            logger.info("Search Engine response: " + response);
        }
    }

    private static HttpResponse<String> makeHttpRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try (HttpClient client = HttpClient.newHttpClient()){
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException("HTTP request to get the search engine info failed", e);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("HTTP request to get the search engine info was interrupted", e);
        }
    }
}
