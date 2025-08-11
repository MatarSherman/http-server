# Todos:

This project is in progress, here are some of the current goals/tasks.

## Tasks

- [ ] Add trailing headers support to responses
- [ ] Add encoding to serialization flow
- [ ] Add middlewares functionality
- [ ] Add automatic security headers
- [ ] Add HTTPS configuration (probably using JSSE SSLServerSocket)
- [x] Make deserializer return 400 for invalid http requests (InvalidHttpRequestException)
- [ ] Add support for websockets
- [ ] Add support for SSE
- [ ] Add handling for OPTION requests and cors
- [ ] Add handling of "Accept-Encoding" header, encode response
  based on request's accepted encodings, apply "vary" header,
  set content-encoding header, fallback to no encoding if needed
- [ ] Add handling of "Accept" header in the request, against
  the body of the response (probably requires adding a "getMimeType"
  on HttpBodySerializer), return 406 if incompatible, add "vary" header
- [ ] Add ETag and related headers handling to serialization flow,
  provide a default "addETag" and handling for if-none-match header,
  end-user should handle if-match header.
- [ ] Add handling for Http HEAD method requests
- [ ] Add validation for HttpResponse: for the status code and message
- [x] Use charset in Content-Type header for String and JSON bodies
- [ ] Remove body serialization for Http responses with status codes: 1xx, 204, 205, 304
- [x] Add date header to serialization
- [ ] Add graceful shutdown
- [ ] Add an environment-variables/configuration file (PORT, timeout, etc...)
- [ ] Add documentation
- [ ] Add robust logging with levels
- [ ] Add unit testing for the main logic
- [ ] Add project-wide meaningful comments
- [ ] Add url path params deserialization
- [x] Add 404 not found default route
- [x] Send 500 status response on internal error
- [ ] Change deserialization flow to start by deserializing
  the path+method, then check against defined routes, then
  continue flow if the route exists
- [ ] Add static file serving functionality
- [x] Add timeout to connections/sockets
- [ ] Add file upload support to deserialization via Apache
  Commons FileUpload (also requires adding "uploadedFiles" field to HttpRequest)
- [ ] Add varying timeouts for: file uploads, long-polling, SSE, keep-alive requests
- [ ] Add support for different charsets in deserialization

## Possible tasks

- [ ] handle Keep-alive and Connection headers
- [x] Convert the "headers" field in request/response from list to map
- [ ] Disallow end-user from setting "content-length" header manually
- [ ] Add file body serializers (and other common serializers)
