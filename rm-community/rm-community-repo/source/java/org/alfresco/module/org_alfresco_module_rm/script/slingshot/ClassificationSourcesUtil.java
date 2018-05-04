/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.script.slingshot;


import static org.alfresco.model.ContentModel.PROP_NODE_UUID;
import static org.alfresco.service.namespace.QName.createQName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * Method to replace the plain text classification source name with all possible nodeRefs during record search
 * @author Ross Gale
 * @since 2.7
 */
public class ClassificationSourcesUtil extends SearchUtil
{


    public static final String CS_URI = "http://www.alfresco.org/model/classificationsources/1.0";
    public static final QName CLASSIFICATION_SOURCES_CONTAINER = createQName(CS_URI, "classificationSourcesContainer");
    public static final QName PROP_CLASSIFICATION_SOURCE_NAME = createQName(CS_URI, "classificationSourceName");
    public static final String SOURCES_KEY = "cs:appliedSources:";
    public static final String START = "start";
    public static final String END = "end";

    /**
     * Replace plain text source name with all matching nodeRefs
     *
     * @param searchQuery String e.g. clf:classificationReasons:"Other source"
     * @return String e.g. (cs:appliedSources:5cc6d344-fa94-4370-9c81-d947b7e8f2ac OR cs:appliedSources:47afd476-358f-4007-a35e-8f83adb06523)
     */
    public String replaceSourceNameWithNodeRef(String searchQuery)
    {
        Pattern pattern = Pattern.compile("cs:appliedSources:\"[^\"]*\"");
        Matcher matcher = pattern.matcher(searchQuery);
        StringBuilder builder = new StringBuilder(searchQuery);
        Map<Integer,Map<String, Integer>> index = new HashMap<>();
        int count = 0;
        //create a map of where the strings to replace are
        while(matcher.find())
        {
            index.put(count, new HashMap<>());
            index.get(count).put(START,matcher.start());
            index.get(count).put(END, matcher.end());
            count++;
        }
        //Go through the string in reverse and replace the plain text reference with nodeIds
        for(int i = index.size(); i > 0; i--)
        {
            Map<String, Integer> element = index.get(i-1);
            int start = element.get(START);
            int end = element.get(END);
            builder.replace(start, end, replaceSingleInstance(searchQuery.substring(start, end)));
        }

        return builder.toString();
    }

    private String replaceSingleInstance(String str)
    {
        StringBuilder stringBuilder = new StringBuilder();
        if (str.contains(SOURCES_KEY))
        {
            boolean multipleResults = false;
            stringBuilder.append('(');
            for (String sourceId : retrieveAllNodeIds(getRootContainer(CLASSIFICATION_SOURCES_CONTAINER)))
            {
                NodeRef reasonNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, sourceId);
                Map<QName, Serializable> properties = nodeService.getProperties(reasonNodeRef);
                if (str.equals(SOURCES_KEY + "\"" + properties.get(PROP_CLASSIFICATION_SOURCE_NAME).toString() + "\""))
                {
                    if (multipleResults)
                    {
                        stringBuilder.append(" OR ");
                    }
                    stringBuilder.append(SOURCES_KEY + "\"" + properties.get(PROP_NODE_UUID).toString() + "\"");
                    //Sources create a node each time even if all the details are the same this will allow multiple nodeIds to be added for a single string
                    multipleResults = true;
                }
            }
        }
        stringBuilder.append(')');
        return stringBuilder.toString();
    }


}
