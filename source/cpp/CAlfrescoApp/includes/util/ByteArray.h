/*
* Copyright (C) 2005 Alfresco, Inc.
*
* Licensed under the Alfresco Network License. You may obtain a
* copy of the License at
*
*   http://www.alfrescosoftware.com/legal/
*
* Please view the license relevant to your network subscription.
*
* BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
* READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
* YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
* ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
* THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
* AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
* TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
* BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
* HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
* SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
* TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
* CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
*/

#ifndef _ByteArray_H
#define _ByteArray_H

//	Includes

#include <memory>
#include <string>

#include "util\Types.h"

//	Classes defined in this header file

namespace Alfresco {
	class ByteArray;

	typedef std::auto_ptr<ByteArray> PTR_ByteArray;
}

/**
 * Byte Array Class
 * 
 * Provides a byte array object similar to Javas byte[].
 */
class Alfresco::ByteArray {
public:
	//	Constructors

	ByteArray( BUFLEN len = 0, bool clearMem = false);
	ByteArray( CBUFPTR data, BUFLEN len);
	ByteArray( const char* data, BUFLEN len);

	//	Copy constructor

	ByteArray( const ByteArray& byts);

	//	Class destructor

	~ByteArray();

	//	Return the data/length

	inline CBUFPTR getData( void) const { return m_data; }
	inline BUFPTR getData( void) { return m_data; }
	inline BUFLEN getLength( void) const { return m_length; }

	//	Set the array length

	void setLength( BUFLEN len, bool clearMem = false);

	//	Set a byte

	void setByte( unsigned int idx, unsigned char val);

	//	Subscript operator

	unsigned char& operator[](const unsigned int idx);

	//	Assignment operator

	ByteArray& operator=( const ByteArray& byts);
	ByteArray& operator=( std::string& byts);

	//	Equality operator

	bool operator== ( const ByteArray& byts);

	//	Return the start address of the byte array

	operator const unsigned char* ( void) { return m_data; }

protected:
	//	Set the byte array and length

	void setData( CBUFPTR data, BUFLEN len);

private:
	//	Instance variables
	//
	//	Byte data and length

	BUFPTR m_data;
	BUFLEN m_length;
};

#endif
