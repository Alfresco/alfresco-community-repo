
package org.alfresco.repo.template;

import java.io.Serializable;

/**
 * @author Brian
 *
 */
public interface TemplateProcessorMethod extends Serializable
{
    Object exec(final Object[] arguments) throws Exception;
}
