/*
 * #%L
 * Alfresco Data model classes
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

package org.alfresco.repo.dictionary;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;

/**
 * Read-only definition of a Custom Model
 *
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelDefinitionImpl implements CustomModelDefinition
{
    private final ModelDefinition m2ModelDefinition;
    private final boolean active;
    private final MessageLookup messageLookup;
    private final Collection<TypeDefinition> typeDefinitions;
    private final Collection<AspectDefinition> aspectDefinitions;
    private final Collection<ConstraintDefinition> modelDefinedConstraints;

    /* package */CustomModelDefinitionImpl(CompiledModel compiledModel, boolean active, MessageLookup messageLookup)
    {
        this.m2ModelDefinition = compiledModel.getModelDefinition();
        this.active = active;
        this.messageLookup = messageLookup;
        // compiledModel.getTypes(), getAspects and getConstraints are never null
        this.typeDefinitions = new ArrayList<>(compiledModel.getTypes());
        this.aspectDefinitions = new ArrayList<>(compiledModel.getAspects());
        this.modelDefinedConstraints = removeInlineConstraints(compiledModel);
    }

    /**
     * Removes the inline constraints (i.e. defined within the property) from
     * all constraints. The result will be constraints that have been defined
     * within the model (Top level) itself.
     *
     * @param compiledModel the compiled model
     * @return list of model defined constraints
     */
    public static List<ConstraintDefinition> removeInlineConstraints(CompiledModel compiledModel)
    {
        List<ConstraintDefinition> modelConstraints = new ArrayList<>();

        Set<QName> propertyConstraints = new HashSet<>();
        for(PropertyDefinition propDef : compiledModel.getProperties())
        {
            if (propDef.getConstraints().size() > 0)
            {
                for (ConstraintDefinition propConst : propDef.getConstraints())
                {
                    propertyConstraints.add(propConst.getName());
                }
            }
        }

        for (ConstraintDefinition constraint : compiledModel.getConstraints())
        {
            if (!propertyConstraints.contains(constraint.getName()))
            {
                modelConstraints.add(constraint);
            }
        }

        return modelConstraints;
    }

    @Override
    public String getDescription()
    {
        return getDescription(messageLookup);
    }

    @Override
    public boolean isActive()
    {
        return this.active;
    }

    @Override
    public String getAnalyserResourceBundleName()
    {
        return m2ModelDefinition.getAnalyserResourceBundleName();
    }

    @Override
    public String getAuthor()
    {
        return m2ModelDefinition.getAuthor();
    }

    @Override
    public long getChecksum(XMLBindingType xmlbindingtype)
    {
        return m2ModelDefinition.getChecksum(xmlbindingtype);
    }

    @Override
    public String getDescription(MessageLookup messagelookup)
    {
        return m2ModelDefinition.getDescription(messagelookup);
    }

    @Override
    public DictionaryDAO getDictionaryDAO()
    {
        return m2ModelDefinition.getDictionaryDAO();
    }

    @Override
    public Collection<NamespaceDefinition> getImportedNamespaces()
    {
        return m2ModelDefinition.getImportedNamespaces();
    }

    @Override
    public QName getName()
    {
        return m2ModelDefinition.getName();
    }

    @Override
    public Collection<NamespaceDefinition> getNamespaces()
    {
        return m2ModelDefinition.getNamespaces();
    }

    @Override
    public Date getPublishedDate()
    {
        return m2ModelDefinition.getPublishedDate();
    }

    @Override
    public String getVersion()
    {
        return m2ModelDefinition.getVersion();
    }

    @Override
    public boolean isNamespaceDefined(String uri)
    {
        return m2ModelDefinition.isNamespaceDefined(uri);
    }

    @Override
    public boolean isNamespaceImported(String uri)
    {
        return m2ModelDefinition.isNamespaceImported(uri);
    }

    @Override
    public void toXML(XMLBindingType xmlbindingtype, OutputStream xml)
    {
        m2ModelDefinition.toXML(xmlbindingtype, xml);
    }

    @Override
    public Collection<TypeDefinition> getTypeDefinitions()
    {
        return Collections.unmodifiableCollection(typeDefinitions);
    }

    @Override
    public Collection<AspectDefinition> getAspectDefinitions()
    {
        return Collections.unmodifiableCollection(aspectDefinitions);
    }

    @Override
    public Collection<ConstraintDefinition> getModelDefinedConstraints()
    {
        return Collections.unmodifiableCollection(modelDefinedConstraints);
    }
}
