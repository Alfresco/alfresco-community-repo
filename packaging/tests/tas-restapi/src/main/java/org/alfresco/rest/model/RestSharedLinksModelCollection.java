package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;

/**
 * Handle collection of <RestSharedLinksModel>
 * 
 * @author meenal bhave
 * Example:
 * {
 *    "list": {
 *        "pagination": {
 *            "count": 1,
 *            "hasMoreItems": false,
 *            "totalItems": 1,
 *            "skipCount": 0,
 *            "maxItems": 100
 *        },
 *        "entries": [
 *            {
 *                "entry": {
 *                    "path": {
 *                        "name": "/Company Home/Sites/sitePublic-LACmuuuWwbeBYeG/documentLibrary/Folder-uxxKzDflwxOuetG/Folder-oawzdncUXFLgnFe",
 *                        "isComplete": true,
 *                        "elements": [
 *                            {
 *                                "id": "50e1fe3c-d60f-4088-8f79-c6c10031a379",
 *                                "name": "Company Home"
 *                            },
 *                            {
 *                                "id": "ad7887fe-ac12-464e-ab26-486b0574959c",
 *                                "name": "Sites"
 *                            },
 *                            {
 *                                "id": "98db769c-9fa1-47ed-b66c-37a59f2c7a3c",
 *                                "name": "sitePublic-LACmuuuWwbeBYeG"
 *                            },
 *                            {
 *                                "id": "155ffb4f-0d81-4548-bde1-44c41f10b34d",
 *                                "name": "documentLibrary"
 *                            },
 *                            {
 *                                "id": "bd1bb93b-bcc3-40d9-857b-5222880d1e4d",
 *                                "name": "Folder-uxxKzDflwxOuetG"
 *                            },
 *                            {
 *                                "id": "7f0c47ae-d334-4b66-a86b-1a60d2518ad1",
 *                                "name": "Folder-oawzdncUXFLgnFe"
 *                            }
 *                        ]
 *                    },
 *                    "modifiedAt": "2017-08-01T09:53:20.784+0000",
 *                    "modifiedByUser": {
 *                        "id": "User-hleADFGrDvxsonw",
 *                        "displayName": "User-hleADFGrDvxsonw FirstName LN-User-hleADFGrDvxsonw"
 *                    },
 *                    "name": "file-CUNTdqhjLRaMPuQ.txt",
 *                    "id": "UztDeg4hSju1DSVZHA3Zmg",
 *                    "nodeId": "fe065c44-4be2-4231-affa-fee8617eb4e6",
 *                    "sharedByUser": {
 *                        "id": "user2",
 *                        "displayName": "user2"
 *                    },
 *                    "content": {
 *                        "mimeType": "text/plain",
 *                        "mimeTypeName": "Plain Text",
 *                        "sizeInBytes": 19,
 *                        "encoding": "ISO-8859-2"
 *                    }
 *                }
 *            }
 *        ]
 *    }
 * }
 */
public class RestSharedLinksModelCollection extends RestModels<RestSharedLinksModel, RestSharedLinksModelCollection>
{
} 