
package org.alfresco.repo.virtual.ref;

import java.util.HashMap;
import java.util.Map;

/**
 * Common {@link Reference} encodings.
 * 
 * @author Bogdan Horje
 */
public enum Encodings
{
    /**
     * {@link Reference} string encoding that parses and produces reference
     * string representations that conform to the following grammar :
     * 
     * <pre>
     *  reference            := protocol':'resource[':'parameters]
     *  protocol             := 'virtual'|'node'|'template'
     *  resource             := repositoryResource | classpathResource
     *  repositoryResource   := 'repository'':'repositoryLocation
     *  repositoryLocation   := ('path'':'path)|('node'':'nodeRef)
     *  classpathResource    := 'classpath'':'path
     *  path                 := string
     *  parameters           := parameter[':'parameters]
     *  parameter            := referenceParameter | integerParameter | stringParameter
     *  referenceParameter   := 'ref'':'reference 
     *  resourceParameter    := 'r'':'resource
     *  integerParameter     := 'i'':'integer
     *  stringParameter      := 's'':'string
     * 
     * Examples (syntax is correct, but not the semantic):  
     *  
     * virtual:classpath:/org/alfresco/
     * virtual:classpath:/org/alfresco/:r:repository:node:workspace:SpacesStore:0029-222-333-444
     * virtual:classpath:/org/alfresco/:r:repository:node:workspace:SpacesStore:0029-222-333-444:r:repository:path:/Data Dictionary/Virtual Folders/claim.json
     * node:repository:node:workspace:SpacesStore:0029-222-333-444:r:virtual:repository:node:workspace:SpacesStore:0029-122-333-0023
     * </pre>
     */
    PLAIN(new Encoding('p',
                       new PlainReferenceParser(),
                       new PlainStringifier(),
                       false)),

    /**
     * A condensed {@link Reference} string representation.
     */
    ZERO(new Encoding('0',
                      new ZeroReferenceParser(),
                      new ZeroStringifier(),
                      false)),

    /**
     * A hash based condensed {@link Reference} string representation.
     */
    HASH(new Encoding('H',
                      new HashReferenceParser(),
                      new HashStringifier(),
                      true));

    private static volatile Map<Character, Encoding> tokenMap;

    public static synchronized Encoding fromToken(Character token)
    {
        return tokenMap.get(token);
    }

    private static synchronized void register(Encoding encoding)
    {
        if (tokenMap == null)
        {
            tokenMap = new HashMap<>();
        }
        tokenMap.put(encoding.token,
                     encoding);
    }

    public final Encoding encoding;

    Encodings(Encoding encoding)
    {
        this.encoding = encoding;
        register(encoding);
    }
}
