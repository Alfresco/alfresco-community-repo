/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.search.impl.elasticsearch.admin;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.Request;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.generic.Response;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SearchEngineDetectorDebugTest {
    private static final String ES_IMAGE  = "docker.elastic.co/elasticsearch/elasticsearch:8.17.0";
    private static final String OS_IMAGE  = "opensearchproject/opensearch:2.17.0";

    private static final GenericContainer<?> container = new GenericContainer<>(ES_IMAGE)
            .withExposedPorts(9200)
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms1g -Xmx1g")
            .waitingFor(Wait.forHttp("/").forPort(9200).forStatusCode(200))
            .withStartupTimeout(Duration.ofMinutes(3));

    private ElasticsearchHttpClientFactory factory;

    @Before
    public void setUp(){
        container.start();

        String host = container.getHost();
        int port = container.getMappedPort(9200);
        System.out.println("Search engine reachable at http://" + host + ":" + port + "/");

        factory = new ElasticsearchHttpClientFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setBaseUrl("/");
        factory.setSecureComms("none");
        factory.setUser("");
        factory.setMaxTotalConnections(10);
        factory.setMaxHostConnections(10);
        factory.setConnectionTimeout(5_000);
        factory.setSocketTimeout(30_000);
        factory.setResponseTimeout(30_000);
        factory.setThreadCount(1);
    }

    @After
    public void tearDown(){
        if (factory != null) {
            factory.destroy();
        }
        container.stop();
    }

    @Test
    public void rawRootRequest() throws Exception {
        String url = "http://" + container.getHost() + ":" + container.getMappedPort(9200) + "/";
        HttpResponse<String> resp = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("status = " + resp.statusCode());
        System.out.println("raw body = \n" + resp.body());

        JSONObject root = new JSONObject(resp.body());
        System.out.println("version.number = " + root.getJSONObject("version").getString("number"));
    }

    @Test
    public void reproduceDetectStepByStep() throws Exception {
        Request request = Requests.builder().method("GET").endpoint("/").build();

        try(Response response = factory.getElasticsearchClient().generic().execute(request)) {
            String raw  = response.getBody()
                            .map(Body::bodyAsString)
                    .orElseThrow(() -> new IOException("Empty response from root endpoint"));
            System.out.println(raw);
            JSONObject root = new JSONObject(raw);
            JSONObject version = root.getJSONObject("version");
            System.out.println("Version: " + version);
            System.out.println("Version number: " + version.getString("number"));
//            System.out.println(Assert.assertTrue(raw.contains()););
            System.out.println("Here it fails!!");
//            System.out.println(root);
//            JSONObject version  = root.getJSONObject("version");
//            String number       = version.getString("number");
//            String distribution = version.optString("distribution", null); // OpenSearch only
//            System.out.println("Search engine root response: " + root.toString());
//            String provider = (distribution != null && distribution.equalsIgnoreCase("opensearch"))
//                    ? "OpenSearch" : "Elasticsearch";
//            System.out.println("Search engine: " + provider + " version: " + number);
        }
    }

    private static void detectSearchEngines(){
        String[] urls = {
            ""
        };
    }
}
