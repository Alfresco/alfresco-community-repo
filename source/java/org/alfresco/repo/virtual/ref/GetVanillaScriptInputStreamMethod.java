
package org.alfresco.repo.virtual.ref;

import java.io.InputStream;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;

/**
 * It returns an {@link InputStream} for the vanilla-virtual folder template resource
 * indicated by the given vanilla protocol reference.
 * 
 * @author Bogdan Horje
 */
public class GetVanillaScriptInputStreamMethod extends AbstractProtocolMethod<InputStream>
{
    private ActualEnvironment environment;

    public GetVanillaScriptInputStreamMethod(ActualEnvironment environment)
    {
        super();
        this.environment = environment;
    }

    @Override
    public InputStream execute(VanillaProtocol vanillaProtocol, Reference reference) throws ProtocolMethodException
    {
        Resource resource = vanillaProtocol.getVanillaTemplateResource(reference);
        try
        {
            return resource.asStream(environment);
        }
        catch (ActualEnvironmentException e)
        {
            throw new ProtocolMethodException(e);
        }
    }
}
