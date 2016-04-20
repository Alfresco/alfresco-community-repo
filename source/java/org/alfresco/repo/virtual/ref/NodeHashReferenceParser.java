
package org.alfresco.repo.virtual.ref;

import org.alfresco.repo.virtual.ref.ReferenceParser.Cursor;

/**
 * Custom parser for hash encoded strings of {@link Reference}s having the
 * protocol set to {@link NodeProtocol}.
 */
public class NodeHashReferenceParser extends ProtocolHashParser
{
    private HashReferenceParser referenceParser;

    public NodeHashReferenceParser(HashStore classpathHashStore, HashReferenceParser referenceParser)
    {
        super(classpathHashStore);
        this.referenceParser = referenceParser;
    }

    @Override
    public Reference parse(Cursor cursor) throws ReferenceParseException
    {
        if (!NODE_PROTOCOL_CODE.equals(cursor.currentToken()))
        {
            throw new ReferenceParseException("Node token \"" + NODE_PROTOCOL_CODE + "\" expected instead of \""
                        + cursor.currentToken() + "\"");
        }
        cursor.i++;

        Resource resource = parseResource(cursor);

        Reference parentReference = referenceParser.parse(cursor);

        return NodeProtocol.newReference(Encodings.HASH.encoding,
                                         resource,
                                         parentReference);

    }

}
