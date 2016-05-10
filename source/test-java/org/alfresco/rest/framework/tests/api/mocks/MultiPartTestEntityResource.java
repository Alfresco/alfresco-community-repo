
package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * @author Jamal Kaabi-Mofrad
 */
@EntityResource(name = "multiparttest", title = "multi-part upload test")
public class MultiPartTestEntityResource
            implements MultiPartResourceAction.Create<MultiPartTestResponse>
{

    @Override
    @WebApiDescription(title = "Creates a multipart", successStatus = Status.STATUS_ACCEPTED)
    public MultiPartTestResponse create(FormData formData, Parameters parameters, WithResponse withResponse)
    {
        return new MultiPartTestResponse(formData.getParameters().get("name")[0]);
    }
}
