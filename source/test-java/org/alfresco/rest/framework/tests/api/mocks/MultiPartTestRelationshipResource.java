
package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartRelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * @author Jamal Kaabi-Mofrad
 */
@RelationshipResource(name = "sheepUpload", entityResource = SheepEntityResource.class, title = "Sheep mulitpart upload")
public class MultiPartTestRelationshipResource
            implements MultiPartRelationshipResourceAction.Create<MultiPartTestResponse>
{

    @Override
    public MultiPartTestResponse create(String entityResourceId, FormData formData,
                Parameters parameters)
    {
        return new MultiPartTestResponse(formData.getParameters().get("filename")[0]);
    }

}
