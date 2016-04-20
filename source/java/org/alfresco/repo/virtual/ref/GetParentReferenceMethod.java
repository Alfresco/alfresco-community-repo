
package org.alfresco.repo.virtual.ref;

/**
 * Returns a virtual parent reference upon execution by subtracting the last
 * template path element from of the given reference.<br>
 * For root template path references a <code>null</code> will be returned upon
 * execution.
 * 
 * @see VirtualProtocol#replaceTemplatePath(Reference, String)
 * @author Bogdan Horje
 */
public class GetParentReferenceMethod extends AbstractProtocolMethod<Reference>
{
    @Override
    public Reference execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        String path = virtualProtocol.getTemplatePath(reference);
        if (path.trim().endsWith(PATH_SEPARATOR))
        {

            int trailingPathIndex = path.lastIndexOf(PATH_SEPARATOR);
            if (trailingPathIndex == 0)
            {
                return null;
            }
            else
            {
                path = path.substring(0,
                                      trailingPathIndex);
            }
        }

        int index = path.lastIndexOf(PATH_SEPARATOR);
        if (index < 0)
        {
            return null;
        }
        else
        {
            String parentPath = path.substring(0,
                                               index);

            if (parentPath.isEmpty())
            {
                if (path.length() > 1)
                {
                    parentPath = PATH_SEPARATOR;
                }
                else
                {
                    return null;
                }
            }

            return virtualProtocol.replaceTemplatePath(reference,
                                                       parentPath);
        }
    }

    @Override
    public Reference execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        return ((ReferenceParameter) reference.getParameters().get(0)).getValue();
    }
}
