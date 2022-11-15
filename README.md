Create a test suite for testing the following methods from this REST service using Java: https://jsonplaceholder.typicode.com
GET
/posts
GET
/posts/ {id}
GET
/posts/ {id} /comments
GET
/comments?postId= {id}
POST
/posts
PUT
/posts/ {id}
PATCH
/posts/ {id}
DELETE
/posts/ {id}


ACCEPTANCE CRITERIA
Tests should be built with layered architecture (core, domain, tests levels)
Tests should be created using either Rest Assured or Spring Rest Template or Apache Http Client.
Tests have to include critical path tests validations both positive and negative (define a set of tests on your own).
Implemented tests should be readable with needed comments.
Tests must be implemented so that they could be launched in parallel.
Naming and Code Conventions should be followed – i.e. https://google.github.io/styleguide/javaguide.html or any other.
