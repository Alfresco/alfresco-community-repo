/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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

#ifndef _AlfrescoTypes_H
#define _AlfrescoTypes_H

//	Includes

#include "util\JavaTypes.h"

namespace Alfresco {

	//	Type definitions
	//
	//	Data buffer pointer, position and length

	typedef unsigned char* BUFPTR;
	typedef unsigned int BUFPOS;
	typedef unsigned int BUFLEN;

	typedef const unsigned char* CBUFPTR;
	typedef const unsigned int CBUFPOS;
	typedef const unsigned int CBUFLEN;

	//	File position and length

	typedef LONG64	FILEPOS;
	typedef LONG64	FILELEN;

	//	Date/time

	typedef LONG64	DATETIME;
	#define NULL_DATETIME ((DATETIME) 0)
}

#endif
