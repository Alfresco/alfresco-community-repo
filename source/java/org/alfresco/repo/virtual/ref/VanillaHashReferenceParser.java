
package org.alfresco.repo.virtual.ref;

import java.util.Collections;
import java.util.List;

import org.alfresco.repo.virtual.ref.ReferenceParser.Cursor;
/**
 * Custom parser for hash encoded strings of {@link Reference}s having the
 * protocol set to {@link VanillaProtocol}.
 */
public class VanillaHashReferenceParser extends VirtualHashReferenceParser
{

    public VanillaHashReferenceParser(HashStore classpathHashStore)
    {
        super(classpathHashStore,
              VANILLA_PROTOCOL_CODE);
    }

    @Override
    protected Reference parseVirtualExtension(Cursor c, Resource templateResource, String templatePath,
                Resource actualNodeResource)
    {
        Resource vanillaTemplateResource = parseResource(c);
        // TODO :parse vanilla template
        List<Parameter> extraParameters = Collections.<Parameter> emptyList();
        return ((VanillaProtocol) Protocols.VANILLA.protocol).newReference(Encodings.HASH.encoding,
                                                                           templateResource,
                                                                           templatePath,
                                                                           actualNodeResource,
                                                                           vanillaTemplateResource,
                                                                           extraParameters);
    }
}
