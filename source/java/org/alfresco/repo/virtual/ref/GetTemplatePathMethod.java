
package org.alfresco.repo.virtual.ref;

/**
 * Returns the virtual folder template inner path for a virtualized entity
 * reference.
 * 
 * @author Bogdan Horje
 */
public class GetTemplatePathMethod extends AbstractProtocolMethod<String>
{
    @Override
    public String execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        String path = virtualProtocol.getTemplatePath(reference);
        return path;
    }
}
