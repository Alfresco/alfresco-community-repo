/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.rm.community.util;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;

import lombok.Getter;
import lombok.Setter;
import org.alfresco.utility.Utility;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Helper class for interaction with docker containers
 *
 * @author Claudia Agache
 * @since 3.1
 */
@Service
public class DockerHelper
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerHelper.class);
    private static final String REPO_IMAGE_NAME = "repository";
    @Getter
    @Setter
    private DockerClient dockerClient;

    @Autowired
    public DockerHelper(@Value ("${docker.host}") String dockerHost)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            this.dockerClient = DockerClientBuilder
                .getInstance(dockerHost)
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
        }
        else
        {
            this.dockerClient = DockerClientBuilder
                .getInstance()
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
        }
    }

    /**
     * Method for returning logs of docker container
     *
     * @param containerId - ID of the container
     * @param timeStamp - get the logs since a specific timestamp
     * @return list of strings, where every string is log line
     */
    private List<String> getDockerLogs(String containerId, int timeStamp)
    {
        final List<String> logs = new ArrayList<>();

        final LogContainerCmd logContainerCmd = getDockerClient().logContainerCmd(containerId);
        logContainerCmd.withStdOut(true)
                       .withStdErr(true)
                       .withSince(timeStamp) // UNIX timestamp to filter logs. Output log-entries since that timestamp.
                       .withTimestamps(true); //print timestamps for every log line

        try
        {
            logContainerCmd.exec(new LogContainerResultCallback()
            {
                @Override
                public void onNext(Frame item)
                {
                    logs.add(item.toString());
                }
            }).awaitCompletion();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();  // set interrupt flag
            LOGGER.error("Failed to retrieve logs of container " + containerId, e);
        }

        return logs;
    }

    /**
     * Get the alfresco container logs
     *
     * @return list of strings, where every string is log line
     */
    public List<String> getAlfrescoLogs()
    {
        final List<Container> alfrescoContainers = findContainersByImageName(REPO_IMAGE_NAME);
        if (alfrescoContainers.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            List<String> alfrescoLogs = new ArrayList<>();
            // get the logs since current time - 10 seconds
            final int timeStamp = (int) (System.currentTimeMillis() / 1000) - 10;
            alfrescoContainers.forEach(alfrescoContainer -> alfrescoLogs.addAll(getDockerLogs(alfrescoContainer.getId(), timeStamp)));
            return alfrescoLogs;
        }
    }

    /**
     * Helper method to check if the specified exception is thrown in alfresco logs
     *
     * @param expectedException the expected exception to be thrown
     * @throws Exception
     */
    public void checkExceptionIsInAlfrescoLogs(String expectedException) throws Exception
    {
        //Retry the operation because sometimes it takes few seconds to throw the exception
        Utility.sleep(6000, 30000, () ->
        {
            List<String> alfrescoLogs = getAlfrescoLogs();
            assertTrue(alfrescoLogs.stream().anyMatch(logLine -> logLine.contains(expectedException)));
        });
    }

    /**
     * Method for finding docker containers after the image name
     *
     * @param imageName - the name of the image used by container
     * @return the containers
     */
    private List<Container> findContainersByImageName(String imageName)
    {
        final List<Container> containers = getDockerClient().listContainersCmd().withShowAll(true).exec();

        return containers.stream()
                         .filter(container -> container.getImage().contains(imageName))
                         .collect(Collectors.toList());
    }
}
