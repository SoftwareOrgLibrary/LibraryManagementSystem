What Shahd worked:
1. **JUnit 5 (Jupiter)**: JUnit 5 was used as the main framework for writing and running unit tests,
providing a modern and flexible API for writing tests with support for new annotations.
2. **Mockito 5.11.0**: Mockito was used to create mock objects in tests, allowing isolation of units
under test and testing them independently from external dependencies.
3. **JaCoCo 0.8.10**: JaCoCo was used to measure code coverage, providing detailed reports on the
percentage of code that has been tested, helping to identify areas that need more testing.
4. **SonarCloud**: SonarCloud was used as a cloud platform for code quality analysis, automatically
scanning code to discover potential errors, code smells, and security issues.
5. **Javadoc**: Javadoc was used to generate automatic code documentation, converting special
comments in the code into comprehensive HTML documentation that facilitates understanding of
APIs and components.
6. **javax.mail 1.6.2**: The javax.mail library was used for sending emails, allowing notifications to
be sent to users via SMTP protocol, supporting reminder and notification functionality in the system.
7. **Maven Surefire Plugin 3.2.5**: Maven Surefire Plugin was used to automatically run unit tests
during the build process, executing all tests and producing detailed reports on results.
8. **SecureRandom**: SecureRandom was used to generate secure random numbers, providing a
reliable source of random numbers for operations requiring high security.
9. **Strategy Pattern**: The Strategy pattern was applied for fine calculation, using different
strategies to calculate fines based on media type (books or CDs), providing flexibility in adding new
types.
10. **Repository Pattern**: The Repository pattern was applied for data management, providing an
abstraction layer between business logic and the database, making code easier to test and allowing
data source changes later.
11. **Dependency Injection**: Dependency injection was manually implemented in the project,
passing dependencies through constructors, improving testability and reducing coupling between
components.
12. **Domain-Driven Design (DDD)**: Domain-Driven Design principles were applied in code
organization, separating business logic (Domain Logic) from infrastructure layers, improving
maintainability.
13. **Layered Architecture**: A layered architecture was used, organizing code into separate layers
(Domain, Repository, Service, App), improving organization and maintainability.
14. **In-Memory Repositories**: In-memory data repositories were used, storing data in in-memory
data structures instead of a database, facilitating testing and development.
15. **Session Management**: A session management system was implemented for users and
administrators, tracking login status and permissions, ensuring security and access control.
16. **Custom Exceptions**: Custom exceptions were created for the project
(AuthenticationException, AuthorizationException, BorrowingNotAllowedException), providing
clearer and more precise error handling.
17. **Interface Segregation**: The Interface Segregation principle from SOLID was applied, dividing
large interfaces into smaller, more specialized interfaces, improving maintainability and usability.
18. **Code Coverage Reports**: Code coverage reports were generated from JaCoCo, visually
displaying the percentage of code that has been tested, helping to identify areas that need more
testing.
19. **Static Code Analysis**: Static code analysis was used via SonarCloud, automatically scanning
code to discover potential issues and programming errors, improving code quality.
20. **Quality Gates**: Quality gates were used in SonarCloud, defining quality standards that code
must meet before approval, ensuring high standards are maintained.
21. **HTML Documentation**: HTML documentation was generated from Javadoc, converting
comments in code into interactive HTML pages, making it easier for developers to understand and
use the code.
22. **UTF-8 Encoding**: UTF-8 encoding was used for texts to support Arabic and all special
characters, ensuring correct text display across all operating systems.
23. **Console Application**: A console application was developed, where users interact with the
system through a text interface, providing a simple and clear user experience.
24. **TimeProvider Interface**: A TimeProvider interface was created to provide time, allowing time
simulation in tests, making it easier to test time-dependent functions without waiting for actual
time.
25. **RealTimeProvider**: A real-time provider implementation (RealTimeProvider) was created,
returning real time from the system, ensuring the system works correctly in production
environments.
And here what Shereen Worked:
26. **Fine Calculation Strategies**: Fine calculation strategies were implemented
(BookFineStrategy, CDFineStrategy), calculating fines differently based on media type, providing
flexibility in defining fine rules.
27. **Media Types Enum**: An enumeration (Enum) was used for media types (Book, CD), defining
media types safely and clearly, preventing errors and improving readability.
28. **IdGenerator**: A sequential unique ID generator (IdGenerator) was created, automatically
generating unique IDs for each new entity, ensuring no ID duplication.
29. **Email Notification System**: An email notification system was developed, sending
notifications to users about due dates and reminders, improving user experience.
30. **SMTP Email Server**: An SMTP email server was developed (SmtpEmailServer), sending email
messages via SMTP protocol, ensuring notifications reach users.
31. **Reminder Service**: A user reminder service was developed (ReminderService), sending
reminders to users about due items, helping to reduce return delays.
32. **Loan Management**: A loan management system was developed, tracking all borrowing and
return operations, providing a complete record of all transactions.
33. **User Management**: A user management system was developed, adding, deleting, and
modifying user data, providing comprehensive user database management.
34. **Admin Authentication**: An administrator authentication system was developed, verifying
administrator identities before allowing access to administrative functions, ensuring security.
35. **Search Functionality**: A media search functionality was developed, allowing search for books
and CDs by title, author, or ISBN, making it easier for users to find what they're looking for.
36. **Fine Payment System**: A fine payment system was developed, allowing users to pay
outstanding fines, providing an organized way to settle fines.
37. **FineCalculator**: A fine calculator (FineCalculator) was developed, calculating fines owed by
users based on number of overdue days and type of borrowed media.
38. **AuthService**: An authentication service (AuthService) was developed, managing login and
logout operations for administrators, with validation of credentials.
39. **LibraryService**: The main library service (LibraryService) was developed, managing all core
operations such as borrowing, returning, searching, and user management.
40. **ReminderService**: A reminder service (ReminderService) was developed, sending reminders
to users about due or overdue items.
41. **SessionManager**: A session manager (SessionManager) was developed, tracking login status
for users and administrators, and managing permissions and access.
42. **InMemoryAdminRepository**: An in-memory admin repository (InMemoryAdminRepository)
was developed, storing administrator data in memory with support for search and save operations.
43. **InMemoryUserRepository**: An in-memory user repository (InMemoryUserRepository) was
developed, storing user data in memory with support for full management operations.
44. **InMemoryMediaRepository**: An in-memory media repository (InMemoryMediaRepository)
was developed, storing book and CD data in memory with support for search and management.
45. **InMemoryLoanRepository**: An in-memory loan repository (InMemoryLoanRepository) was
developed, storing loan data in memory with support for tracking and management operations.
46. **EmailNotifier**: An email notifier (EmailNotifier) was developed, sending notifications to users
via email using the specified mail server.
47. **SmtpEmailServer**: An SMTP email server (SmtpEmailServer) was developed, implementing
actual email message sending via SMTP protocol.
48. **BookFineStrategy**: A book fine calculation strategy (BookFineStrategy) was developed,
calculating fines for books based on specific rules (e.g., 5 units per day of delay).
49. **CDFineStrategy**: A CD fine calculation strategy (CDFineStrategy) was developed, calculating
fines for CDs based on different rules than books (e.g., 10 units per day of delay).
51. **TimeProvider Abstraction**: A time provider abstraction (TimeProvider Interface) was
created, allowing separation of time logic from implementation, making it easier to test timedependent code by using a mock time provider in tests.

This image show that there are two projects:
1- Libmng
2- LibraryManagementsystem
the first Project is made just to show the coverage result of the java project
And the second Project is made to show the quality analysis for it





















