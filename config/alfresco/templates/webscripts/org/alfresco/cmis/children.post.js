script:
{
    // TODO: Handle direct posting of media
    
    status.code = 400;
    status.message = "Posting of media resource not supported";
    status.redirect = true;
    break script;
}
