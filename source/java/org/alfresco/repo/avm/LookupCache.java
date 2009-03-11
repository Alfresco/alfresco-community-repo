/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.avm;

import org.alfresco.repo.avm.util.SimplePath;

/**
 * All lookup traffic goes through here.
 */
public interface LookupCache
{
   
   public Lookup lookup(AVMStore store, int version, SimplePath path, boolean write, boolean includeDeleted);

   // Following are the cache invalidation calls.

   /**
    * Called when a simple write operation occurs.  This
    * invalidates all read lookups and all layered lookups.
    */
   public void onWrite(String storeName);

   /**
    * Called when a delete has occurred in a store.  This invalidates both
    * reads and write lookups in that store.
    */
   public void onDelete(String storeName);

   /**
    * Called when a snapshot occurs in a store.  This invalidates write
    * lookups.  Read lookups stay untouched.
    */
   public void onSnapshot(String storeName);

   /**
    * Full reset of cache.
    */
   public void reset();

}
