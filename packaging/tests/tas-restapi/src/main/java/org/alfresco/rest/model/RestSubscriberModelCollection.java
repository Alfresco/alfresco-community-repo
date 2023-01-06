/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
/*
 * Copyright 2017 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */

package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;

/**
 * Handle collection of <RestSubscriptionModel>
 * 
 * @author meenal bhave
 * Example:
 * {
    "list": {
        "pagination": {
            "count": 20,
            "hasMoreItems": false,
            "totalItems": 20,
            "skipCount": 0,
            "maxItems": 100
        },
        "entries": [
            {
                "entry": {
                    "createdAt": "2017-07-14T13:52:47.319+0000",
                    "syncService": {
                        "id": "0",
                        "uri": "https://localhost:9090/alfresco",
                        "config": {
                            "filters": {
                                "nodeAspects": [
                                    "rma:filePlanComponent",
                                    "sf:*",
                                    "smf:*",
                                    "cm:workingcopy"
                                ],
                                "smartFolderNodeAspects": [
                                    "sf:*",
                                    "smf:*"
                                ],
                                "nodeTypesWhitelist": [
 
                                ],
                                "nodeTypes": [
                                    "bpm:package",
                                    "cm:systemfolder",
                                    "cm:failedThumbnail"
                                ]
                            },
                            "dsyncClientVersionMin": "1.0.1",
                            "repoInfo": {
                                "versionLabel": "5.2.2",
                                "edition": "Enterprise"
                            }
                        }
                    },
                    "deviceOS": "windows",
                    "syncServiceId": "0",
                    "id": "da34e9e5-57c8-4275-b24a-373b9f56e6da"
                }
            },
            {
                "entry": {
                    "createdAt": "2017-07-14T16:14:29.851+0000",
                    "syncService": {
                        "id": "0",
                        "uri": "https://localhost:9090/alfresco",
                        "config": {
                            "filters": {
                                "nodeAspects": [
                                    "rma:filePlanComponent",
                                    "sf:*",
                                    "smf:*",
                                    "cm:workingcopy"
                                ],
                                "smartFolderNodeAspects": [
                                    "sf:*",
                                    "smf:*"
                                ],
                                "nodeTypesWhitelist": [
                                    "dod:filePlan",
                                    "hwf:rejectedCloudTask",
                                    "imap:imapBody",
                                    "st:site"
                                ],
                                "nodeTypes": [
                                    "bpm:package",
                                    "cm:systemfolder",
                                    "cm:failedThumbnail",
                                    "rma:rmsite"
                                ]
                            },
                            "dsyncClientVersionMin": "1.0.1",
                            "repoInfo": {
                                "versionLabel": "5.2.2",
                                "edition": "Enterprise"
                            }
                        }
                    },
                    "deviceOS": "windows",
                    "syncServiceId": "0",
                    "id": "7c53fef9-d6fd-4657-830a-475850d6f04e"
                }
            },
            {
                "entry": {
                    "createdAt": "2017-07-21T13:19:11.792+0000",
                    "syncService": {
                        "id": "0",
                        "uri": "https://localhost:9090/alfresco",
                        "config": {
                            "filters": {
                                "nodeAspects": [
                                    "rma:filePlanComponent",
                                    "sf:*",
                                    "smf:*",
                                    "cm:workingcopy"
                                ],
                                "smartFolderNodeAspects": [
                                    "sf:*",
                                    "smf:*"
                                ],
                                "nodeTypesWhitelist": [
                                    "dod:filePlan",
                                    "hwf:rejectedCloudTask",
                                    "imap:imapBody",
                                    "st:site",
                                    "resetpasswordwf:requestPasswordResetTask",
                                    "rma:hold",
                                    "rma:recordCategory",
                                    "hwf:approvedCloudTask"
                                ],
                                "nodeTypes": [
                                    "bpm:package",
                                    "cm:systemfolder",
                                    "cm:failedThumbnail"
                                ]
                            },
                            "dsyncClientVersionMin": "1.0.1",
                            "repoInfo": {
                                "versionLabel": "5.2.2",
                                "edition": "Enterprise"
                            }
                        }
                    },
                    "deviceOS": "windows",
                    "syncServiceId": "0",
                    "id": "d9141278-8105-4986-a3f6-c003fdb3925e"
                }
            }
 */
public class RestSubscriberModelCollection extends RestModels<RestSubscriberModel, RestSubscriberModelCollection>
{
} 

