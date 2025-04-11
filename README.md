# Currency exchange
REST API for describing currencies and exchange rates. Allows you to view and edit lists of currencies and exchange rates, and calculate conversion of arbitrary amounts from one currency to another. The application accepts HTTP requests, performs validation, executes business logic and sends a JSON response.

## Some details for users
- User-friendly data entry. Currency code, name and symbol can be entered in lowercase and uppercase letters with extra spaces.
  
  Enter: 
  
  aUd - australiAN&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DOLLAR - &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;a$
  
  Saved result:

  AUD - Australian Dollar - A$


- A decimal number can be entered using a period, comma, and extra spaces before the number.


- Strict validation of currency input. Only real currencies. After entering a valid currency code and an incorrect name or symbol, a hint will be given for the correct input.



## Some technical details
- The final version of the project does NOT use Lombok.


- Hikari configuration is done in the hikari.properties file in the resources folder.


- Error handling is partially implemented in the filter, partially in servlets, with the aim of outputting additional common text for several errors in one of the servlets.


- The DAO classes are implemented as singletons, the overall configuration takes place in the AppConfig class.


- There is a small change in the test frontend. Now when editing an existing exchange rate, when entering an incorrect numerical value, for example, 5.5ddd, this value will not only not be saved to the database, but will not even be temporarily displayed in the browser until the page is reloaded.


## Technologies / tools used
- Java 17
- Jakarta Servlet 6.1.0
- Apache Tomcat 10
- Apache Maven
- JDBC
- SQLite
- Hikari Connection Pool
- Jackson
- Postman