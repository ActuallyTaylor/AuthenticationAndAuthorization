# Project Purpose
This project is a prototype Java system that uses an implementation of JAAS to handle authenticating and authorizing users. There are two types of users in the system, with separate authorization levels. Admin users have full authorization, including the ability to to register new users and edit existing users. Default users (as well as admin users) have the ability to view a list of the usernames of all users in the system. 

To accomplish this, our implementation uses LoginContext, LoginModule, Subject, callbacks, and principles from JAAS. 
- LoginContext: Coordinates the login process for all users. LoginContext is created with the database of users and callback handler and invokes the configured Login module. The Subject, representing an authenticated user, is returned after a successful login. 
- LoginModule: Performs user authentication. User credentials are requested through the configured callbacks then checked against the configured user database. On a successful login, the Subject is updated with user and role principals applicable to that user. 
- Subject: A representation of an authenticated user during a specific session. The Subject holds the applicable user and role principal. As app services are called, the app checks the subject's role before allowing any called service to run. 
- Callbacks: Allow the login module to request user information without directly having to take in the inputs. A username is requested using NameCallback, a password with PasswordCallback, and menu or role selections with ChoiceCallback. These callbacks were all implemented in the ConsoleCallbackHandler component. 
- Principals: Store defined user roles. 

# Design Overview
This project's JAAS implementation was done through these key components: 

- Main: Concrete configuration of JAAS login components. Handles service calls made by the user, and utilizes JAAS to check Subject roles before running any service actions when called. 
- UserLoginModule: Checks the user's entered credentials, and on a successful login adds user and role principals to the authenticated Subject of the current session. This Subject is then checked when authorizing service requests made by an authenticated user. 
- ConsoleCallBackHandler: Specifies the callbacks needed to collect user credentials (username, password). Also includes the callback needed for role selection when creating a new user, as well as selecting a service. 
- UnsafeUserDatabase: A prototype database that stores user account information in a largely unprotected local JSON file for a lightweight implementation. 

# Authentication Process


# Authorization Process


# Account and Role Management
