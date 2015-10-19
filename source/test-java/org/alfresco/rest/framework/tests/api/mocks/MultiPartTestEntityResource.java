
package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * @author Jamal Kaabi-Mofrad
 */
@EntityResource(name = "multiparttest", title = "multi-part upload test")
public class MultiPartTestEntityResource
            implements MultiPartResourceAction.Create<MultiPartTestResponse>
{

    @Override
    public MultiPartTestResponse create(FormData formData, Parameters parameters)
    {
        return new MultiPartTestResponse(formData.getParameters().get("filename")[0]);
    }
}
