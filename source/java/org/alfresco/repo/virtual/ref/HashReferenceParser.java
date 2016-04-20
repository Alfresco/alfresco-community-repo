
package org.alfresco.repo.virtual.ref;

/**
 * Hash encoded reference string parser.<br>
 * Delegates to {@link VirtualHashReferenceParser},
 * {@link VanillaHashReferenceParser} or {@link NodeHashReferenceParser} for
 * custom protocol parsing.
 */
public class HashReferenceParser implements ReferenceParser, HashEncodingArtefact
{
    /**
     * 
     */
    private static final long serialVersionUID = -2569625423953183530L;

    private NodeHashReferenceParser nodeReferenceParser;

    private VirtualHashReferenceParser virtualReferenceParser;

    private VanillaHashReferenceParser vanillaReferenceParser;

    public HashReferenceParser()
    {
        HashStore cpStore = HashStoreConfiguration.getInstance().getClasspathHashStore();

        nodeReferenceParser = new NodeHashReferenceParser(cpStore,
                                                          this);

        virtualReferenceParser = new VirtualHashReferenceParser(cpStore);

        vanillaReferenceParser = new VanillaHashReferenceParser(cpStore);
    }

    @Override
    public Reference parse(String referenceString) throws ReferenceParseException
    {
        String[] tokens = referenceString.split("-");
        Cursor c = new Cursor(tokens,
                              0);

        return parse(c);
    }

    public Reference parse(Cursor c)
    {
        if (NODE_PROTOCOL_CODE.equals(c.currentToken()))
        {
            return nodeReferenceParser.parse(c);
        }
        else if (VANILLA_PROTOCOL_CODE.equals(c.currentToken()))
        {
            return vanillaReferenceParser.parse(c);
        }
        else if (VIRTUAL_PROTOCOL_CODE.equals(c.currentToken()))
        {
            return virtualReferenceParser.parse(c);
        }
        else
        {
            throw new ReferenceEncodingException("Unknown reference code " + c.currentToken());
        }

    }
}
