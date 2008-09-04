# ! /bin/sh

# TODO: change url to Alfresco root service document

python validator/appclienttest.py --html --verbose --output=results.html "http://localhost:8080/alfresco/service/api/repository" 
open results.html
