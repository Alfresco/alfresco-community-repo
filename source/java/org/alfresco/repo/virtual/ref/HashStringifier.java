
package org.alfresco.repo.virtual.ref;

import java.util.List;

/**
 * Hash encoded reference stringifier.<br>
 * Delegates to {@link VanillaHashStringifier},
 * {@link VirtualHashStringifier} or {@link NodeHashStringifier} for
 * custom protocol parsing.
 */
public class HashStringifier implements Stringifier
{

    /**
     * 
     */
    private static final long serialVersionUID = 2213824445193662644L;

    private NodeHashStringifier nodeStringifier;

    private VirtualHashStringifier virtualStringifier;

    private VanillaHashStringifier vanillaStringifier;

    public HashStringifier()
    {
        HashStore cpStore = HashStoreConfiguration.getInstance().getClasspathHashStore();
        nodeStringifier = new NodeHashStringifier(cpStore,
                                                  this);
        virtualStringifier = new VirtualHashStringifier(cpStore,
                                                        this);

        vanillaStringifier = new VanillaHashStringifier(cpStore,
                                                        this);
    }

    @Override
    public String stringify(Reference reference) throws ReferenceEncodingException
    {
        final Protocol protocol = reference.getProtocol();

        if (Protocols.NODE.protocol.equals(protocol))
        {
            return nodeStringifier.stringify(reference);
        }
        else if (Protocols.VIRTUAL.protocol.equals(protocol))
        {
            return virtualStringifier.stringify(reference);
        }
        else if (Protocols.VANILLA.protocol.equals(protocol))
        {
            return vanillaStringifier.stringify(reference);
        }
        else
        {
            return stringifyUnknown(reference);
        }
    }

    private String stringifyUnknown(Reference reference) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Could not stringify unknown protocol reference s"
                    + reference.encode(Encodings.PLAIN.encoding));
    }

    @Override
    public String stringify(Resource resource) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyResource(Resource resource) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyResource(RepositoryResource resource) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyResource(ClasspathResource resource) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringify(RepositoryLocation repositoryLocation) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryLocation repositoryLocation) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryNodeRef repositoryNodeRef) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyRepositoryLocation(RepositoryPath repositoryPath) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringify(List<Parameter> parameters) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringify(Parameter parameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyParameter(Parameter parameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyParameter(ResourceParameter resourceParameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyParameter(StringParameter stringParameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

    @Override
    public String stringifyParameter(ReferenceParameter referenceParameter) throws ReferenceEncodingException
    {
        throw new ReferenceEncodingException("Unknown hash protocol.");
    }

}
