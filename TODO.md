# Todos:

This project is in progress, here are some of the current goals/tasks.

## Tasks

- [ ] Add graceful shutdown
- [ ] Add an environment-variables/configuration file (PORT, timeout, etc...)
- [ ] Add documentation
- [ ] Add robust logging with levels
- [ ] Add unit testing for the main logic
- [ ] Add project-wide meaningful comments
- [ ] Add url path params deserialization
- [x] Add 404 not found default route
- [ ] Send 500 status response on internal error
- [ ] Change deserialization flow to start by deserializing the path+method, then check against defined routes,
then continue flow if the route exists
- [ ] Add static file serving functionality
- [x] Add timeout to connections/sockets
- [ ] Add file upload support to deserialization via Apache Commons FileUpload (also requires adding "uploadedFiles" field to HttpRequest)
- [ ] Add varying timeouts for: file uploads, long-polling, SSE, keep-alive requests
- [ ] Add support for different charsets in deserialization

## Possible tasks

- [x] Convert the "headers" field in request/response from list to map
- [ ] Disallow end-user from setting "content-length" header manually
- [ ] Add file body serializers (and other common serializers)
