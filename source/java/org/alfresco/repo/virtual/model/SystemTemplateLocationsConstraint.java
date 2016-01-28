/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.virtual.config.NodeRefExpression;
import org.alfresco.repo.virtual.store.SystemVirtualizationMethod;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A system paths string values constraint to be used in conjunction with
 * {@link SystemVirtualizationMethod}s to provide available system paths by
 * exploring a predefined location in the java classpath ad a predefined
 * repository location.
 *
 * @author Silviu Dinuta
 */
public class SystemTemplateLocationsConstraint extends ListOfValuesConstraint
{
    public static final String NULL_SYSTEM_TEMPLATE = "nullSystemTemplate";

    public static final String NULL_SYSTEM_TEMPLATE_MESSAGE = "sf_smartFolder.nullSystemTemplate.message";

    private NodeService nodeService;

    private String templatesParentClasspath;

    private NodeRefExpression templatesParentRepositoryPath;

    private NamespacePrefixResolver namespacePrefixResolver;

    private String repositoryTemplateTypeName;

    public SystemTemplateLocationsConstraint()
    {
        super();
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setTemplatesParentClasspath(String templatesParentClasspath)
    {
        this.templatesParentClasspath = templatesParentClasspath;
    }

    public String getTemplatesParentClasspath()
    {
        return this.templatesParentClasspath;
    }

    public NodeRefExpression getTemplatesParentRepositoryPath()
    {
        return this.templatesParentRepositoryPath;
    }

    public void setTemplatesParentRepositoryPath(NodeRefExpression templatesParentRepositoryPath)
    {
        this.templatesParentRepositoryPath = templatesParentRepositoryPath;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setRepositoryTemplateTypeName(String repositoryTemplateTypeName)
    {
        this.repositoryTemplateTypeName = repositoryTemplateTypeName;
    }

    @Override
    public List<String> getRawAllowedValues()
    {
        List<String> result = null;
        try
        {
            result = loadClasspathTemplates(templatesParentClasspath,
                                            "json");
        }
        catch (IOException e)
        {
            throw new ConstraintException("ListTemplateTypesConstraints",
                                          e);
        }
        List<String> repositoryTemplates = loadRepositoryTemplates(templatesParentRepositoryPath);
        result.addAll(repositoryTemplates);
        if (result.size() == 0)
        {
            result.add(NULL_SYSTEM_TEMPLATE);
        }
        super.setAllowedValues(result);
        return result;
    }

    @Override
    public List<String> getAllowedValues()
    {
        if (sorted == true)
        {
            List<String> rawValues = getRawAllowedValues();
            List<String> values = new ArrayList<String>(rawValues);
            Collections.sort(values,
                             new LabelComparator());
            return values;
        }
        else
        {
            return super.getAllowedValues();
        }
    }

    @Override
    public String getDisplayLabel(String constraintAllowableValue, MessageLookup messageLookup)
    {
        if (constraintAllowableValue.startsWith("N"))
        {
            Serializable nameProperty = nodeService.getProperty(new NodeRef(constraintAllowableValue.substring(1)),
                                                                ContentModel.PROP_NAME);
            return nameProperty.toString();
        }
        else if (constraintAllowableValue.equals(SystemTemplateLocationsConstraint.NULL_SYSTEM_TEMPLATE))
        {
            String message = messageLookup.getMessage(SystemTemplateLocationsConstraint.NULL_SYSTEM_TEMPLATE_MESSAGE,
                                                      I18NUtil.getLocale());
            return message == null ? constraintAllowableValue : message;
        }
        else
        {
            return constraintAllowableValue.substring(constraintAllowableValue.lastIndexOf("/") + 1);
        }
    }

    @Override
    protected void evaluateSingleValue(Object value)
    {
        super.setAllowedValues(getAllowedValues());
        super.evaluateSingleValue(value);
    }

    private List<String> loadClasspathTemplates(String templatesParentClasspath, String... extensions)
                throws IOException
    {
        List<String> result = new ArrayList<String>(5);
        List<String> files = new ArrayList<String>(5);

        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource[] resources = resolver.getResources("classpath*:" + templatesParentClasspath + "/*");
        for (Resource resource : resources)
        {
            files.add(resource.getFilename());
        }

        if (extensions != null && extensions.length > 0)
        {

            String extensionStr = "";
            for (int i = 0; i < extensions.length; i++)
            {
                if (i == extensions.length - 1)
                {
                    extensionStr += extensions[i];
                }
                else
                {
                    extensionStr += extensions[i] + "|";
                }
            }
            String fileExtensions = "(?i).*\\.(" + extensionStr + ")$";
            Pattern pattern = Pattern.compile(fileExtensions);
            for (String file : files)
            {
                if (pattern.matcher(file).matches())
                {
                    result.add("C" + templatesParentClasspath + "/" + file);
                }
            }
        }
        return result;
    }

    private List<String> loadRepositoryTemplates(NodeRefExpression templatesParentRepositoryPath)
    {
        List<String> result = new ArrayList<String>();
        NodeRef findNodeRef = templatesParentRepositoryPath.resolve();

        if (findNodeRef != null)
        {
            final QName repositoryTemplateTypeQName = QName.createQName(repositoryTemplateTypeName,
                                                                        namespacePrefixResolver);
            Set<QName> searchTypeQNames = new HashSet<QName>();
            searchTypeQNames.add(repositoryTemplateTypeQName);

            List<ChildAssociationRef> children = nodeService.getChildAssocs(findNodeRef,
                                                                            searchTypeQNames);
            for (ChildAssociationRef childAssociationRef : children)
            {
                NodeRef childNodeRef = childAssociationRef.getChildRef();
                QName childType = nodeService.getType(childNodeRef);
                if (searchTypeQNames.contains(childType))
                {
                    result.add("N" + childNodeRef.toString());
                }
            }
        }
        return result;
    }

    private class LabelComparator implements Comparator<String>
    {

        @Override
        public int compare(String o1, String o2)
        {
            return getDisplayLabel(o1,
                                   null).compareTo(getDisplayLabel(o2,
                                                                   null));
        }
    }
}
