/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.action.parameter;

import java.util.List;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Record metadata bootstrap bean.
 * <p>
 * This method of bootstrapping record metadata aspects into the RecordService deprecates the
 * previous practice of extending rma:recordMetaData.
 *
 * @author Mark Hibbins
 * @since 2.2
 */
public class NodeParameterSuggesterBootstrap
{
    /** namespace service */
    private NamespaceService namespaceService;

    /** map of record metadata aspects against file plan type */
    private List<String> nodeParameterProcessorAspectsNames;

    /** node parameter processor */
    private NodeParameterProcessor nodeParameterProcessor;

    /**
     * @param recordMetadataAspects map of record metadata aspects against file plan types
     */
    public void setNodeParameterProcessorAspects(List<String> nodeParameterProcessorAspectsNames)
    {
        this.nodeParameterProcessorAspectsNames = nodeParameterProcessorAspectsNames;
    }

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param nodeParameterProcessor  Node parameter processor
     */
    public void setNodeParameterProcessor(NodeParameterProcessor nodeParameterProcessor)
    {
        this.nodeParameterProcessor = nodeParameterProcessor;
    }

    /**
     * Init method
     */
    public void init()
    {
        ParameterCheck.mandatory("namespaceService", namespaceService);

        if (nodeParameterProcessorAspectsNames != null)
        {
            for (String name : nodeParameterProcessorAspectsNames)
            {
                // convert to qname and save it
                QName aspect = QName.createQName(name, namespaceService);

                // register with node parameter processor
                this.nodeParameterProcessor.addSuggestionDefinition(aspect);
            }
        }
    }
}
