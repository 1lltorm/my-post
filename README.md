# hubr

hubr is simple a web application with the ability to create and moderate posts and much more

* [Technologies](#technologies)
* [Features](#features)
* [Quick start](#quick-start)
* [ER Model](#hubr-er-model)

### Technologies
Spring (Boot, Security, JPA, MVC), Lombok, Maven, Docker, Hibernate, PostgreSQL, JUnit, Mockito, Thymeleaf, Bootstrap, HTML, CSS
 
### Features
  * Register a new account with email confirmation and reCAPTCHA    <sup> (for this you need to follow this [instruction](#registration-settings)) </sup>
  * User
     * Subscribe to tags and users
     * Сomment on posts
     * Get notifications from subscribed users
     * Upload profile
     * Create new posts and tags
  * Admin
    * Moderate new posts and tags
    * Ban users and grant them rights
    * Delete user comments
  * And much more


### Quick start
1. Clone this repo into folder.

```Bash
git clone https://github.com/qReolq/hubr.git
cd hubr
```
2. Start docker compose

```Bash
docker compose up
```
3. Go to http://localhost:8080/auth/login

4. You can [register](#registration-settings) or login as an existing user ~ login:timur password:1q2w

5. Stop docker
```
docker compose down
```

### Registration settings
1. To register you need to provide your google email address and google apps password to application.properties file

```properties
spring.mail.username=your_gmail_addres
spring.mail.password=your_application_passwords
```
2. Package the project and run docker
```Bash
./mvnw package -DskipTests
docker compose up --build
```
### hubr ER Model

![image](https://user-images.githubusercontent.com/115367574/232320459-08dbfa2b-46a3-450e-ab6b-c8265f5404c0.png)
