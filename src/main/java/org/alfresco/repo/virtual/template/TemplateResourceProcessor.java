/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.virtual.template;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.repo.virtual.VirtualContext;
import org.alfresco.repo.virtual.ref.ClasspathResource;
import org.alfresco.repo.virtual.ref.RepositoryResource;
import org.alfresco.repo.virtual.ref.Resource;
import org.alfresco.repo.virtual.ref.ResourceProcessingError;
import org.alfresco.repo.virtual.ref.ResourceProcessor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * Executes JavaScript virtual folders templates ensuring their required context and the
 * {@link VirtualFolderDefinition} post execution JSON map result unmarshalling.
 * 
 * @author Bogdan Horje
 */
public class TemplateResourceProcessor implements ResourceProcessor<VirtualFolderDefinition>
{
    private static final String ID_KEY = "id";

    private static final String NAME_KEY = "name";

    private static final String DESCRIPTION_KEY = "description";

    private static final String NODES_KEY = "nodes";

    private static final String SEARCH_KEY = "search";

    private static final String LANGUAGE_KEY = "language";

    private static final String QUERY_KEY = "query";

    private static final String STORE_KEY = "store";

    private static final String FILING_KEY = "filing";

    private static final String CLASSIFICATION_KEY = "classification";

    private static final String TYPE_KEY = "type";

    private static final String ASPECTS_KEY = "aspects";

    private static final String PROPERTIES_KEY = "properties";

    private static final String PATH_KEY = "path";

    private VirtualContext context;

    public TemplateResourceProcessor(VirtualContext context)
    {
        super();
        this.context = context;
    }

    @Override
    public VirtualFolderDefinition process(Resource resource) throws ResourceProcessingError
    {
        ParameterCheck.mandatory("resource",
                                 resource);

        if (resource instanceof ClasspathResource)
        {
            return process((ClasspathResource) resource);
        }
        else if (resource instanceof RepositoryResource)
        {
            return process((RepositoryResource) resource);
        }
        else
        {
            throw new ResourceProcessingError("Unsupported resource class " + resource.getClass());
        }
    }

    /**
     * Processes the JavaScript template given by the {@link ClasspathResource}
     * and creates a {@link VirtualFolderDefinition} (composite) structure that
     * represents the virtual folder.
     * 
     * @param A {@link ClasspathResource} reference.
     * @return A {@link VirtualFolderDefinition} reference that represents the
     *         structure (tree of nodes) of the virtual folder.
     */
    @SuppressWarnings("unchecked")
    @Override
    public VirtualFolderDefinition process(ClasspathResource classpath) throws ResourceProcessingError
    {
        Object result;
        try
        {
            result = context.getActualEnviroment().executeScript(classpath.getClasspath(),
                                                                 createScriptParameters(context));
            return asVirtualStructure(context,
                                      (Map<String, Object>) result);
        }
        catch (ActualEnvironmentException e)
        {
            throw new ResourceProcessingError(e);
        }
    }

    /**
     * Processes the JavaScript template given by the {@link RepositoryResource}
     * and creates a {@link VirtualFolderDefinition} (composite) structure that
     * represents the virtual folder.
     * 
     * @param A {@link RepositoryResource} reference.
     * @return A {@link VirtualFolderDefinition} reference that represents the
     *         structure (tree of nodes) of the virtual folder.
     */
    @SuppressWarnings("unchecked")
    @Override
    public VirtualFolderDefinition process(RepositoryResource repositoryResource) throws ResourceProcessingError
    {
        Object result;
        try
        {
            NodeRef templateNodeRef = repositoryResource.getLocation().asNodeRef(context.getActualEnviroment());
            result = context.getActualEnviroment().executeScript(templateNodeRef,
                                                                 createScriptParameters(context));
            return asVirtualStructure(context,
                                      (Map<String, Object>) result);
        }
        catch (ActualEnvironmentException e)
        {
            throw new ResourceProcessingError(e);
        }
    }

    /**
     * Adds the script virtual context to the parameters of the context.
     * 
     * @param context A {@link VirtualContext} reference.
     * @return A map of context parameters.
     * @throws ActualEnvironmentException
     */
    protected Map<String, Object> createScriptParameters(VirtualContext context) throws ActualEnvironmentException
    {
        Map<String, Object> parameters = context.getParameters();
        Object scriptContext = context.getActualEnviroment().createScriptVirtualContext(context);
        parameters.put(VirtualContext.CONTEXT_PARAM,
                       scriptContext);
        return parameters;
    }

