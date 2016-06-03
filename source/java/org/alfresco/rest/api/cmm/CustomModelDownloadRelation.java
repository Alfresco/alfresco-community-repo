
package org.alfresco.rest.api.cmm;

import java.util.Collections;
import java.util.List;

import org.alfresco.rest.api.CustomModels;
import org.alfresco.rest.api.model.CustomModelDownload;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Jamal Kaabi-Mofrad
 */
@RelationshipResource(name = "download", entityResource = CustomModelEntityResource.class, title = "Custom Model Download")
public class CustomModelDownloadRelation implements RelationshipResourceAction.Create<CustomModelDownload>, InitializingBean
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
    @WebApiDescription(title = "Creates download node containing the custom model file and if specified, its associated Share extension module file.")
    public List<CustomModelDownload> create(String modelName, List<CustomModelDownload> download, Parameters parameters)
    {
        CustomModelDownload result = customModels.createDownload(modelName, parameters);
        return Collections.singletonList(result);
    }
}
