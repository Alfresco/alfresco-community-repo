/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.domain.schema;

import java.util.Collections;
import java.util.List;

import org.alfresco.repo.admin.patch.impl.SchemaUpgradeScriptPatch;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.schemacomp.SchemaDifferenceHelper;

/**
 * Registers a list of create scripts.
 * 
 * @author Derek Hulley
 * @since 4.2
 */
public class SchemaBootstrapRegistration
{
    private SchemaBootstrap schemaBootstrap;
    private List<String> preCreateScriptUrls;
    private List<String> postCreateScriptUrls;
    private List<SchemaUpgradeScriptPatch> preUpdateScriptPatches;
    private List<SchemaUpgradeScriptPatch> postUpdateScriptPatches;
    private List<SchemaUpgradeScriptPatch> updateActivitiScriptPatches;
    private SchemaDifferenceHelper differenceHelper;
    
    public SchemaBootstrapRegistration()
    {
        this.preCreateScriptUrls = Collections.emptyList();
        this.postCreateScriptUrls = Collections.emptyList();
        this.preUpdateScriptPatches = Collections.emptyList();
        this.postUpdateScriptPatches = Collections.emptyList();
        this.updateActivitiScriptPatches = Collections.emptyList();
    }

    /**
     * @param schemaBootstrap           the component with which to register the URLs
     */
    public void setSchemaBootstrap(SchemaBootstrap schemaBootstrap)
    {
        this.schemaBootstrap = schemaBootstrap;
    }

    /**
     * @param differenceHelper           the component with which to register upgrade script pacthes
     */
    public void setDifferenceHelper(SchemaDifferenceHelper differenceHelper)
    {
        this.differenceHelper = differenceHelper;
    }

    /**
     * @param preCreateScriptUrls       a list of schema create URLs that will be registered in order.
     * 
     * @see SchemaBootstrap#addPreCreateScriptUrl(String)
     */
    public void setPreCreateScriptUrls(List<String> preCreateScriptUrls)
    {
        this.preCreateScriptUrls = preCreateScriptUrls;
    }

    /**
     * @param preCreateScriptUrls      a list of schema create URLs that will be registered in order.
     * 
     * @see SchemaBootstrap#addPostCreateScriptUrl(String)
     */
    public void setPostCreateScriptUrls(List<String> preCreateScriptUrls)
    {
        this.postCreateScriptUrls = preCreateScriptUrls;
    }

    /**
     * @param updateActivitiScriptPatches    a list of schema upgade script patches for Activiti tables to execute
     * 
     * @see SchemaBootstrap#addUpdateActivitiScriptPatch(org.alfresco.repo.admin.patch.impl.SchemaUpgradeScriptPatch)
     */
    public void setUpdateActivitiScriptPatches(List<SchemaUpgradeScriptPatch> updateActivitiScriptPatches)
    {
        this.updateActivitiScriptPatches = updateActivitiScriptPatches;
    }

    /**
     * @param preUpdateScriptPatches    a list of schema upgade script patches to execute before Hibernate patching
     * 
     * @see SchemaBootstrap#addPreUpdateScriptPatch(org.alfresco.repo.admin.patch.impl.SchemaUpgradeScriptPatch)
     */
    public void setPreUpdateScriptPatches(List<SchemaUpgradeScriptPatch> preUpdateScriptPatches)
    {
        this.preUpdateScriptPatches = preUpdateScriptPatches;
    }

    /**
     * @param postUpdateScriptPatches   a list of schema upgade script patches to execute after Hibernate patching
     * 
     * @see SchemaBootstrap#addPostUpdateScriptPatch(org.alfresco.repo.admin.patch.impl.SchemaUpgradeScriptPatch)
     */
    public void setPostUpdateScriptPatches(List<SchemaUpgradeScriptPatch> postUpdateScriptPatches)
    {
        this.postUpdateScriptPatches = postUpdateScriptPatches;
    }

    /**
     * Registers all the necessary scripts and patches with the {@link SchemaBootstrap}.
     */
    public void register()
    {
        PropertyCheck.mandatory(this, "schemaBootstrap", schemaBootstrap);
        PropertyCheck.mandatory(this, "preCreateScriptUrls", preCreateScriptUrls);
        PropertyCheck.mandatory(this, "postCreateScriptUrls", postCreateScriptUrls);
        PropertyCheck.mandatory(this, "preUpdateScriptPatches", preUpdateScriptPatches);
        PropertyCheck.mandatory(this, "postUpdateScriptPatches", postUpdateScriptPatches);
        PropertyCheck.mandatory(this, "updateActivitiScriptPatches", updateActivitiScriptPatches);
        
        for (String preCreateScriptUrl : preCreateScriptUrls)
        {
            schemaBootstrap.addPreCreateScriptUrl(preCreateScriptUrl);
        }
        for (String postCreateScriptUrl : postCreateScriptUrls)
        {
            schemaBootstrap.addPostCreateScriptUrl(postCreateScriptUrl);
        }
        for (SchemaUpgradeScriptPatch preUpdateScriptPatch : preUpdateScriptPatches)
        {
            schemaBootstrap.addPreUpdateScriptPatch(preUpdateScriptPatch);
        }
        for (SchemaUpgradeScriptPatch postUpdateScriptPatch : postUpdateScriptPatches)
        {
            schemaBootstrap.addPostUpdateScriptPatch(postUpdateScriptPatch);
            differenceHelper.addUpgradeScriptPatch(postUpdateScriptPatch);
        }
        for (SchemaUpgradeScriptPatch updateActivitiScriptPatch : updateActivitiScriptPatches)
        {
            schemaBootstrap.addUpdateActivitiScriptPatch(updateActivitiScriptPatch);
        }
    }
}