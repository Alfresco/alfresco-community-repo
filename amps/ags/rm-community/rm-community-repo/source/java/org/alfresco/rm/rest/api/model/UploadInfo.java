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
package org.alfresco.rm.rest.api.model;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Encapsulates the elements of an upload request
 * 
 * @author Ana Bozianu
 * @since 2.6
 */
public class UploadInfo
{
    private String fileName;
    private String nodeType;
    private String relativePath;
    private Content content;
    private Map<String, Object> properties;

    public UploadInfo(FormData formData)
    {
        properties = new HashMap<>();

        for (FormData.FormField field : formData.getFields())
        {
            switch (field.getName().toLowerCase())
            {
                case "name":
                    fileName = getStringOrNull(field.getValue());
                    break;
                case "nodetype":
                    nodeType = getStringOrNull(field.getValue());
                    break;
                case "relativepath":
                    relativePath = getStringOrNull(field.getValue());
                    break;
                case "filedata":
                    if (field.getIsFile())
                    {
                        fileName = (fileName != null ? fileName : field.getFilename());
                        content = field.getContent();
                    }
                    break;

                default:
                {
                    final String propName = field.getName();
                    if (propName.indexOf(QName.NAMESPACE_PREFIX) > -1)
                    {
                        properties.put(propName, field.getValue());
                    }
                }
            }
        }

        if (StringUtils.isBlank(fileName) || content == null)
        {
            throw new InvalidArgumentException("Required parameters are missing");
        }
    }

    private String getStringOrNull(String value)
    {
        if (StringUtils.isNotEmpty(value))
        {
            return value.equalsIgnoreCase("null") ? null : value;
        }
        return null;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public Content getContent()
    {
        return content;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }
}
