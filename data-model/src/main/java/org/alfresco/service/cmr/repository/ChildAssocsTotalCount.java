package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Immutable record that encapsulates a list of child associations and the total count of child associations for a given parent node. This is typically used to return both the current page of child associations and the total number available, supporting pagination scenarios.
 *
 * @param childAssocs
 *            an unmodifiable list of {@link ChildAssociationRef} representing the child associations
 * @param totalCount
 *            the total number of child associations for the parent node, regardless of paging
 */
@AlfrescoPublicApi
public record ChildAssocsTotalCount(List<ChildAssociationRef> childAssocs, int totalCount) implements Serializable
{
    public static final ChildAssocsTotalCount EMPTY = new ChildAssocsTotalCount(Collections.emptyList(), 0);

    public ChildAssocsTotalCount
    {
        childAssocs = Collections.unmodifiableList(childAssocs);
    }
}
