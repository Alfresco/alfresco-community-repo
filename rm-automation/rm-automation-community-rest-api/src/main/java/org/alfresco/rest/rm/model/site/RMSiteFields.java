/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.model.site;

/**
 *RM Site properties from the RM Model Schema
 *"entry": {
 *      "id": "string",
 *      "guid": "string",
 *      "title": "string",
 *      "description": "string",
 *      "visibility": "{@link org.springframework.social.alfresco.api.entities.Site.Visibility}",
 *      "compliance": "{@link RMSiteCompliance}",
 *      "role": "{@link org.alfresco.utility.constants.UserRole}"
 *}
 * @author Tuna Aksoy
 * @author Rodica Sutu
 * @since 2.6
 */
public class RMSiteFields
{
    public static final String ID = "id";
    public static final String COMPLIANCE = "compliance";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String VISIBILITY ="visibility";
    public static final String ROLE = "role";
}
