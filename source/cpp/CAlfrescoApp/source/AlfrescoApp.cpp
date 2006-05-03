#include <iostream>

#include "alfresco\Alfresco.hpp"

#include "util\String.h"
#include "util\DataBuffer.h"
#include "util\FileName.h"

#include <shellapi.h>

using namespace std;
using namespace JLAN;

// Function prototypes

bool doFolderStatus( Alfresco& alfresco, const wchar_t* fileSpec = L"*.*");
bool doCheckInOut( Alfresco& alfresco, StringList& files);
bool doCheckIn( Alfresco& alfresco, PTR_AlfrescoFileInfo& fileInfo);
bool doCheckOut( Alfresco& alfresco, PTR_AlfrescoFileInfo& fileInfo);

/**
 * Alfresco Windows Drag And Drop Application
 *
 * @author GKSpencer
 */
int wmain( int argc, wchar_t* argv[], wchar_t* envp[]) {

	// Output a startup banner

	wcout << L"Alfresco Drag And Drop Application" << endl;
	wcout << L"----------------------------------" << endl;

	// Check if the app is running from a network drive

	String appPath(argv[0]);
//	String appPath("\\\\StarlaA\\Alfresco\\Garys Space\\AlfrescoApp.exe");
//	String appPath("Z:\\Garys Space\\AlfrescoApp.exe");
//	argc = 2;

	// Looks like a UNC path, trim off the application name

	int pos = appPath.lastIndexOf(PathSeperator);
	if ( pos < 0) {
		wcout << L"%% Invalid application path, " << appPath << endl;
		return 1;
	}

	// Get the path to the folder containing the application

	String folderPath = appPath.substring(0, pos);

	// Create the Alfresco interface

	Alfresco alfresco(folderPath);
	if ( alfresco.isAlfrescoFolder()) {

		// If there are no file paths on the command line then display a status page for the files
		// in the Alfresco folder

		if ( argc == 1) {

			// Display status for the files in the Alfresco folder

			doFolderStatus( alfresco);
		}
		else {

			// Build a list of the file names

			StringList fileList;

			for ( int i = 1; i < argc; i++)
				fileList.addString( String(argv[i]));
//				fileList.addString(L"N:\\testArea\\msword\\CIFSLOG.doc");
//			fileList.addString(L"\\\\StarlaA\\Alfresco\\Garys Space\\CIFSLOG.doc");

			// Process the file list and check in or out each file

			doCheckInOut( alfresco, fileList);
		}
	}
	else {
		wcout << L"%% Not a valid Alfresco CIFS folder, " << folderPath << endl;
		return 1;
	}

	// Wait for user input

	wcout << L"Press <Enter> to continue ..." << flush;
	getchar();
}

/**
 * Display file status of the files in the target Alfresco folder
 *
 * @param Alfresco& alfresco
 * @param const wchar_t* fileSpec
 * @return bool
 */
bool doFolderStatus( Alfresco& alfresco, const wchar_t* fileSpec) {

	// Get the base UNC path

	String uncPath = alfresco.getUNCPath();
	uncPath.append(PathSeperator);

	// Search the Alfresco folder

	WIN32_FIND_DATA findData;
	String searchPath = uncPath;
	searchPath.append( fileSpec);

	bool sts = false;
	HANDLE fHandle = FindFirstFile( searchPath, &findData);

	if ( fHandle != INVALID_HANDLE_VALUE) {

		// Loop until all files have been returned

		PTR_AlfrescoFileInfo pFileInfo;
		sts = true;

		while ( fHandle != INVALID_HANDLE_VALUE) {

			// Get the file name, ignore the '.' and '..' files

			String fName = findData.cFileName;

			if ( fName.equals(L".") || fName.equals(L"..")) {

				// Get the next file/folder name in the search

				if ( FindNextFile( fHandle, &findData) == 0)
					fHandle = INVALID_HANDLE_VALUE;
				continue;
			}

			// Get the file information for the current file folder

			pFileInfo = alfresco.getFileInformation( findData.cFileName);

			if ( pFileInfo.get() != NULL) {

				// Output the file details

				wcout << pFileInfo->getName() << endl;

				if ( pFileInfo->isType() == TypeFolder)
					wcout << L" [Folder]" << endl;

				if ( pFileInfo->isWorkingCopy())
					wcout << L" [Work: " << pFileInfo->getCopyOwner() << L", " << pFileInfo->getCopiedFrom() << L"]" << endl;
				
				if ( pFileInfo->getLockType() != LockNone)
					wcout << L" [Lock: " << (pFileInfo->getLockType() == LockRead ? L"READ" : L"WRITE") << L", " <<
							pFileInfo->getLockOwner() << L"]" << endl;

				if ( pFileInfo->hasContent())
					wcout << L" [Content: " << pFileInfo->getContentLength() << L", " << pFileInfo->getContentType() << L"]" << endl;;

				// Get the next file/folder name in the search

				if ( FindNextFile( fHandle, &findData) == 0)
					fHandle = INVALID_HANDLE_VALUE;
			}
			else {
				sts = false;
				fHandle = INVALID_HANDLE_VALUE;
			}
		}
	}

	// Return status

	return sts;
}

/**
 * Process the list of files and check in or out each file
 *
 * @param alfresco Alfresco&
 * @param files StringList&
 */
