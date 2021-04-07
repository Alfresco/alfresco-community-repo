/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import lombok.Getter;
import lombok.Setter;
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
            this.dockerClient = DockerClientBuilder.getInstance(dockerHost).build();
        }
        else
        {
            this.dockerClient = DockerClientBuilder.getInstance().build();
        }
    }

    /**
     * Method for returning logs of docker container
     *
     * @param containerId - ID of the container
     * @return list of strings, where every string is log line
     */
    private List<String> getDockerLogs(String containerId)
    {
        final List<String> logs = new ArrayList<>();
        // get the logs since current time - 10 seconds
        final int timeStamp = (int) (System.currentTimeMillis() / 1000) - 10;

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
        final Optional<Container> alfrescoContainer = findContainerByImageName(REPO_IMAGE_NAME);
        return (alfrescoContainer.isPresent()) ? getDockerLogs(alfrescoContainer.get().getId()) : Collections.emptyList();
    }

    /**
     * Method for finding a docker container after the image name
     *
     * @param imageName - the name of the image used by container
     * @return the container
     */
    private Optional<Container> findContainerByImageName(String imageName)
    {
        final List<Container> containers = getDockerClient().listContainersCmd().withShowAll(true).exec();

        return containers.stream()
                         .filter(container -> container.getImage().contains(imageName))
                         .findFirst();
    }
}
