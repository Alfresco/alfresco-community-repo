package org.alfresco.rest.repo.resource.general;

/**
 * Declares an action introducing new repository resource like: folder, file, category, association, etc.
 *
 * @param <SPECIFIER> repository resource specifier, see {@link Specifier}
 */
@SuppressWarnings({"PMD.GenericsNaming"})
public interface ResourceIntroducer<SPECIFIER extends Specifier>
{
    SPECIFIER add();
}
