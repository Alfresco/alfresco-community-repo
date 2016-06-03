
package org.alfresco.repo.virtual.store;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * A template virtualization rule implementation that uses Alfresco-aspect
 * defined meta-data for locating templates.<br>
 * Stores the aspect {@link QName} to be used in resolving templates by querying
 * aspect defined meta data from the virtualized {@link NodeRef}.
 * 
 * @author Bogdan Horje
 */
public abstract class AspectVirtualizationMethod extends TemplateVirtualizationMethod
{
    private QName aspectQName;

    private String aspectName;

    protected NamespacePrefixResolver namespacePrefixResolver;

    public AspectVirtualizationMethod()
    {

    }

    public AspectVirtualizationMethod(QName aspectName)
    {
        super();
        this.aspectQName = aspectName;
    }

    public void init()
    {
        if (aspectName != null)
        {
            aspectQName = QName.createQName(aspectName,
                                            namespacePrefixResolver);
        }
    }

    /**
     * Determines if a given {@link NodeRef} can be virtualized by this
     * virtualization method by checking the presence of the of the configured
     * aspect (i.e. {@link #aspectQName}) on the given {@link NodeRef}.
     * 
     * @param env the environment in which the virtualization should take place
     * @param nodeRef the {@link NodeRef} that should be virtualized
     * @return <code>true</code> if the given {@link NodeRef} can be virtualized
     *         by this virtualization method (i.e. the configurend aspect is set
     *         on the given {@link NodeRef}) <code>false</code> otherwise
     * @throws VirtualizationException
     */
    @Override
    public boolean canVirtualize(ActualEnvironment env, NodeRef nodeRef) throws ActualEnvironmentException
    {
        return env.hasAspect(nodeRef,
                             getAspectQName());
    }

    protected QName getAspectQName()
    {
        return aspectQName;
    }

    public void setAspectName(String aspectName)
    {
        this.aspectName = aspectName;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }
}
