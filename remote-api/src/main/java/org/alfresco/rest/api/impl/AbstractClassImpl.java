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

import com.google.common.collect.ImmutableList;
import org.alfresco.rest.api.ClassDefinitionMapper;
import org.alfresco.rest.api.model.*;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class AbstractClassImpl<T extends AbstractClass> {
    static String PARAM_MODEL_IDS = "modelId";
    static String PARAM_PARENT_IDS = "parentId";
    static String PARAM_NAMESPACE_URI = "namespaceUri";
    static String PARAM_INCLUDE_SUBASPECTS = "INCLUDESUBASPECTS";
    static String PARAM_INCLUDE_SUBTYPES = "INCLUDESUBTYPES";
    static String PARAM_INCLUDE_PROPERTIES = "properties";
    static String PARAM_INCLUDE_MANDATORY_ASPECTS = "mandatoryAspects";
    static String PARAM_INCLUDE_ASSOCIATIONS = "associations";
    static List<String> ALL_PROPERTIES = ImmutableList.of(PARAM_INCLUDE_PROPERTIES, PARAM_INCLUDE_MANDATORY_ASPECTS, PARAM_INCLUDE_ASSOCIATIONS);

    private DictionaryService dictionaryService;
    private NamespacePrefixResolver namespaceService;
    private ClassDefinitionMapper classDefinitionMapper;

    AbstractClassImpl(DictionaryService dictionaryService, NamespacePrefixResolver namespaceService, ClassDefinitionMapper classDefinitionMapper)
    {
        this.dictionaryService = dictionaryService;
        this.namespaceService = namespaceService;
        this.classDefinitionMapper = classDefinitionMapper;
    }

    public CollectionWithPagingInfo<T> createPagedResult(List<T> list, Paging paging)
    {
        int skipCount = paging.getSkipCount();
        int maxItems = paging.getMaxItems();
        int totalItems = list.size();

        Collections.sort(list);

        if (skipCount >= totalItems)
        {
            List<T> empty = Collections.emptyList();
            return CollectionWithPagingInfo.asPaged(paging, empty, false, totalItems);
        }
        else
        {
            int end = Math.min(skipCount + maxItems, totalItems);
            boolean hasMoreItems = totalItems > end;
            list = list.subList(skipCount, end);
            return CollectionWithPagingInfo.asPaged(paging, list, hasMoreItems, totalItems);
        }
    }

    public boolean filterByNamespace(ModelApiFilter query, QName qName)
    {
        //System aspect/type is not allowed
        if (qName.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI))
        {
            return false;
        }
        if (query != null && query.getMatchedPrefix() != null)
        {
            return Pattern.matches(query.getMatchedPrefix(), qName.getNamespaceURI());
        }
        if (query != null && query.getNotMatchedPrefix() != null)
        {
            return  !Pattern.matches(query.getNotMatchedPrefix(), qName.getNamespaceURI());
        }
        return  true;
    }

    public ModelApiFilter getQuery(Query queryParameters)
    {
        if (queryParameters != null)
        {
            ClassQueryWalker propertyWalker = new ClassQueryWalker();
            QueryHelper.walk(queryParameters, propertyWalker);

            return ModelApiFilter.builder()
                    .withModelId(propertyWalker.getModelIds())
                    .withParentIds(propertyWalker.getParentIds())
                    .withMatchPrefix(propertyWalker.getMatchedPrefix())
                    .withNotMatchPrefix(propertyWalker.getNotMatchedPrefix())
                    .build();
        }
        return null;
    }

    void validateListParam(Set<String> listParam, String paramName)
    {
        if (listParam.isEmpty())
        {
            throw new IllegalArgumentException(StringUtils.capitalize(paramName) + "s filter list cannot be empty.");
        }

        listParam.stream()
                .filter(StringUtils::isBlank)
                .findAny()
                .ifPresent(qName -> {
                    throw new IllegalArgumentException(StringUtils.capitalize(paramName) + " cannot be empty (i.e. '')");
                });
    }


    protected Set<Pair<QName,Boolean>> parseModelIds(Set<String> modelIds, String apiSuffix)
    {
        return modelIds.stream().map(modelId ->
        {
            QName qName = null;
            boolean filterIncludeSubClass = false;

            int idx = modelId.lastIndexOf(' ');
            if (idx > 0)
            {
                String suffix = modelId.substring(idx);
                if (suffix.equalsIgnoreCase(" " + apiSuffix))
                {
                    filterIncludeSubClass = true;
                    modelId = modelId.substring(0, idx);
                }
            }

            try
            {
                qName = QName.createQName(modelId, this.namespaceService);
            }
            catch (Exception ex)
            {
                throw new InvalidArgumentException(modelId + " isn't a valid QName. " + ex.getMessage());
            }

            if (qName == null)
                throw new InvalidArgumentException(modelId + " isn't a valid QName. ");

            return new Pair<>(qName, filterIncludeSubClass);
        }).collect(Collectors.toSet());
    }


    public T constructFromFilters(T abstractClass, org.alfresco.service.cmr.dictionary.ClassDefinition classDefinition, List<String> includes) {

        if (includes != null && includes.contains(PARAM_INCLUDE_PROPERTIES))
        {
            List<PropertyDefinition> properties = Collections.emptyList();
            ClassDefinition _classDefinition = this.classDefinitionMapper.fromDictionaryClassDefinition(classDefinition, dictionaryService);
            if (_classDefinition.getProperties() != null)
            {
                properties = _classDefinition.getProperties();
            }
            abstractClass.setProperties(properties);
        }

        if (includes != null && includes.contains(PARAM_INCLUDE_ASSOCIATIONS))
        {
            List<Association> associations = getAssociations(classDefinition.getAssociations());
            abstractClass.setAssociations(associations);
        }

        if (includes != null && includes.contains(PARAM_INCLUDE_MANDATORY_ASPECTS))
        {
            if (classDefinition.getDefaultAspectNames() != null)
            {
                List<String> aspects = classDefinition.getDefaultAspectNames().stream().map(QName::toPrefixString).collect(Collectors.toList());
                abstractClass.setMandatoryAspects(aspects);
            }
        }

        abstractClass.setContainer(classDefinition.isContainer());
        abstractClass.setArchive(classDefinition.getArchive());
        abstractClass.setIncludedInSupertypeQuery(classDefinition.getIncludedInSuperTypeQuery());
        return  abstractClass;
    }

    List<Association> getAssociations(Map<QName, AssociationDefinition> associationDefinitionMap)
    {
        Collection<AssociationDefinition> associationDefinitions = associationDefinitionMap.values();

        if (associationDefinitions.size() == 0)
            return Collections.emptyList();

        List<Association> associations = new ArrayList<Association>();

        for (AssociationDefinition definition : associationDefinitions)
        {
            Association association = new Association();

            association.setId(definition.getName().toPrefixString());
            association.setTitle(definition.getTitle());
            association.setDescription(definition.getDescription());
            association.setChild(definition.isChild());
            association.setProtected(definition.isProtected());

            AssociationSource source = new AssociationSource();

            String sourceRole = definition.getSourceRoleName() != null ? definition.getSourceRoleName().toPrefixString() : null;
            source.setRole(sourceRole);

            String sourceClass = definition.getSourceClass() != null ? definition.getSourceClass().getName().toPrefixString() : null;
            source.setCls(sourceClass);

            source.setIsMany(definition.isSourceMany());
            source.setIsMandatory(definition.isSourceMandatory());

            AssociationSource target = new AssociationSource();
            String targetRole = definition.getTargetRoleName() != null ? definition.getTargetRoleName().toPrefixString() : null;
            target.setRole(targetRole);

            String targetClass = definition.getTargetClass() != null ? definition.getTargetClass().getName().toPrefixString() : null;
            target.setCls(targetClass);

            target.setIsMany(definition.isTargetMany());
            target.setIsMandatory(definition.isTargetMandatory());
            target.setIsMandatoryEnforced(definition.isTargetMandatoryEnforced());

            association.setSource(source);
            association.setTarget(target);
            associations.add(association);
        }

        return associations;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static class ClassQueryWalker extends MapBasedQueryWalker
    {
        private Set<String> modelIds = null;
        private Set<String> parentIds = null;
        private String notMatchedPrefix = null;
        private String matchedPrefix = null;

        public ClassQueryWalker()
        {
            super(new HashSet<>(Arrays.asList(PARAM_MODEL_IDS, PARAM_PARENT_IDS)), new HashSet<>(Collections.singleton(PARAM_NAMESPACE_URI)));
        }

        @Override
        public void in(String propertyName, boolean negated, String... propertyValues)
        {
            if (negated)
            {
                throw new InvalidArgumentException("Cannot use NOT for " + propertyName);
            }

            if (propertyName.equalsIgnoreCase(PARAM_MODEL_IDS))
            {
                modelIds = new HashSet<>(Arrays.asList(propertyValues));
            }

            if (propertyName.equalsIgnoreCase(PARAM_PARENT_IDS))
            {
                parentIds = new HashSet<>(Arrays.asList(propertyValues));
            }
        }

        @Override
        public void matches(String property, String value, boolean negated)
        {
            if (negated && property.equals(PARAM_NAMESPACE_URI))
            {
                notMatchedPrefix = value;
            }
            else if (property.equals(PARAM_NAMESPACE_URI))
            {
                matchedPrefix = value;
            }
        }

        public Set<String> getModelIds()
        {
            return this.modelIds;
        }

        public Set<String> getParentIds()
        {
            return this.parentIds;
        }

        public String getNotMatchedPrefix()
        {
            return this.notMatchedPrefix;
        }

        public String getMatchedPrefix()
        {
            return this.matchedPrefix;
        }
    }

    public static class ModelApiFilter
    {
        private Set<String> modelIds;
        private Set<String> parentIds;
        private String matchedPrefix;
        private String notMatchedPrefix;

        public ModelApiFilter()
        {
        }

        public Set<String> getModelIds()
        {
            return modelIds;
        }

        public String getMatchedPrefix()
        {
            return matchedPrefix;
        }

        public String getNotMatchedPrefix()
        {
            return notMatchedPrefix;
        }

        public Set<String> getParentIds()
        {
            return parentIds;
        }

        public static ModelApiFilterBuilder builder()
        {
            return new ModelApiFilterBuilder();
        }

        public static class ModelApiFilterBuilder
        {
            private Set<String> modelIds;
            private Set<String> parentIds;
            private String matchedPrefix;
            private String notMatchedPrefix;

            public ModelApiFilterBuilder withModelId(Set<String> modelIds)
            {
                this.modelIds = modelIds;
                return this;
            }

            public ModelApiFilterBuilder withParentIds(Set<String> parentIds)
            {
                this.parentIds = parentIds;
                return this;
            }

            public ModelApiFilterBuilder withMatchPrefix(String matchedPrefix)
            {
                this.matchedPrefix = matchedPrefix;
                return this;
            }

            public ModelApiFilterBuilder withNotMatchPrefix(String notMatchedPrefix)
            {
                this.notMatchedPrefix = notMatchedPrefix;
                return this;
            }

            public ModelApiFilter build()
            {
                ModelApiFilter modelApiFilter = new ModelApiFilter();
                modelApiFilter.modelIds = modelIds;
                modelApiFilter.parentIds = parentIds;
                modelApiFilter.matchedPrefix = matchedPrefix;
                modelApiFilter.notMatchedPrefix = notMatchedPrefix;
                return modelApiFilter;
            }
        }
    }
}
