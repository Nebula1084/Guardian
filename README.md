# Deployment
## Build
The guardian utilize `Gradle` as build tool and manage the multi-module project. 
Use this commands to build and clean the project.
~~~
# build the project
./gradlew build

# clean the project
./gradlew clean
~~~
There are two artifacts, client and server. They will be output to following directories. 
~~~
# Android apk
Guardian/app/build/outputs/apk/app-*.apk

# Server
Guardian/server/build/libs/guardian-server-1.0.0.jar
~~~

## Database
Guardian use `postgres:10` from docker hub as data source. You could use this command
to fetch the docker image.
~~~
docker pull postgres:10
~~~
The DDL is stored in this file. Execute the SQL under schema `guardian` .
~~~
Guardian/server/data.ddl
~~~
Here is the database configuration file. You can modify the `port` the server
will bind. And change the `username` and `password` to your personal setting.
~~~
/Guardian/server/src/main/resources/application.yml
~~~
The default profile is `dev`, thus you should modify the database url under
`dev` profile to your own database. 

## Run Server
Under directory `Guardian` execute the following command. This is a standalone
server. We could deploy it by just run it.
~~~
java -jar server/build/libs/guardian-server-1.0.0.jar 
~~~

## Setup Client
Since we do not have a uniform domain name, we need to adjust we hard-coded 
IP address every time when we re-deploy the server. The IP address is coded
in this file.
~~~
Guardian/app/src/main/java/hku/cs/smp/guardian/MainActivity.java
~~~
It's a static variable, you also need to configure it.
~~~
public final static String HOST = "175.159.181.165";
~~~
Although, the application would work just fine without server support. But
it's necessary for some feature like uploading tag and inquiring the phone
number.

# Usage
This section describe the usage of `Guardian` app.

# Architecture
The project is composed of three modules, `client`, `common` and `server`.
* `client` is responsible for interacting with user and visualizing the data.
* `server` collects the data from the client and persist them into database.
* `common` define a `TCP` protocol between Client and Server allowing more performance
than `HTTP` protocol.

## Client 
The client is the most complex module in this project. from perspective of
functionality, we have three major demand, blocking, config and tag. User
need a screen to browse all incoming calls to tag them as malicious calls. 
And another screen is necessary to configure the metadata of block and alert.
We need to upload tag information to server and download them according to
phone number to alert user or block incoming phone.

Guardian use one `MainActivity` to manage two functional components `BlockFragment`
and `ConfigFragment`. 

* `BlockFragment` visualizes all incoming calls as a `ListView`. It retrieve data
from database and content-provider asynchronously. And it insert the tag into local
database.
* `ConfigFragment` is a `PreferenceFragment`, it would maintain shared preference 
in local flash. 

`TagDatabase` persist all tag information the user generate. It utilize a ORM framework
`Room`. 

We use `PhoneBlocker` to intercept all incoming calls. It's a `BrocastReciver` actually.
The `PhoneBlocker` will read the data from database, preference and network, then
decided whether alert, block or just let it go.

An `UploadService` is running background. It would read the data from `TagDatabase` and upload
them to the server. 

`ContactsHelper` provide a interface to retrieve data from contacts information.

`AlertActivity` will popup when we need to alert the user. It use `Volley`'s `NetworkImageView`
to download the image. And poll data from `sever` to render a pie chart using `HelloCharts`.

## Common
This module adopt `Netty` framework to implement `NIO` server and client. All data flow
with this module is wrapped with `RxJava`.

Protocol is pretty simple, only two operation is needed.
* Tag: `TagRequest` and `TagResponse`. This protocol upload tag information to server.
* Inquiry: `InquiryRequest` and `InquiryResponse`. Thi protocol send a phone number to server
and retrieve all tag information of this number.

* `MessageDecoder` would convert the binary stream into object.
* `MessageHandler` push this message to the processor flow. 
* `MessageEncoder` encode the response into binary stream.

## Server
`server` is most simple module. It utilize `Spring` framework to assemble components.

`CallService` implement two simple method.
* `tag` would invoke the stored function defined in database, this stored function would
guarantee the atomicity of tag transaction in concurrent scenario. 
* `Inquiry` is just a select, it utilize `JDBCTemplate` to de-serialize data.
