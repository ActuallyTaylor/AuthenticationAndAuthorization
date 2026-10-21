# Authentication & Authorization Activity

This project is a demonstration of the Authentication & Authorization Tactic for SWEN-755. The general idea is to
demonstrate an implementation of the Java Authentication and Authorization Service (JAAS), to demonstrate the difference
between authentication and authorization, and to show those differences through a minimal application.

### Authentication

This application allows user accounts to be created, guarded by username and password. This enforces authentication on
users, as only the user who owns the account should be able to login via username and password. Thus, the user is known
to be authentic if they sign in successfully, and they should be permitted to perform any action for which they are
authorized.

### Authorization

User roles represent authorization in the context of this application. Some accounts are created with DEFAULT
privileges, meaning that they are only authorized to perform unrestricted actions in the system. Other accounts are
created with ADMIN privileges, which enable users signed into that account to perform restricted actions. This allows
user interaction with the system to be separated by permissions. Combined with authentication discussed above, the
system demonstrates how to securely prevent unauthorized users from performing actions they should not be able to
perform within a system.

## Run Commands

In order to run the application, the following commands are used:

```shell
mvn clean package
```

```shell
java -jar target/AuthenticationAndAuthorization-1.0-SNAPSHOT.jar
```

On first run, a user will be presented with the following prompts:

```
Creating new database!
Please register an admin account:
username:
```

To create the initial admin account, enter a username and password. Once that account is created, a login prompt will
show, from which the new admin account can be signed in. Once signed in, an admin will be presented with the following
options:

```
Choose a service:
 0) /users (DEFAULT)
 1) /register (ADMIN)
 2) /edit-user (ADMIN)
 3) /exit
```

Each service is specified by a URL, and the prompt shows the authorization level required for each service. An admin is
able to register new users and edit existing users, while any account is able to list all current users. To choose a
service, enter the URL.

## Service Descriptions/Notes

- `/users`: List all current existing users
- `/register`: Create a new user account. Allows specification of username, password, and access level
- `/edit-user`: Edit an existing user. To keep a username or password value unchanged, enter no text in the prompt.
- `/exit`: Exit the application

