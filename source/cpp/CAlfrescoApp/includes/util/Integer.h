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

#ifndef _JavaInteger_H
#define _JavaInteger_H

//	Includes

#include "util\String.h"
#include "util\Exception.h"
#include "util\Types.h"

//	Classes defined in this header file

namespace Alfresco {
	class Integer;
}

/**
 * Java-like Integer Class
 *
 * Provides static methods to convert integer values to strings.
 */
class Alfresco::Integer {
public:
	//	Convert an integer to a hexadecimal string

	static String toHexString( const unsigned int ival);
	static String toHexString( BUFPTR ptr);

	//	Convert an integer value to a string

	static String toString( unsigned int ival, unsigned int radix = 10);

	//	Parse a string to generate an integer value

	static unsigned int parseInt( const String& str, unsigned int radix = 10);

private:
	//	Hide constructors, static only class

	Integer( void);
	Integer(Integer& ival);
};

#endif