package org.alfresco.util;

import java.beans.PropertyEditorSupport;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * QName type editor.
 *
 * @author Roy Wetherall
 * @since 5.0
 */
public class QNameTypeEditor extends PropertyEditorSupport
{
    /** namespace service */
    private NamespaceService namespaceService;

    /**
     * @param namespaceService  namespace service
     */
    public QNameTypeEditor(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
     */
    @Override
    public void setAsText(String text)
    {
        // convert prefix string to QName
        setValue(QName.createQName(text, namespaceService));
    }
}
