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
