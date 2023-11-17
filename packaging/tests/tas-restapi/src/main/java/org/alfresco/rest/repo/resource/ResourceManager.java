package org.alfresco.rest.repo.resource;

import org.alfresco.rest.repo.resource.general.Modifier;
import org.alfresco.rest.repo.resource.general.ResourceIntroducer;
import org.alfresco.rest.repo.resource.general.Specifier;
import org.alfresco.utility.model.TestModel;

/**
 * Declares operations introducing new, or allowing to manage repository resources like: folders, files, categories, associations, etc.
 *
 * @param <RESOURCE>> repository resource, e.g. folder, file, category, etc.
 * @param <SPECIFIER> repository resource specifier, see {@link Specifier}
 * @param <MODIFIER>> repository resource modifier, see {@link Modifier}
 */
public interface ResourceManager<RESOURCE extends TestModel, SPECIFIER extends Specifier, MODIFIER extends Modifier<RESOURCE, ?>>
    extends ResourceIntroducer<SPECIFIER>
{
    RESOURCE get(String id);

    MODIFIER modify(RESOURCE resource);

    default MODIFIER modify(String id)
    {
        return modify(get(id));
    }

    void delete(RESOURCE resource);
}
