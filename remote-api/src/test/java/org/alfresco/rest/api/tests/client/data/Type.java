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
package org.alfresco.rest.api.tests.client.data;

import org.alfresco.rest.api.model.Association;
import org.alfresco.rest.api.model.AssociationSource;
import org.alfresco.rest.api.model.Model;
import org.alfresco.rest.api.model.PropertyDefinition;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Type extends org.alfresco.rest.api.model.Type implements Serializable, ExpectedComparison
{

    @Override
    public void expected(Object model)
    {
        assertTrue("model is an instance of " + model.getClass(), model instanceof Type);

        Type other = (Type) model;

        AssertUtil.assertEquals("id", getId(), other.getId());
        AssertUtil.assertEquals("title", getTitle(), other.getTitle());
        AssertUtil.assertEquals("description", getDescription(), other.getDescription());
        AssertUtil.assertEquals("parenId", getParentId(), other.getParentId());
        AssertUtil.assertEquals("archive", getArchive(), other.getArchive());
        AssertUtil.assertEquals("container", getContainer(), other.getContainer());
        AssertUtil.assertEquals("includedInSupertypeQuery", getIncludedInSupertypeQuery(), other.getIncludedInSupertypeQuery());

        if (getModel() != null && other.getModel() != null)
        {
            AssertUtil.assertEquals("modelId", getModel().getId(), other.getModel().getId());
            AssertUtil.assertEquals("author", getModel().getAuthor(), other.getModel().getAuthor());
            AssertUtil.assertEquals("namespaceUri", getModel().getNamespaceUri(), other.getModel().getNamespaceUri());
            AssertUtil.assertEquals("namespacePrefix", getModel().getNamespacePrefix(), other.getModel().getNamespacePrefix());
        }
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSON()
    {
        JSONObject jsonObject = new JSONObject();
        if (getId() != null)
        {
            jsonObject.put("id", getId());
        }

        jsonObject.put("title", getTitle());

        if (getParentId() != null)
        {
            jsonObject.put("parentId", getParentId());
        }

        if (getDescription() != null)
        {
            jsonObject.put("description", getDescription());
        }

        if (getProperties() != null)
        {
            jsonObject.put("properties", getProperties());
        }

        if (getModel() != null)
        {
            jsonObject.put("model", getModel());
        }

        if (getMandatoryAspects() != null)
        {
            jsonObject.put("mandatoryAspects", getMandatoryAspects());
        }

        if (getContainer() != null)
        {
            jsonObject.put("container", getContainer());
        }

        if (getArchive() != null)
        {
            jsonObject.put("archive", getArchive());
        }

        if (getIncludedInSupertypeQuery() != null)
        {
            jsonObject.put("includedInSupertypeQuery", getIncludedInSupertypeQuery());
        }

        if (getAssociations() != null)
        {
            jsonObject.put("associations", getAssociations());
        }

        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static Type parseType(JSONObject jsonObject)
    {
        String id = (String) jsonObject.get("id");
        String title = (String) jsonObject.get("title");
        String description = (String) jsonObject.get("description");
        String parentId = (String) jsonObject.get("parentId");
        List<PropertyDefinition> properties = (List<PropertyDefinition>) jsonObject.get("properties");
        List<String> mandatoryAspects = jsonObject.get("mandatoryAspects") != null ? new ArrayList((List<String>)jsonObject.get("mandatoryAspects")) : null;
        Boolean container = (Boolean) jsonObject.get("container");
        Boolean archive = (Boolean) jsonObject.get("archive");
        Boolean includedInSupertypeQuery = (Boolean) jsonObject.get("includedInSupertypeQuery");

        List<org.alfresco.rest.api.model.Association> associations = null;

        if (jsonObject.get("associations") != null)
        {
            associations = new ArrayList<>();
            JSONArray jsonArray =  (JSONArray) jsonObject.get("associations");
            for(int i = 0; i < jsonArray.size(); i++)
            {
                org.alfresco.rest.api.model.Association association = new Association();
                JSONObject object = (JSONObject) jsonArray.get(i);
                association.setId((String) object.get("id"));
                association.setTitle((String) object.get("title"));
                association.setDescription((String) object.get("description"));
                association.setChild((Boolean) object.get("child"));
                association.setProtected((Boolean) object.get("isProtected"));

                JSONObject sourceModel = (JSONObject) object.get("source");
                if (sourceModel != null)
                {
                    AssociationSource source = new AssociationSource();
                    source.setCls((String) sourceModel.get("cls"));
                    source.setRole((String) sourceModel.get("role"));
                    source.setIsMandatory((Boolean) sourceModel.get("isMandatory"));
                    source.setIsMany((Boolean) sourceModel.get("isMany"));
                    source.setIsMandatoryEnforced((Boolean) sourceModel.get("isMandatoryEnforced"));
                    association.setSource(source);
                }

                JSONObject targetModel = (JSONObject) object.get("target");
                {
                    AssociationSource target = new AssociationSource();
                    target.setCls((String) targetModel.get("cls"));
                    target.setRole((String) targetModel.get("role"));
                    target.setIsMandatory((Boolean) targetModel.get("isMandatory"));
                    target.setIsMany((Boolean) targetModel.get("isMany"));
                    target.setIsMandatoryEnforced((Boolean) targetModel.get("isMandatoryEnforced"));
                    association.setTarget(target);
                }
                associations.add(association);
            }
        }

        JSONObject jsonModel = (JSONObject) jsonObject.get("model");
        Model model = new Model();
        model.setId((String) jsonModel.get("id"));
        model.setDescription((String) jsonModel.get("description"));
        model.setNamespacePrefix((String) jsonModel.get("namespacePrefix"));
        model.setNamespaceUri((String) jsonModel.get("namespaceUri"));
        model.setAuthor((String) jsonModel.get("author"));

        Type type = new Type();
        type.setId(id);
        type.setTitle(title);
        type.setDescription(description);
        type.setParentId(parentId);
        type.setProperties(properties);
        type.setMandatoryAspects(mandatoryAspects);
        type.setContainer(container);
        type.setArchive(archive);
        type.setIncludedInSupertypeQuery(includedInSupertypeQuery);
        type.setAssociations(associations);
        type.setModel(model);

        return type;
    }

    @SuppressWarnings("unchecked")
    public static PublicApiClient.ListResponse<Type> parseTypes(JSONObject jsonObject)
    {
        List<Type> types = new ArrayList<Type>();

        JSONObject jsonList = (JSONObject)jsonObject.get("list");
        assertNotNull(jsonList);

        JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
        assertNotNull(jsonEntries);

        for(int i = 0; i < jsonEntries.size(); i++)
        {
            JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
            JSONObject entry = (JSONObject)jsonEntry.get("entry");
            types.add(parseType(entry));
        }

        PublicApiClient.ExpectedPaging paging = PublicApiClient.ExpectedPaging.parsePagination(jsonList);
        return new PublicApiClient.ListResponse<Type>(paging, types);
    }

}
