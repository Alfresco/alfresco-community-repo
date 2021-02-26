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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.rest.api.ClassDefinitionMapper;
import org.alfresco.rest.api.Types;
import org.alfresco.rest.api.model.Type;
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
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;

import java.util.Collection;
import java.util.List;
import java.util.Set;
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

    TypesImpl(DictionaryService dictionaryService, NamespacePrefixResolver namespaceService, ClassDefinitionMapper classDefinitionMapper)
    {
        super(dictionaryService, namespaceService, classDefinitionMapper);
    }

    @Override
    public CollectionWithPagingInfo<Type> listTypes(Parameters params)
    {
        Paging paging = params.getPaging();
        ModelApiFilter query = getQuery(params.getQuery());
        Stream<QName> typeStream = null;

        if (query != null && query.getModelIds() != null)
        {
            validateListParam(query.getModelIds(), PARAM_MODEL_IDS);
            Set<Pair<QName, Boolean>> modelsFilter = parseModelIds(query.getModelIds(), PARAM_INCLUDE_SUBTYPES);
            typeStream = modelsFilter.stream().map(this::getModelTypes).flatMap(Collection::stream);
        }
        else if (query != null && query.getParentIds() != null)
        {
            validateListParam(query.getParentIds(), PARAM_PARENT_IDS);
            typeStream = query.getParentIds().stream().map(this::getChildTypes).flatMap(Collection::stream);
        }
        else
        {
            typeStream = this.dictionaryService.getAllTypes().stream();
        }

        List<Type> allTypes = typeStream
                .filter((qName) -> filterByNamespace(query, qName))
                .filter(distinctByKey(QName::getPrefixString))
                .map((qName) -> this.convertToType(dictionaryService.getType(qName), params.getInclude()))
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

        return this.convertToType(typeDefinition, ALL_PROPERTIES);
    }

    public Type convertToType(TypeDefinition typeDefinition, List<String> includes)
    {
        try
        {
            Type type = new Type(typeDefinition, dictionaryService);
            constructFromFilters(type, typeDefinition, includes);
            return type;
        }
        catch (Exception ex)
        {
            throw new AlfrescoRuntimeException("Failed to parse Type: " + typeDefinition.getName() + " . " + ex.getMessage());
        }
    }

    private Collection<QName> getModelTypes(Pair<QName,Boolean> model)
    {
        ModelDefinition modelDefinition =  null;

        try
        {
            modelDefinition =  this.dictionaryService.getModel(model.getFirst());
        }
        catch (Exception exception)
        {
            throw new InvalidArgumentException(exception.getMessage());
        }

        if (modelDefinition == null)
            throw new EntityNotFoundException("model");

        Collection<QName> aspects = this.dictionaryService.getTypes(modelDefinition.getName());

        if (!model.getSecond()) //look for model types alone
            return aspects;

        Stream<QName> aspectStream = aspects.stream();
        Stream<QName> childrenStream = aspects.stream()
                .map(aspect -> this.dictionaryService.getSubTypes(aspect, false))
                .flatMap(Collection::stream);

        return Stream.concat(aspectStream, childrenStream).collect(Collectors.toList());
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