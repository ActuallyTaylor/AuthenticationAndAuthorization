# Authentication & Authorization Activity

## Run Commands
```shell
mvn clean package
```

```shell
java -jar target/AuthenticationAndAuthorization-1.0-SNAPSHOT.jar
```

On first run, create the initial admin account, then log in. Choose a service from
the console menu: `/users` is available to DEFAULT and ADMIN users, while
`/register` and `/edit-user` require ADMIN. In `/edit-user`, leave the new
username or password blank to keep that value unchanged. Choose `/exit` to quit.