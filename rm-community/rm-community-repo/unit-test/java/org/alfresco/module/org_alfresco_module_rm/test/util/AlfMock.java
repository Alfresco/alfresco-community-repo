 
package org.alfresco.module.org_alfresco_module_rm.test.util;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

/**
 * Utilities helpful when mocking Alfresco constructs.
 *
 * @author Roy Wetherall
 * @since 2.4.a
 */
public class AlfMock
{
	/**
     * Helper to generate random text value suitable for a property
     * value or node name
     */
    public static String generateText()
    {
        return UUID.randomUUID().toString();
    }

    /**
     * Helper method to generate a qname.
     */
    public static QName generateQName()
    {
        return generateQName(GUID.generate());
    }

    /**
     * Helper method to generate a qname.
     */
    public static QName generateQName(String uri)
    {
        return QName.createQName(uri, GUID.generate());
    }

	/**
     * Helper method to generate a node reference.
     *
     * @return  {@link NodeRef} node reference that behaves like a node that exists in the spaces store
     */
    public static NodeRef generateNodeRef(NodeService mockedNodeService)
    {
        return generateNodeRef(mockedNodeService, null);
    }

    /**
     * Helper method to generate a node reference of a particular type.
     *
     * @param type  content type qualified name
     * @return {@link NodeRef}  node reference that behaves like a node that exists in the spaces store with
     *                          the content type provided
     */
    public static NodeRef generateNodeRef(NodeService mockedNodeService, QName type)
    {
        return generateNodeRef(mockedNodeService, type, true);
    }

    /**
     * Helper method to generate a cm:content node reference with a given name.
     *
     * @param name      content name
     * @return NodeRef  node reference
     */
    public static NodeRef generateCmContent(NodeService mockedNodeService, String name)
    {
        NodeRef nodeRef = generateNodeRef(mockedNodeService, ContentModel.TYPE_CONTENT, true);
        doReturn(name).when(mockedNodeService).getProperty(nodeRef, ContentModel.PROP_NAME);
        return nodeRef;
    }

    /**
     * Helper method to generate a node reference of a particular type with a given existence characteristic.
     *
     * @param type  content type qualified name
     * @param exists indicates whether this node should behave like a node that exists or not
     * @return {@link NodeRef}  node reference that behaves like a node that exists (or not) in the spaces store with
     *                          the content type provided
     */
    public static NodeRef generateNodeRef(NodeService mockedNodeService, QName type, boolean exists)
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        when(mockedNodeService.exists(eq(nodeRef))).thenReturn(exists);
        if (type != null)
        {
            when(mockedNodeService.getType(eq(nodeRef))).thenReturn(type);
        }
        return nodeRef;
    }

}
