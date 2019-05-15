package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;

/**
 * Handle collection of <RestPreferenceModel>
 * {
 * "list": {
 * "pagination": {
 * "count": 2,
 * "hasMoreItems": false,
 * "totalItems": 2,
 * "skipCount": 0,
 * "maxItems": 100
 * },
 * "entries": [
 * {
 * "entry": {
 * "id": "org.alfresco.ext.sites.favourites.site-lwdxYDQFIi.createdAt",
 * "value": "2016-09-30T16:31:05.085Z"
 * }
 * },
 * {
 * "entry": {
 * "id": "org.alfresco.share.sites.favourites.site-lwdxYDQFIi",
 * "value": true
 * }
 * }
 * ]
 * }
 * }
 *
 * @author Cristina Axinte
 */
public class RestPreferenceModelsCollection extends RestModels<RestPreferenceModel, RestPreferenceModelsCollection>
{
}    