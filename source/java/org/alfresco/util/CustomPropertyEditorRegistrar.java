package org.alfresco.util;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

/**
 * Custom property editor registrar.
 *
 * @author Roy Wetherall
 * @since 5.0
 */
public class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar
{
    /** namespace service */
    private NamespaceService namespaceService;

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @see org.springframework.beans.PropertyEditorRegistrar#registerCustomEditors(org.springframework.beans.PropertyEditorRegistry)
     */
    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry)
    {
        // add custom QName editor
        registry.registerCustomEditor(QName.class, new QNameTypeEditor(namespaceService));
    }
}
