package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;

/**
 * Handle collection of <RestSharedLinksModel>
 * 
 * @author meenal bhave
 * Example:
 * {
 *      "list": 
 *      {
 *              "pagination": 
 *              {
 *                      "count": 2,
 *                      "hasMoreItems": false,
 *                      "totalItems": 2,
 *                      "skipCount": 0,
 *                      "maxItems": 100
 *              },
 *              "entries": [
 *              {
 *                      "entry": 
 *                      {
 *                              "id": "doclib",
 *                              "content": 
 *                              {
 *                                      "mimeType": "image/png",
 *                                      "mimeTypeName": "PNG Image",
 *                                      "sizeInBytes": 414,
 *                                      "encoding": "UTF-8"
 *                              },
 *                              "status": "CREATED"
 *                      }
 *              },
 *              {
 *                      "entry": 
 *                      {
 *                              "id": "pdf",
 *                              "content": 
 *                              {
 *                                      "mimeType": "application/pdf",
 *                                      "mimeTypeName": "Adobe PDF Document",
 *                                      "sizeInBytes": 10144,
 *                                      "encoding": "UTF-8"
 *                              },
 *                              "status": "CREATED"
 *                      }
 *              }
 *              ]
 *      }
 * }
 */
public class RestRenditionInfoModelCollection extends RestModels<RestRenditionInfoModel, RestRenditionInfoModelCollection>
{
}