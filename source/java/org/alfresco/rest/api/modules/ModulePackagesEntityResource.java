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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.rest.api.modules;

import org.alfresco.rest.api.model.CustomModel;
import org.alfresco.rest.api.model.ModulePackage;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Returns Alfresco Module Information.
 */
@EntityResource(name="modulepackages", title = "Installed Modules Packages")
public class ModulePackagesEntityResource implements   EntityResourceAction.Read<ModulePackage>,
                                                EntityResourceAction.ReadById<ModulePackage>
{
    @Autowired
    ModuleService moduleService;

    @Override
    @WebApiDescription(title="Returns ModulePackage information for the given module.")
    public ModulePackage readById(String modelName, Parameters parameters) throws EntityNotFoundException
    {
        ModuleDetails moduleDetails = moduleService.getModule(modelName);
        if(moduleDetails == null)
        {
            // module does not exist
            throw new EntityNotFoundException(modelName);
        }
        return ModulePackage.fromModuleDetails(moduleDetails);
    }

    @Override
    @WebApiDescription(title="Returns a paged list of all Modules.")
    public CollectionWithPagingInfo<ModulePackage> readAll(Parameters parameters)
    {
        List<ModuleDetails> details = moduleService.getAllModules();
        if (details!= null && details.size()>0)
        {
            List<ModulePackage> packages = new ArrayList<>(details.size());
            for (ModuleDetails detail : details)
            {
                packages.add(ModulePackage.fromModuleDetails(detail));
            }
            return CollectionWithPagingInfo.asPaged(parameters.getPaging(), packages);
        }

        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), Collections.EMPTY_LIST);
    }
}
