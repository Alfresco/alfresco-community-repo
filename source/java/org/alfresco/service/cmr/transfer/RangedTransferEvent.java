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
package org.alfresco.service.cmr.transfer;

/**
 * A Ranged Transfer event is a detail record for a State that has many smaller steps.   For example when sending content the first 
 * event is 1 of the number of files to send.   The second is 2 of the number of files to send.
 * 
 * These events are intended to support "progress bar" types of interfaces.
 *  
 * @author Mark Rogers
 */
public interface RangedTransferEvent extends TransferEvent
{
    /**
     * The position in the range
     * @return
     */
    long getPosition();
    
    /**
     * The maximum range
     * @return
     */
    long getRange();

}
