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

package org.alfresco.module.org_alfresco_module_rm.patch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Module patch executer base implementation
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class ModulePatchExecuterImpl extends   AbstractModuleComponent
                                    implements ModulePatchExecuter
{
    /** logger */
    protected static final Log LOGGER = LogFactory.getLog(ModulePatchExecuterImpl.class);

    /** default start schema */
    private static final int START_SCHEMA = 0;

    /** attribute key */
    private static final String KEY_MODULE_SCHEMA = "module-schema";

    /** configured module schema version */
    protected int moduleSchema = START_SCHEMA;

    /** attribute service */
    protected AttributeService attributeService;

    /** module patches */
    protected Map<String, ModulePatch> modulePatches = new HashMap<>(21);

    /**
     * @param attributeService  attribute service
     */
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    /**
     * @param moduleSchema  configured module schema version
     */
    public void setModuleSchema(int moduleSchema)
    {
        this.moduleSchema = moduleSchema;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.ModulePatchExecuter#register(org.alfresco.module.org_alfresco_module_rm.patch.ModulePatch)
     */
    @Override
    public void register(ModulePatch modulePatch)
    {
        // ensure that the module patch being registered relates to the module id
        if (!getModuleId().equals(modulePatch.getModuleId()))
        {
            throw new AlfrescoRuntimeException("Unable to register module patch, becuase module id is invalid.");
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Registering module patch " + modulePatch.getId() + " for module " + getModuleId());
        }

        modulePatches.put(modulePatch.getId(), modulePatch);
    }

    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executeInternal()
    {
        // get current schema version
        int currentSchema = getCurrentSchema();

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Running module patch executer (currentSchema=" + currentSchema + ", configuredSchema=" + moduleSchema + ")");
        }

        if (moduleSchema > currentSchema)
        {
            // determine what patches should be applied
            List<ModulePatch> patchesToApply = new ArrayList<>(13);
            for (ModulePatch modulePatch : modulePatches.values())
            {
                if (modulePatch.getFixesFromSchema() <= currentSchema &&
                    modulePatch.getFixesToSchema() >= currentSchema)
                {
                    patchesToApply.add(modulePatch);
                }
            }

            // apply the patches in the correct order
            Collections.sort(patchesToApply);
            for (ModulePatch patchToApply : patchesToApply)
            {
                patchToApply.apply();
            }

            // update the schema
            updateSchema(moduleSchema);
        }
    }

    /**
     * Get the currently recorded schema version for the module
     *
     * @return  int currently recorded schema version
     */
    protected int getCurrentSchema()
    {
        Integer result = START_SCHEMA;
        if (attributeService.exists(KEY_MODULE_SCHEMA, getModuleId()))
        {
            result = (Integer)attributeService.getAttribute(KEY_MODULE_SCHEMA, getModuleId());
        }
        return result;
    }

    /**
     * Update the recorded schema version for the module.
     *
     * @param newSchema new schema version
     */
    protected void updateSchema(int newSchema)
    {
        attributeService.setAttribute(Integer.valueOf(newSchema), KEY_MODULE_SCHEMA,  getModuleId());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.ModulePatchExecuter#initSchemaVersion()
     */
    @Override
    public void initSchemaVersion()
    {
        updateSchema(moduleSchema);
    }
}
