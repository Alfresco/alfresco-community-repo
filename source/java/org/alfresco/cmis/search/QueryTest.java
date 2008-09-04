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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.search;

import org.alfresco.cmis.dictionary.BaseCMISTest;

/**
 * @author andyh
 */
public class QueryTest extends BaseCMISTest
{
    public void testBasicSelect()
    {
        String query = "SELECT * FROM DOCUMENT_OBJECT_TYPE";
        cmisQueryService.query(query);
    }
    
    public void xtestParse1()
    {
        String query = "SELECT UPPER(1.0) AS WOOF FROM DOCUMENT_OBJECT_TYPE AS DOC LEFT OUTER JOIN FOLDER_OBJECT_TYPE AS FOLDER ON (DOC.NAME = FOLDER.NAME) WHERE LOWER(DOC.NAME = ' woof' AND CONTAINS(, 'one two three') AND  CONTAINS(, 'DOC.NAME:lemur AND woof') AND (DOC.NAME in ('one', 'two') AND IN_FOLDER('meep') AND DOC.NAME like 'woof' and DOC.NAME = 'woof' and DOC.OBJECT_ID = 'meep') ORDER BY DOC.NAME DESC, WOOF";
        cmisQueryService.query(query);
    }
    
    public void xtestParse2()
    {
        String query = "SELECT TITLE, AUTHORS, DATE FROM WHITE_PAPER WHERE ( IN_TREE( , 'ID00093854763') ) AND ( 'SMITH' = ANY AUTHORS )";
        cmisQueryService.query(query);
    }
    
    public void xtestParse3()
    {
        String query = "SELECT OBJECT_ID, SCORE() AS X, DESTINATION, DEPARTURE_DATES FROM TRAVEL_BROCHURE WHERE ( CONTAINS(, 'CARIBBEAN CENTRAL AMERICA CRUISE TOUR') ) AND ( '2009-1-1' < ANY DEPARTURE_DATES ) ORDER BY X DESC";
        cmisQueryService.query(query);
    }
    
    public void xtestParse4()
    {
        String query = "SELECT * FROM CAR_REVIEW WHERE ( LOWER(MAKE) = 'buick' ) OR ( ANY FEATURES IN ('NAVIGATION SYSTEM', 'SATELLITE RADIO', 'MP3' ) )";
        cmisQueryService.query(query);
    }
    
    public void xtestParse5()
    {
        String query = "SELECT Y.CLAIM_NUM, X.PROPERTY_ADDRESS, Y.DAMAGE_ESTIMATES FROM POLICY AS X JOIN CLAIMS AS Y ON ( X.POLICY_NUM = Y.POLICY_NUM ) WHERE ( 100000 <= ANY Y.DAMAGE_ESTIMATES ) AND ( Y.CAUSE NOT LIKE '%Katrina%' )";
        cmisQueryService.query(query);
    }
}
