package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;

/**
 * Handle collection of <RestContainerModel>
 * Example:
 * {
 * "list": {
 * "pagination": {
 * "count": 1,
 * "hasMoreItems": false,
 * "totalItems": 1,
 * "skipCount": 0,
 * "maxItems": 100
 * },
 * "entries": [
 * {
 * "entry": {
 * "id": "d79666e2-3d77-4cbd-aa15-a1e0dcc4da1e",
 * "folderId": "documentLibrary"
 * }
 * }
 * ]
 * }
 * }
 */
public class RestSiteContainerModelsCollection extends RestModels<RestSiteContainerModel, RestSiteContainerModelsCollection>
{
}    