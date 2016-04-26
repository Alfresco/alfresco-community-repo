
package org.alfresco.repo.virtual.template;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.namespace.QName;

/**
 * Encapsulated node creation parameters needed to produce {@link FilingData}
 * used for node creation in virtual contexts using {@link FilingRule}s.
 */
public class FilingParameters
{
    private Reference parentRef;

    private QName assocTypeQName;

    private QName assocQName;

    private QName nodeTypeQName;

    private Map<QName, Serializable> properties;

    public FilingParameters(Reference parentReference)
    {
        this(parentReference,
             null,
             null,
             null,
             null);
    }

    public FilingParameters(Reference parentReference, QName assocTypeQName, QName assocQName, QName nodeTypeQName,
                Map<QName, Serializable> properties)
    {
        super();
        this.parentRef = parentReference;
        this.assocTypeQName = assocTypeQName;
        this.assocQName = assocQName;
        this.nodeTypeQName = nodeTypeQName;
        this.properties = properties;
    }

    public Reference getParentRef()
    {
        return this.parentRef;
    }

    public QName getAssocTypeQName()
    {
        return this.assocTypeQName;
    }

    public QName getAssocQName()
    {
        return this.assocQName;
    }

    public QName getNodeTypeQName()
    {
        return this.nodeTypeQName;
    }

    public Map<QName, Serializable> getProperties()
    {
        return this.properties;
    }

}
