Automation testing task #2 (API)

Resource: https://swapi.dev/

Endpoints examples:

 find in site tutorial

Tools: Java AND Rest Assured

Use Cases 1:
1. Find the film with latest release date.
2. Using previous response (1) find the tallest person among the characters that
were part of that film.
3. Find the tallest person ever played in any Star Wars film.
Use Cases 2:
1. Create contract test (Json schema validation) for /people API.

How to Run tests

After cloning the project execute the command 'mvn test -DsuiteXmlFile="testng.xml"'

To view the test report, navigate to the following directory: target/surefire-reports/html/ and open the file index.html in a browser of your choice
