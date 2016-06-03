
package org.alfresco.repo.virtual.store;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.model.SystemTemplateLocationsConstraint;
import org.alfresco.repo.virtual.ref.Encodings;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.VirtualProtocol;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * An {@link AspectVirtualizationMethod} that uses an aspect defined String
 * property that holds the system path of a template resource.<br>
 * System paths are custom string reference of a resource that can be located
 * either in the repository or in the java classpath - <i>system paths are
 * deprecated and they will be replaced by {@link Encodings#PLAIN} encoded
 * {@link Reference} strings</i>.<br>
 * 
 * @author Bogdan Horje
 */
public class SystemVirtualizationMethod extends AspectVirtualizationMethod
{
    /** {@link QName} of template system path holding property */
    private QName systemPathPropertyQName;

    /**
     * String name of template system path holding property. Will be converted
     * into a {@link QName} during {@link #init()}
     */
    private String systemPathPropertyName;

    public SystemVirtualizationMethod()
    {

    }

    /**
     * Bean initialization.
     */
    @Override
    public void init()
    {
        super.init();
        if (systemPathPropertyName != null)
        {
            systemPathPropertyQName = QName.createQName(systemPathPropertyName,
                                                        namespacePrefixResolver);
        }
    }

    public void setSystemPathPropertyName(String systemPathPropertyName)
    {
        this.systemPathPropertyName = systemPathPropertyName;
    }

    @Override
    public Reference virtualize(ActualEnvironment env, NodeRef nodeRef) throws VirtualizationException
    {
        String templateSystemPath = (String) env.getProperty(nodeRef,
                                                             systemPathPropertyQName);
        if (templateSystemPath != null)
        {
            return newVirtualReference(env,
                                       nodeRef,
                                       templateSystemPath);
        }
        else
        {
            // default branch - invalid virtual node
            throw new VirtualizationException("Invalid virtualization : missing template system-path.");
        }
    }

    @Override
    public boolean canVirtualize(ActualEnvironment env, NodeRef nodeRef) throws ActualEnvironmentException
    {
        boolean canVirtualize = super.canVirtualize(env,
                                                    nodeRef);
        if (canVirtualize)
        {
            // TODO: optimize - should not need another repository meta data access !!!
            // Optimization requires a default value specified.

            String templateSystemPath = (String) env.getProperty(nodeRef,
                                                                 systemPathPropertyQName);
            if(templateSystemPath==null){
                return false;
            }
            final char systemToken = templateSystemPath.charAt(0);
            if (systemToken == VirtualProtocol.NODE_TEMPLATE_PATH_TOKEN)
            {
                return env.exists(new NodeRef(templateSystemPath.substring(1)));
            }

            if (systemToken == VirtualProtocol.CLASS_TEMPLATE_PATH_TOKEN)
            {
                return env.exists(templateSystemPath.substring(1));
            }
            canVirtualize = !templateSystemPath.equals(SystemTemplateLocationsConstraint.NULL_SYSTEM_TEMPLATE);
        }

        return canVirtualize;
    }

}
