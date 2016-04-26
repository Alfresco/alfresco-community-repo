
package org.alfresco.repo.virtual.config;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * A repository context in which a {@link NodeRefPathExpression} should resolve
 * to a {@link NodeRef} using a relative name or qname path.
 */
public interface NodeRefContext
{
    /**
     * @param namePath
     * @param resolver
     * @return the {@link NodeRef} the given name path resolves to using the
     *         supplied resolver.
     */
    NodeRef resolveNamePath(String[] namePath, NodeRefResolver resolver);

    /**
     * @param qNamePath
     * @param resolver
     * @return the {@link NodeRef} the given {@link QName} prefixed string path
     *         resolves to using the supplied resolver.
     */
    NodeRef resolveQNamePath(String[] qNamePath, NodeRefResolver resolver);

    /**
     * @return the name of this context
     */
    String getContextName();

    NodeRef createNamePath(String[] namePath, NodeRefResolver resolver);

    NodeRef createQNamePath(String[] qNamePath, String[] names, NodeRefResolver resolver);

}
