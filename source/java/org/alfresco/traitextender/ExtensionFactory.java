
package org.alfresco.traitextender;

/**
 * Creates extension instances for given {@link Trait}s and
 * {@link ExtensionPoint}s.
 *
 * @author Bogdan Horje
 */
public interface ExtensionFactory<E>
{
    <T extends Trait> E createExtension(T trait);

    /**
     * @param point
     * @return <code>true</code> if the given extensio-point API elements are
     *         compatible with the returned extension (i.e. the given extension
     *         API is assignable form the type of the extension created by this
     *         factory and the {@link Trait} accepted as aparameter in
     *         {@link #createExtension(Trait)} is assignable from the type of
     *         the given trait API).
     */
    boolean canCreateExtensionFor(ExtensionPoint<?, ?> point);
}
