/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.core.swagger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.utility.exception.TestConfigurationException;
import org.apache.commons.io.FilenameUtils;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;

/**
 * Handles all
 * <code>Entry<String, io.swagger.models.Path> path : swagger.getPaths().entrySet()</code>
 * 
 * @author Paul Brodner
 */
public class SwaggerPaths
{
    private Swagger swagger;
    private BufferedWriter fileWithMissingRequests;
    private BufferedWriter fileWithImplementedRequests;
    private String swaggerFilePath;
    private int implementedRequestCount = 0;
    private int missingRequestCount = 0;

    public SwaggerPaths(Swagger swagger, String swaggerFilePath)
    {
        this.swagger = swagger;
        this.swaggerFilePath = swaggerFilePath;
    }

    /**
     * Compare requests that exist in swagger yaml file vs request implemented in your code
     * any findings are saved to a missing-request txt file.
     *
     * @throws TestConfigurationException
     */
    public void computeCoverage()
    {
        try
        {
            System.out.println("Start computing the coverage of TAS vs Swagger file. Stand by...");
            File missingReq = new File(String.format("missing-requests-%s.txt", FilenameUtils.getBaseName(swaggerFilePath)));
            missingReq.delete();
            fileWithMissingRequests = new BufferedWriter(new FileWriter(missingReq));
            fileWithMissingRequests.write(String.format("BasePath: {%s}", swagger.getBasePath()));
            fileWithMissingRequests.newLine();
            fileWithMissingRequests.write("These requests generated should be analyzed and modified according to your needs.");
            fileWithMissingRequests.newLine();
            fileWithMissingRequests.write("PLEASE UPDATE your 'RestReturnedModel' name with the appropiate returned model by your request.");
            fileWithMissingRequests.newLine();
            fileWithMissingRequests.newLine();

            File implReq = new File(String.format("implemented-requests-%s.txt", FilenameUtils.getBaseName(swaggerFilePath)));
            implReq.delete();
            fileWithImplementedRequests = new BufferedWriter(new FileWriter(implReq));

            for (Entry<String, io.swagger.models.Path> path : swagger.getPaths().entrySet())
            {
                for (Map.Entry<HttpMethod, Operation> operation : path.getValue().getOperationMap().entrySet())
                {
                    searchPattern(path.getKey(), operation);
                }
            }

            System.out.println(toString());

            fileWithImplementedRequests.close();
            fileWithMissingRequests.close();

            if (missingRequestCount > 0)
            {
                System.out.println("[ERROR] PLEASE ANALYSE THE GENERATED <missing-requests> file(s), it seems some request were NOT implemented!");
            }
            else
                missingReq.delete();

            System.out.println("ALSO ANALYZE <implemented-requests.txt> for current implementation, take a look at duplicated requests if any!");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Exception while trying to create coverage report.", e);
        }
    }

    /**
     * Use RegExp to check for requests in code, line by line: no further algorithm performance analysis required at this time
     * 
     * @param httpMethod
     * @param pathUrl
     * @param methodName
     */
    private void searchPattern(String pathUrl, Entry<HttpMethod, Operation> operation)
    {
        String originalPathUrl = pathUrl;
        String httpMethod = operation.getKey().name();
                
        /* update path url, removing first "/" as implemented in TAS requests. */
        if (pathUrl.startsWith("/"))
            pathUrl = pathUrl.substring(1, pathUrl.length());

        if (pathUrl.contains("{"))
            pathUrl = pathUrl.replace("{", "\\{");

        /*
         * if in code we have something like: <code> "(HttpMethod.GET, "process-definitions?{parameters}" </code>
         * our regular expression will search for text insider the 'HttpMethod.GET' concatenated with found 'pathUrl" until the optional double brackets
         * RegExp: .*HttpMethod.%s.*\\\"%s\\\"?.*
         * Result: .*HttpMethod.GET."process-definition".* - if this line is found we have a match
         */
        String patternRegEx = String.format(".*HttpMethod.%s.*\\\"%s\\\"?.*", httpMethod, pathUrl);

        // all request are saved under this directory, but limited to the rest/request folder
        File project = Paths.get(".").toAbsolutePath().normalize().toFile();
        Path requestsPath = Paths.get(project.getPath(), "src/main/java/org/alfresco/rest/requests");

        BufferedReader br;
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(requestsPath))
        {
            boolean found = false;

            for (Path path : directoryStream)
            {
                if (Files.isRegularFile(path) && Files.isReadable(path))
                {
                    String line;
                    InputStream fis = new FileInputStream(path.toFile());
                    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                    br = new BufferedReader(isr);

                    while ((line = br.readLine()) != null)
                    {

                        if (line.matches(patternRegEx))
                        {
                            // log("OK - Pattern %-60s found in: {%s} file." , originalPathUrl, path.getFileName());
                            fileWithImplementedRequests.write(String.format("%-10s %-60s %s", httpMethod, pathUrl, path.getFileName()));
                            fileWithImplementedRequests.newLine();
                            fileWithImplementedRequests.flush();
                            implementedRequestCount += 1;
                            found = true;
                        }
                    }
                    br.close();
                }
            }

            if (!found)
            {
                fileWithMissingRequests.write(String.format("%-10s %-60s %s", httpMethod, originalPathUrl, patternRegEx));
                fileWithMissingRequests.newLine();
                
                SwaggerRequest swaggerReqModel = new SwaggerRequest(httpMethod, pathUrl, operation.getValue());
                fileWithMissingRequests.write(swaggerReqModel.getRequestSample());
                fileWithMissingRequests.flush();
                missingRequestCount += 1;
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("|\n").append("|------------------------------------------------------------------------\n").append("COVERAGE: ")
                .append(swaggerFilePath).append("\n");

        int percentage = (implementedRequestCount * 100) / (implementedRequestCount + missingRequestCount);
        sb.append("\t\tImplemented:\t").append(String.valueOf(percentage)).append("% [# ").append(implementedRequestCount).append("]\t Missing: ");

        percentage = (missingRequestCount * 100) / (implementedRequestCount + missingRequestCount);
        sb.append(String.valueOf(percentage)).append("% [# ").append(missingRequestCount).append("]");
        return sb.toString();
    }
}
