/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.person;

import org.alfresco.service.namespace.QName;

/**
 * Person model constants
 * 
 * @author Glen Johnson
 */
public interface PersonModel
{
    // Person Model
    public static final String PERSON_MODEL_URL = "http://www.alfresco.org/model/person/1.0";
    public static final String PERSON_MODEL_PREFIX = "pers";
    
    // Person
    public static final QName TYPE_PERSON = QName.createQName(PERSON_MODEL_URL, "person");
    public static final QName PROP_PERSON_USER_NAME = QName.createQName(PERSON_MODEL_URL, "personUserName");
    public static final QName PROP_PERSON_TITLE = QName.createQName(PERSON_MODEL_URL, "personTitle");
    public static final QName PROP_PERSON_FIRST_NAME = QName.createQName(PERSON_MODEL_URL, "personFirstName");
    public static final QName PROP_PERSON_LAST_NAME = QName.createQName(PERSON_MODEL_URL, "personLastName");
    public static final QName PROP_PERSON_ORGANISATION = QName.createQName(PERSON_MODEL_URL, "personOrganisation");
    public static final QName PROP_PERSON_JOB_TITLE = QName.createQName(PERSON_MODEL_URL, "personJobTitle");
    public static final QName PROP_PERSON_EMAIL = QName.createQName(PERSON_MODEL_URL, "personEmail");
    public static final QName PROP_PERSON_BIO = QName.createQName(PERSON_MODEL_URL, "personBio");
    public static final QName PROP_PERSON_AVATAR_URL = QName.createQName(PERSON_MODEL_URL, "personAvatarUrl");
    // TODO glen.johnson@alfresco.com add something here for person filter preset
}
