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

#include "util\Exception.h"

using namespace Alfresco;
using namespace std;

//	Define standard Java-like exceptions

EXCEPTION_CLASS(Alfresco, IOException);
EXCEPTION_CLASS(Alfresco, NullPointerException);
EXCEPTION_CLASS(Alfresco, ArrayIndexOutOfBoundsException);
EXCEPTION_CLASS(Alfresco, NumberFormatException);

/**
* Class constructor
* 
* @param moduleName const char*
* @param lineNum unsigned int
* @param msg const wchar_t*
* @param msg2 const wchar_t*
* @param msg3 const wchar_t*
* @param msg4 const wchar_t*
* @param msg5 const wchar_t*
*/
Exception::Exception( const char* moduleName, unsigned int lineNum, const wchar_t* msg, const wchar_t* msg2,
										  const wchar_t* msg3, const wchar_t* msg4, const wchar_t* msg5) {

	//	Prefix the message string with the module name and line number

	m_msg = moduleName;
	if ( lineNum != 0) {
		m_msg += " (";
		m_msg += lineNum;
		m_msg += ")";
	}
	m_msg += ": ";

	//	Add the messages parts

	if ( msg)
		m_msg += msg;

	if ( msg2) {
		m_msg += " ";
		m_msg += msg2;
	}

	if ( msg3) {
		m_msg += " ";
		m_msg += msg3;
	}

	if ( msg4) {
		m_msg += " ";
		m_msg += msg4;
	}

	if ( msg5) {
		m_msg += " ";
		m_msg += msg5;
	}
}

/**
 * Class constructor
 * 
 * @param msg const wchar_t*
 * @param msg2 const wchar_t*
 * @param msg3 const wchar_t*
 * @param msg4 const wchar_t*
 * @param msg5 const wchar_t*
 */
Exception::Exception( const wchar_t* msg, const wchar_t* msg2, const wchar_t* msg3, const wchar_t* msg4, const wchar_t* msg5) {
	if ( msg)
		m_msg = msg;

	if ( msg2) {
		m_msg += " ";
		m_msg += msg2;
	}

	if ( msg3) {
		m_msg += " ";
		m_msg += msg3;
	}

	if ( msg4) {
		m_msg += " ";
		m_msg += msg4;
	}

	if ( msg5) {
		m_msg += " ";
		m_msg += msg5;
	}
}

/**
 * Copy constructor
 * 
 * @param ex const Exception&
 */
Exception::Exception( const Exception& ex) {
	m_msg = ex.getMessage();
}

/**
 * Class destructor
 * 
 */
Exception::~Exception() {
}


