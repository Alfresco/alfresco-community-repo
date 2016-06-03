
package org.alfresco.util;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.Path;

/**
 * Alfresco path-related utility functions.
 * 
 * @since 3.5
 */
public class PathUtil
{
    /**
     * Return the human readable form of the specified node Path. Fast version
     * of the method that simply converts QName localname components to Strings.
     * 
     * @param path Path to extract readable form from
     * @param showLeaf Whether to process the final leaf element of the path
     * 
     * @return human readable form of the Path
     */
    public static String getDisplayPath(Path path, boolean showLeaf)
    {
        // This method was moved here from org.alfresco.web.bean.repository.Repository
        StringBuilder buf = new StringBuilder(64);

        int count = path.size() - (showLeaf ? 0 : 1);
        for (int i = 0; i < count; i++)
        {
            String elementString = null;
            Path.Element element = path.get(i);
            if (element instanceof Path.ChildAssocElement)
            {
                ChildAssociationRef elementRef = ((Path.ChildAssocElement) element).getRef();
                if (elementRef.getParentRef() != null)
                {
                    elementString = elementRef.getQName().getLocalName();
                }
            } else
            {
                elementString = element.getElementString();
            }

            if (elementString != null)
            {
                buf.append("/");
                buf.append(elementString);
            }
        }

        return buf.toString();
    }
}
