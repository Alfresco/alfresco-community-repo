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

package org.alfresco.repo.action.parameter;

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

    /** configured node parameter processor aspect and type names, comma separated */
    private String nodeParameterProcessorAspectsNames;

    /** node parameter processor */
    private NodeParameterProcessor nodeParameterProcessor;

    /**
     * @param nodeParameterProcessorAspectsNames map of record metadata aspects against file plan types
     */
    public void setNodeParameterProcessorAspects(String nodeParameterProcessorAspectsNames)
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
            String[] aspectsNames = this.nodeParameterProcessorAspectsNames.split(",");
            for (String name : aspectsNames)
            {
                if((name != null) && !"".equals(name.trim()))
                {
                    // convert to qname and save it
                    QName aspect = QName.createQName(name.trim(), namespaceService);

                    // register with node parameter processor
                    this.nodeParameterProcessor.addSuggestionDefinition(aspect);
                }
            }
        }
    }
}
