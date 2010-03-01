/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
