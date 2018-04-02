# amstel

Generic event processing pipeline POC.

## API
#### Posting an event
Endpoint
```
POST​ ​/events/group/[GroupId]/device/[Deviceid]
```
Data
```json
{
  ​​​​​​​​​​​​​"value":​ ​12.3,
  ​"timestamp":​ ​1478192204000
}
```
Returns `201` in​ ​case​ ​of​ ​success  

Example:
```bash
http :8080/events/group/4/device/3 timestamp:=1522517129483 value:=45677867             
HTTP/1.1 201 Created
Content-Length: 76
Content-Type: text/plain; charset=UTF-8
Date: Mon, 02 Apr 2018 19:20:55 GMT
Server: akka-http/10.1.0
```

#### Retrieving statistics

##### By group and device ids
User credentials required
Endpoint
```
GET​ ​/statistics/group/[GroupId]/device/[DeviceIds]?from=[FromTimestamp]&to=[ToTimestamp]
```
Returns
```json
{
  "sum":​ ​1000,
  "avg":​ ​100,
  "max":​ ​200,​
  "min":​ ​50,
  "count":​ ​10
}
```
Example:  

With test user credentials (supplied)
```bash
http GET :8080/statistics/group/4/device/3?from=1522517129483\&to=1522517129483 -a someuser:somepassword 
HTTP/1.1 200 OK
Content-Length: 71
Content-Type: application/json
Date: Mon, 02 Apr 2018 19:22:02 GMT
Server: akka-http/10.1.0

{
    "avg": 45677867,
    "count": 1,
    "max": 45677867,
    "min": 45677867,
    "sum": 45677867
}
```
##### By group only
User credentials required  

Endpoint
```
GET​ ​/statistics/group/[GroupId]?from=[FromTimestamp]&to=[ToTimestamp]
```
Returns
```json
{
  "sum":​ ​1000,
  "avg":​ ​100,
  "max":​ ​200,​
  "min":​ ​50,
  "count":​ ​10
}
```
Example:
With test user credentials (supplied)
```bash
http GET :8080/statistics/group/4?from=1522517129483\&to=1522517129483 -a someuser:somepassword 
HTTP/1.1 200 OK
Content-Length: 71
Content-Type: application/json
Date: Mon, 02 Apr 2018 19:22:02 GMT
Server: akka-http/10.1.0

{
    "avg": 45677867,
    "count": 1,
    "max": 45677867,
    "min": 45677867,
    "sum": 45677867
}
```
## Setup and Requirements

To build the project, [SBT](https://github.com/sbt/sbt) should be used. You could inslall it with [Homebrew](https://github.com/Homebrew/brew) (if running MacOS):
```bash
brew install sbt
```
Or use `apt-get` (for Debian-based systems)
```bash
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt-get update
sudo apt-get install sbt
```

### Building and running the project
##### To run the app (one main class so you won't need to specify any)
```bash
sbt 'api/run'
```

##### To package a jar
```bash
sbt assembly
```
You'll find a jar in `<project_dir>/target/api`

##### To run the tests:
```bash
sbt testOnly
```

### Simulation load
To run local load simulation
```bash
sbt generateLoad
```
