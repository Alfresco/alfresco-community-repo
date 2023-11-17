package org.alfresco.rest.repo.resource.general;

/**
 * Declares an action, which removes repository resource like association.
 *
 * @param <SPECIFIER> repository resource specifier, see {@link Specifier}
 */
public interface ResourceRemover<SPECIFIER extends Specifier>
{
    SPECIFIER remove();
}
