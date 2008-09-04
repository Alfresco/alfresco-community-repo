# ! /bin/sh

svn co http://feedvalidator.googlecode.com/svn/trunk/apptestsuite/client/validator/ validator
python validator/appclienttest.py --html --verbose --output=results.html "http://bitworking.org/projects/apptestsite/app.cgi/service/;service_document" 
open results.html
