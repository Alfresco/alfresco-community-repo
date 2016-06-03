
package org.alfresco.traitextender;

/**
 * Implementors are sets of extension implementations that are registered on
 * specific {@link ExtensionPoint} for given {@link Extender}s.
 *
 * @author Bogdan Horje
 */
public interface ExtensionBundle
{
    /**
     * Sets up an registers extension factories with the give {@link Extender}
     * for all extensions defined by this bundle.
     * 
     * @param extender
     */
    void start(Extender extender);

    /**
     * Unregisters all defined extensions from the given {@link Extender} .
     * 
     * @param extender
     */
    void stop(Extender extender);

    String getId();
}
