/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.dictionary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.util.CachingDateFormat;


/**
 * Support translating model from and to XML
 * 
 * @author David Caruana
 *
 */
public class M2XML
{
   
    /**
     * Convert XML date (of the form yyyy-MM-dd) to Date 
     * 
     * @param date  the xml representation of the date
     * @return  the date
     * @throws ParseException
     */
    public static Date deserialiseDate(String date)
        throws ParseException
    {
        Date xmlDate = null;
        if (date != null)
        {
            SimpleDateFormat df = CachingDateFormat.getDateOnlyFormat();
            xmlDate = df.parse(date);
        }
        return xmlDate;
    }

    
    /**
     * Convert date to XML date (of the form yyyy-MM-dd)
     * 
     * @param date  the date
     * @return  the xml representation of the date
     */
    public static String serialiseDate(Date date)
    {
        String xmlDate = null;
        if (date != null)
        {
            SimpleDateFormat df = CachingDateFormat.getDateOnlyFormat();
            xmlDate = df.format(date);
        }
        return xmlDate;
    }
    
}
