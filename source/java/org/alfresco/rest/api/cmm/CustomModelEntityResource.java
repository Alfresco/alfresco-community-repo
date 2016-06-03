
package org.alfresco.rest.api.cmm;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.CustomModels;
import org.alfresco.rest.api.model.CustomModel;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Jamal Kaabi-Mofrad
 */
@EntityResource(name = "cmm", title = "Custom Model Management")
public class CustomModelEntityResource implements EntityResourceAction.Read<CustomModel>,
            EntityResourceAction.ReadById<CustomModel>,
            EntityResourceAction.Create<CustomModel>,
            EntityResourceAction.Update<CustomModel>,
            EntityResourceAction.Delete,
            InitializingBean
{

    private CustomModels customModels;

    public void setCustomModels(CustomModels customModels)
    {
        this.customModels = customModels;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "customModels", customModels);
    }

    @Override
    @WebApiDescription(title="Returns custom model information for the given model name.")
    public CustomModel readById(String modelName, Parameters parameters) throws EntityNotFoundException
    {
        return customModels.getCustomModel(modelName, parameters);
    }

    @Override
    @WebApiDescription(title="Returns a paged list of all custom models.")
    public CollectionWithPagingInfo<CustomModel> readAll(Parameters parameters)
    {
        return customModels.getCustomModels(parameters);
    }

    @Override
    @WebApiDescription(title="Creates custom model(s).")
    public List<CustomModel> create(List<CustomModel> entity, Parameters parameters)
    {
        List<CustomModel> result = new ArrayList<>(entity.size());
        for (CustomModel cm : entity)
        {
            result.add(customModels.createCustomModel(cm));
        }
        return result;
    }

    @Override
    @WebApiDescription(title = "Updates or activates/deactivates the custom model.")
    public CustomModel update(String modelName, CustomModel entity, Parameters parameters)
    {
        return customModels.updateCustomModel(modelName, entity, parameters);
    }

    @Override
    @WebApiDescription(title = "Deletes the custom model.")
    public void delete(String modelName, Parameters parameters)
    {
        customModels.deleteCustomModel(modelName);
    }
}