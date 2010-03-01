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
