
package org.alfresco.rest.framework.resource.actions.interfaces;

import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * @author Jamal Kaabi-Mofrad
 */
public interface MultiPartResourceAction
{

    /**
     * HTTP POST - Upload file content and meta-data into repository
     */
    public static interface Create<E> extends ResourceAction
    {
        public E create(FormData formData, Parameters parameters, WithResponse withResponse);
    }
}
