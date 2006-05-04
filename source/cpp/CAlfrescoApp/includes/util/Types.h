/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