    /**
     * Creates a filing rule based on the criteria (path, classification: type,
     * aspects) given in the template.
     * 
     * @param context The context in which the virtualization process takes
     *            place.
     * @param filing A map containing the filing criteria used to create the
     *            rule.
     * @return A {@link FilingRule} reference.
     * @throws ResourceProcessingError
     */
    @SuppressWarnings("unchecked")
    protected FilingRule asFilingRule(VirtualContext context, Object filing) throws ResourceProcessingError
    {
        if (filing == null)
        {
            return null;
        }

        Map<String, Object> filingMap = (Map<String, Object>) filing;

        if (filingMap.isEmpty())
        {
            return null;
        }

        String path = (String) filingMap.get(PATH_KEY);

        Map<String, Object> classificationMap = (Map<String, Object>) filingMap.get(CLASSIFICATION_KEY);

        String type = null;

        List<String> aspects = null;

        if (classificationMap != null)
        {
            type = (String) classificationMap.get(TYPE_KEY);

            aspects = (List<String>) classificationMap.get(ASPECTS_KEY);
        }

        if (aspects == null)
        {
            aspects = Collections.emptyList();
        }

        Map<String, String> properties = (Map<String, String>) filingMap.get(PROPERTIES_KEY);
        if (properties == null)
        {
            properties = Collections.emptyMap();
        }

        if (path == null && type == null && aspects.isEmpty() && properties.isEmpty())
        {
            return new NullFilingRule(context.getActualEnviroment());
        }
        else
        {
            return new TemplateFilingRule(context.getActualEnviroment(),
                                          path,
                                          type,
                                          new HashSet<String>(aspects),
                                          properties);

        }

    }

    /**
     * Creates a query based on the details: language, store, query statement
     * given in the template.
     * 
     * @param context The context in which the virtualization process takes
     *            place.
     * @param search A map containing the details that define the query:
     *            language, store, query statement.
     * @return an {@link AlfrescoVirtualQuery} reference.
     * @throws ResourceProcessingError
     */
    protected VirtualQuery asVirtualQuery(VirtualContext context, Map<String, Object> search)
                throws ResourceProcessingError
    {
        if (search == null)
        {
            return null;
        }

        Map<String, Object> mapSearch = search;

        String language = (String) mapSearch.get(LANGUAGE_KEY);
        String store = (String) mapSearch.get(STORE_KEY);
        String query = (String) mapSearch.get(QUERY_KEY);

        return new VirtualQueryImpl(store,
                                        language,
                                        query);
    }

    /**
     * Creates a {@link VirtualFolderDefinition} that represents the structure
     * of the virtual folder.
     * 
     * @param context The context in which the virtualization process takes
     *            place.
     * @param result A map containing the details that define the virtual
     *            entries.
     * @return a {@link VirtualFolderDefinition} reference.
     * @throws ResourceProcessingError
     */
    protected VirtualFolderDefinition asVirtualStructure(VirtualContext context, Map<String, Object> result)
                throws ResourceProcessingError
    {
        try
        {
            Map<String, Object> mapResult = result;

            VirtualFolderDefinition virtualStructure = new VirtualFolderDefinition();

            String id = (String) mapResult.get(ID_KEY);
            virtualStructure.setId(id);

            String name = (String) mapResult.get(NAME_KEY);
            virtualStructure.setName(name);

            String description = (String) mapResult.get(DESCRIPTION_KEY);
            virtualStructure.setDescription(description);

            @SuppressWarnings("unchecked")
            VirtualQuery virtualQuery = asVirtualQuery(context,
                                                       (Map<String, Object>) mapResult.get(SEARCH_KEY));
            virtualStructure.setQuery(virtualQuery);

            FilingRule filingRule = asFilingRule(context,
                                                 mapResult.get(FILING_KEY));
            if (filingRule == null)
            {
                filingRule = new NullFilingRule(context.getActualEnviroment());
            }
            virtualStructure.setFilingRule(filingRule);

            @SuppressWarnings("unchecked")
            Map<String, String> properties = (Map<String, String>) mapResult.get(PROPERTIES_KEY);
            if (properties == null)
            {
                properties = Collections.emptyMap();
            }

            virtualStructure.setProperties(properties);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) mapResult.get(NODES_KEY);

            if (nodes != null)
            {
                for (Map<String, Object> node : nodes)
                {
                    VirtualFolderDefinition child = asVirtualStructure(context,
                                                                       node);
                    virtualStructure.addChild(child);
                }
            }

            return virtualStructure;
        }
        catch (ClassCastException e)
        {
            throw new ResourceProcessingError(e);
        }
    }

}
