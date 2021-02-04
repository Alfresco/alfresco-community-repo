/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.rest.api.impl;

import org.alfresco.rest.api.Types;
import org.alfresco.rest.api.ClassDefinitionMapper;
import org.alfresco.rest.api.model.Type;
import org.alfresco.rest.api.model.PropertyDefinition;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypesImpl extends AbstractClassImpl<Type> implements Types
{
    private DictionaryService dictionaryService;
    private NamespacePrefixResolver namespaceService;
    private ClassDefinitionMapper classDefinitionMapper;

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespacePrefixResolver namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setClassDefinitionMapper(ClassDefinitionMapper classDefinitionMapper)
    {
        this.classDefinitionMapper = classDefinitionMapper;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "classDefinitionMapper", classDefinitionMapper);
    }


    @Override
    public CollectionWithPagingInfo<Type> listTypes(Parameters params)
    {
        Paging paging = params.getPaging();
        ModelApiFilter query = getQuery(params.getQuery());
        Stream<QName> typeList = null;

        if (query != null && query.getModelIds() != null)
        {
            validateListParam(query.getModelIds(), PARAM_MODEL_IDS);
            typeList = query.getModelIds().parallelStream().map(this::getModelTypes).flatMap(Collection::parallelStream);
        }
        else if (query != null && query.getParentIds() != null)
        {
            validateListParam(query.getParentIds(), PARAM_PARENT_IDS);
            typeList = query.getParentIds().parallelStream().map(this::getChildTypes).flatMap(Collection::parallelStream);
        }
        else
        {
                typeList = this.dictionaryService.getAllTypes().parallelStream();
        }

        List<Type> allTypes = typeList.filter((qName) -> filterByNamespace(query, qName))
                .map((qName) -> this.convertToType(dictionaryService.getType(qName)))
                .collect(Collectors.toList());
        return createPagedResult(allTypes, paging);
    }

    @Override
    public Type getType(String typeId)
    {
        if (typeId == null)
            throw new InvalidArgumentException("Invalid parameter: unknown scheme specified");

        TypeDefinition typeDefinition = null;

        try
        {
           typeDefinition = dictionaryService.getType(QName.createQName(typeId, this.namespaceService));
        }
        catch (NamespaceException exception)
        {
            throw new EntityNotFoundException(typeId);
        }

        if (typeDefinition == null)
            throw new EntityNotFoundException(typeId);

        return this.convertToType(typeDefinition);
    }

    public Type convertToType(TypeDefinition typeDefinition)
    {
        List<PropertyDefinition> properties = this.classDefinitionMapper.fromDictionaryClassDefinition(typeDefinition, dictionaryService).getProperties();
        return new Type(typeDefinition, dictionaryService, properties);
    }

    private Collection<QName> getModelTypes(String modelId)
    {
        ModelDefinition modelDefinition =  null;

        if (modelId == null)
            throw new InvalidArgumentException("modelId is null");

        try
        {
            modelDefinition =  this.dictionaryService.getModel(QName.createQName(modelId, this.namespaceService));
        }
        catch (NamespaceException exception)
        {
            throw new InvalidArgumentException(exception.getMessage());
        }

        return this.dictionaryService.getTypes(modelDefinition.getName());
    }

    private Collection<QName> getChildTypes(String typeId)
    {
        Collection<QName> subTypes = null;
        try
        {
            QName parentType = QName.createQName(typeId, this.namespaceService);
            subTypes = this.dictionaryService.getSubTypes(parentType, true);
        }
        catch (NamespaceException exception)
        {
            throw new InvalidArgumentException(exception.getMessage());
        }

        return subTypes;
    }
}