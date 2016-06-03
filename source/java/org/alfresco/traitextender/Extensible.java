
package org.alfresco.traitextender;

/**
 * An {@link Extensible} object exposes a set of {@link Trait}s as
 * {@link ExtendedTrait}s objects.<br>
 * An {@link ExtendedTrait} is an association between a {@link Trait} exposing
 * object and several extension objects.<br>
 * The actual {@link Trait}s and associated extensions provided by an
 * {@link Extensible} object are given by its {@link ExtensionPoint} handling
 * strategy and by the current set of registered extensions (see
 * {@link Extender}).<br>
 * The exposed {@link Trait}s can be thought of as parts of an object's
 * interface that will be exposed to an extension. Upon the extension invocation
 * the given trait instances will be made available to their corresponding
 * extensions.
 *
 * @author Bogdan Horje
 */
public interface Extensible
{
    <T extends Trait> ExtendedTrait<T> getTrait(Class<? extends T> traitAPI);
}