bool doCheckInOut( Alfresco& alfresco, StringList& files) {

	// Process the list of files and either check in the file if it is a working copy or check out
	// the file

	for ( unsigned int i = 0; i < files.numberOfStrings(); i++) {

		// Get the current file name

		String curFile = files.getStringAt( i);

		// Check if the path is on an Alfresco mapped drive

		if ( alfresco.isMappedDrive() && curFile.startsWithIgnoreCase( alfresco.getDrivePath())) {

			// Convert the path to a UNC path

			String uncPath = alfresco.getRootPath();
			uncPath.append( curFile.substring(2));

			curFile = uncPath;
		}

		// Check that the path is to a file

		bool copyFile = false;

		DWORD attr = GetFileAttributes( curFile);
		if ( attr != INVALID_FILE_ATTRIBUTES && (attr & FILE_ATTRIBUTE_DIRECTORY) == 0) {

			// Get the file name from the path

			StringList nameParts = FileName::splitPath( curFile);
			String curName = nameParts.getStringAt( 1);

			// Get the Alfresco file status information

			PTR_AlfrescoFileInfo pFileInfo = alfresco.getFileInformation( curName);

			// If the path is to a file that is not on the Alfresco share the file will need to be copied,
			// after checking the status of a matching file in the Alfresco folder

			if ( curFile.length() >= 3 && curFile.substring(1,3).equals( L":\\")) {

				// Check if there is an existing file with the same name

				if ( pFileInfo.get() != NULL) {

					// Check if the file is a working copy

					if ( pFileInfo->isWorkingCopy()) {

						// Local file matches a working copy file in the Alfresco folder

						wcout << L"Found matching working copy for local file " << curName << endl;
					}
					else if ( pFileInfo->getLockType() != LockNone) {

						// File is locked, may be the original document

						wcout << L"%% Destination file " << curName << L" is locked" << endl;
						return false;
					}
					else {

						// Indicate that we have copied a new file to the Alfresco share, do not check in/out

						copyFile = true;
					}
				}
				else {

					// Indicate that we have copied a new file to the Alfresco share, do not check in/out

					copyFile = true;
				}

				// Build the from/to paths, must be double null terminated

				wchar_t fromPath[MAX_PATH + 1];
				wchar_t toPath[MAX_PATH + 1];

				memset( fromPath, 0, sizeof( fromPath));
				memset( toPath, 0, sizeof( toPath));

				wcscpy( fromPath, curFile.data());
				wcscpy( toPath, alfresco.getUNCPath());

				// Copy the local file to the Alfresco folder

				SHFILEOPSTRUCT fileOpStruct;
				memset( &fileOpStruct, 0, sizeof(SHFILEOPSTRUCT));

				fileOpStruct.hwnd  = HWND_DESKTOP;
				fileOpStruct.wFunc = FO_COPY;
				fileOpStruct.pFrom = fromPath;
				fileOpStruct.pTo   = toPath;
				fileOpStruct.fFlags= 0;
				fileOpStruct.fAnyOperationsAborted =false;

				// Copy the file to the Alfresco folder

				if ( SHFileOperation( &fileOpStruct) != 0) {

					// File copy failed

					wcout << L"%% Failed to copy file " << curFile << endl;
					return false;
				}
				else if ( fileOpStruct.fAnyOperationsAborted) {

					// User aborted the file copy

					wcout << L"%% Copy aborted for " << curFile << endl;
					return false;
				}

				// Get the file information for the copied file

				pFileInfo = alfresco.getFileInformation( curName);
			}

			// Check in or check out the file

			if ( pFileInfo.get() != NULL) {

				// Check if the file should be checked in/out

				if ( copyFile == false) {

					// Check if the file is a working copy, if so then check it in

					if ( pFileInfo->isWorkingCopy()) {

						// Check in the file

						doCheckIn( alfresco, pFileInfo);
					}
					else if ( pFileInfo->getLockType() == LockNone) {

						// Check out the file

						doCheckOut( alfresco, pFileInfo);
					}
					else {

						// File is locked, may already be checked out

						wcout << L"%% File " << curFile << L" is locked" << endl;
					}
				}
				else {

					// No existing file to link the copied file to

					wcout << L"Copied file " << curFile << L" to Alfresco share" << endl;
				}

			}
			else
				wcout << L"%% Failed to get file status for " << curFile << endl;
		}
		else
			wcout << L"%% Path " << curFile << L" is a folder, ignored" << endl;
	}

	// Return status

	return true;
}

/**
 * Check in the specified file
 *
 * @param alfresco Alfresco&
 * @param pFileInfo PTR_AlfrescoFileInfo&
 * @return bool
 */
bool doCheckIn( Alfresco& alfresco, PTR_AlfrescoFileInfo& pFileInfo) {

	bool checkedIn = false;

	try {

		// Check in the specified file

		alfresco.checkIn( pFileInfo->getName());

		wcout << L"Checked in file " << pFileInfo->getName() << endl;

		// Indicate that the check in was successful

		checkedIn = true;
	}
	catch (Exception ex) {
		wcerr << L"%% Error checking in file " << pFileInfo->getName() << endl;
		wcerr << L"   " << ex.getMessage() << endl;
	}

	// Return the check in status

	return checkedIn;
}

/**
 * Check out the specified file
 *
 * @param alfresco Alfresco&
 * @param pFileInfo PTR_AlfrescoFileInfo&
 * @return bool
 */
bool doCheckOut( Alfresco& alfresco, PTR_AlfrescoFileInfo& pFileInfo) {

	bool checkedOut = false;

	try {

		// Check out the specified file

		String workingCopy;
		alfresco.checkOut( pFileInfo->getName(), workingCopy);

		wcout << L"Checked out file " << pFileInfo->getName() << " to " << workingCopy << endl;

		// Indicate that the check out was successful

		checkedOut = true;
	}
	catch (Exception ex) {
		wcerr << L"%% Error checking out file " << pFileInfo->getName() << endl;
		wcerr << L"   " << ex.getMessage() << endl;
	}

	// Return the check out status

	return checkedOut;
}
