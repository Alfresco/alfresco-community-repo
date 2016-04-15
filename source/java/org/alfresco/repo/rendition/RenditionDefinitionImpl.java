
package org.alfresco.repo.rendition;

import java.io.Serializable;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.rendition.RenderCallback;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @author Neil McErlean
 * @since 3.3
 */
public class RenditionDefinitionImpl extends ActionImpl implements RenditionDefinition
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 4336392868488634875L;

    public static final String RENDITION_DEFINITION_NAME = "renderingActionName";

    public NodeRef renditionParent;
    public QName renditionAssociationType;
    private RenderCallback renderCallback;

    /**
     * @param id
     *            the action id
     * @param renditionName
     *            a unique name for the rendering action.
     * @param renderingEngineName
     *            the name of the rendering action definition
     */
    public RenditionDefinitionImpl(String id, QName renditionName, String renderingEngineName)
    {
        super(null, id, renderingEngineName);
        setParameterValue(RENDITION_DEFINITION_NAME, renditionName);
    }

    public RenditionDefinitionImpl(Action action)
    {
        super(action);
    }

    public RenditionDefinitionImpl(Action action, String renderingEngineName)
    {
        super(action, renderingEngineName);
    }

    /*
     * @see
     * org.alfresco.service.cmr.rendition.RenditionDefinition#getRenditionName()
     */
    public QName getRenditionName()
    {
        Serializable parameterValue = getParameterValue(RENDITION_DEFINITION_NAME);
		return (QName) parameterValue;
    }

    /*
     * @see
     * org.alfresco.service.cmr.rendition.RenditionDefinition#getRenditionParent
     * ()
     */
    public NodeRef getRenditionParent()
    {
        return this.renditionParent;
    }

    /*
     * @see
     * org.alfresco.service.cmr.rendition.RenditionDefinition#setRenditionParent
     * (org.alfresco.service.cmr.repository.NodeRef)
     */
    public void setRenditionParent(NodeRef renditionParent)
    {
        this.renditionParent = renditionParent;
    }

    /*
     * @seeorg.alfresco.service.cmr.rendition.RenditionDefinition#
     * getRenditionAssociationType()
     */
    public QName getRenditionAssociationType()
    {
        return this.renditionAssociationType;
    }

    /*
     * @seeorg.alfresco.service.cmr.rendition.RenditionDefinition#
     * setRenditionAssociationType(org.alfresco.service.namespace.QName)
     */
    public void setRenditionAssociationType(QName renditionAssociationType)
    {
        this.renditionAssociationType = renditionAssociationType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.service.cmr.rendition.RenditionDefinition#setCallback(org
     * .alfresco.service.cmr.rendition.RenderCallback)
     */
    public void setCallback(RenderCallback callback)
    {
        this.renderCallback = callback;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.service.cmr.rendition.RenditionDefinition#setCallback(org
     * .alfresco.service.cmr.rendition.RenderCallback)
     */
    public RenderCallback getCallback()
    {
        return this.renderCallback;
    }
}
