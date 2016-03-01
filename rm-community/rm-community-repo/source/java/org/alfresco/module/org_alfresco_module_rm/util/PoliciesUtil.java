 
package org.alfresco.module.org_alfresco_module_rm.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Utility class with policy helper methods.
 *
 * @author Roy Wetherall
 */
public final class PoliciesUtil
{
    private PoliciesUtil()
    {
        // Will not be called
    }

    /**
     * Get all aspect and node type qualified names
     *
     * @param nodeRef
     *            the node we are interested in
     * @return Returns a set of qualified names containing the node type and all
     *         the node aspects, or null if the node no longer exists
     */
    public static Set<QName> getTypeAndAspectQNames(final NodeService nodeService, final NodeRef nodeRef)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Set<QName>>()
        {
            public Set<QName> doWork()
            {
                Set<QName> qnames = null;
                try
                {
                    Set<QName> aspectQNames = nodeService.getAspects(nodeRef);

                    QName typeQName = nodeService.getType(nodeRef);

                    qnames = new HashSet<QName>(aspectQNames.size() + 1);
                    qnames.addAll(aspectQNames);
                    qnames.add(typeQName);
                }
                catch (InvalidNodeRefException e)
                {
                    qnames = Collections.emptySet();
                }
                // done
                return qnames;
            }
        }, AuthenticationUtil.getAdminUserName());
    }
}
