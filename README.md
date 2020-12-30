# Portal Weather
[![Build Status](https://server.stijnhooft.be/jenkins/buildStatus/icon?job=portal-weather/master)](https://server.stijnhooft.be/jenkins/job/portal-weather/job/master/)

A REST service providing weather functionality. At this moment, it has one endpoint to retrieve forecasts.

## Supported external weather APIs
* OpenWeatherMap

## Dependencies
This service is dependent on a deployment of **portal-location**.

## Docker environment variables
| Name | Example value | Description | Required? |
| ---- | ------------- | ----------- | -------- |
| JAVA_OPTS_WEATHER | -Xmx400m -Xms400m | Java opts you want to pass to the JVM | optional
| CACHE_PATH | /opt/cache/ | Path of the cache in the Docker container. Map this to your volume. | optional
| CACHE_FORECASTS_HOURS_CONSIDERED_UP_TO_DATE | 1 | [See property documentation](#Hours-considered-up-to-date) | optional
| CACHE_FORECASTS_MAX_MB | 100 | [See property documentation](#Max-mb) | optional
| CACHE_FORECASTS_MAX_NO_OF_ENTRIES | 1000 | [See property documentation](#Max-no-of-entries) | optional
| CACHE_LOCATIONS_MAX_MB | 100 | [See property documentation](#Max-mb) | optional
| CACHE_LOCATIONS_MAX_NO_OF_ENTRIES | 1000 | [See property documentation](#Max-no-of-entries) | optional
| OPEN_WEATHER_MAP_ENABLED | true | Should OpenWeatherMap be used? Default: true | optional
| OPEN_WEATHER_MAP_ORDER | 1 | Order of usage of OpenWeatherMap in comparison with other services. See [property documentation](#order). Default: 1 | optional
| OPEN_WEATHER_MAP_API_KEY | secret | API key for OpenWeatherMap | required when OpenWeatherMap is enabled


## Contract
### /forecasts

* Method: POST 
* Example body: 
```json
{
    "forecastRequests": [{
        "location": "Zottegem",
        "startDateTime": "2020-09-09T00:00:00",
        "endDateTime": "2020-09-09T23:59:59"
    }]
}
```

* Example response:
```json
{
    "forecasts": [
        {
            "location": "Zottegem",
            "date": "2020-09-09",
            "source": "be.stijnhooft.portal.weather.forecasts.services.impl.OpenWeatherMapForecastService (cached)",
            "createdAt": "2020-09-09T17:46:34.615051",
            "temperature": {
                "minTemperature": 16.74,
                "maxTemperature": 22.41,
                "feelsLike": 21.2
            },
            "cloudiness": 99,
            "precipitation": {
                "intensity": 0,
                "probability": 0.0,
                "type": "NOTHING"
            },
            "wind": {
                "beaufort": 3,
                "direction": "WEST"
            }
        }
    ]
}
```

## Configuration
Since the plugin-like nature of this application, we have 2 types of configuration:
* General framework configuration
* External API configuration

### General framework
```
be.stijnhooft.portal.weather.cache.path = /Users/stijnhooft/app/portal/weather/
be.stijnhooft.portal.weather.cache.forecasts.hours-considered-up-to-date = 1
be.stijnhooft.portal.weather.cache.forecasts.max-mb = 100
be.stijnhooft.portal.weather.cache.forecasts.max-no-of-entries = 1000
be.stijnhooft.portal.weather.cache.locations.max-mb = 100
be.stijnhooft.portal.weather.cache.locations.max-no-of-entries = 1000
```

#### Path
When provided, the cache will be saved in that directory. 

When not provided, the cache is kept in-memory only.

#### Hours-considered-up-to-date
How many hours is a retrieved forecast seen as up to date? Default is 1.

#### Max-mb
How many megabytes may the cache take on the disk?

#### Max-no-of-entries
How many entries can the cache keep? Default is 1000.


### OpenWeatherMap
```
be.stijnhooft.portal.weather.service.OpenWeatherMap.enabled = true
be.stijnhooft.portal.weather.service.OpenWeatherMap.order = 1
be.stijnhooft.portal.weather.service.OpenWeatherMap.api-key = your-api-key
```

#### Enabled
When not providing these properties for a certain service, it will assume it is enabled.
 
#### Order
APIs are called in a certain order, just until all requests have been fulfilled. In a perfect world, only the API with order = 1 is called, because it can serve all calls; the other API's shouldn't be bothered.
When not providing an order for a certain service, it will assume an order of 1.
When multiple services have the same order number, the order of these specific services cannot be guaranteed.


#### API key
If you don't disable the OpenWeatherMap forecast service, 
it is required to provide an API key.

When running in Docker, you can provide this as an environment variable "OPEN_WEATHER_MAP_API_KEY".


## Internal working
The app is built around these concepts:
##### Data
* Forecast
* Location
##### Logic
* WeatherFacade
* ForecastService
* LocationService

### Forecast
The forecast of **one specific day** in **one specific location**.

### Location
Latitude and longitude of a location.

### WeatherFacade
Queries its services, one by one, until all required information has been found.
The goal is to call as less external APIs as possible while still providing up to date forecast results.

The WeatherFacade is expected to always producing an answer, even if parts of (or no) information could be gathered. Worst case you get an empty answer.

#### Cache
The application always caches its results. The cache has 2 purposes:
* When you execute the same query [within the hour](#hours-considered-up-to-date), the application will not call any external API.
* When you execute the same query [more than an hour later](#hours-considered-up-to-date), the application will try to fetch more up-to-date forecasts. But if none of the external APIs can provide a forecast, the cached results wil be used as a fallback. 


### ForecastService
A ForecastService queries a specific external weather API. For example, you could have an OpenWeatherMapService, a ClimaCellService, ...
It is responsible for the communication with the specific API and translation of our generic request/response to the API specific request/response.

A ForecastService is expected to manage its own rate limiting, exception handling, ...


### LocationService
A LocationService translated the user's location description (plain text) into coordinates.
How these coordinates are retrieved, is abstracted away behind the portal-location project. 


## Release
### How to release
To release a module, this project makes use of the JGitflow plugin and the Dockerfile-maven-plugin.

1. Make sure all changes have been committed and pushed to Github.
1. Switch to the dev branch.
1. Make sure that the dev branch has at least all commits that were made to the master branch
1. Make sure that your Maven has been set up correctly (see below)
1. Run `mvn jgitflow:release-start -Pproduction`.
1. Run `mvn jgitflow:release-finish -Pproduction`.
1. In Github, mark the release as latest release.
1. Congratulations, you have released both a Maven and a Docker build!

More information about the JGitflow plugin can be found [here](https://gist.github.com/lemiorhan/97b4f827c08aed58a9d8).

#### Maven configuration
At the moment, releases are made on a local machine. No Jenkins job has been made (yet).
Therefore, make sure you have the following config in your Maven `settings.xml`;

````$xml
<servers>
    <server>
        <id>docker.io</id>
        <username>your_username</username>
        <password>*************</password>
    </server>
    <server>
        <id>portal-nexus-releases</id>
        <username>your_username</username>
        <password>*************</password>
    </server>
</servers>
````
* docker.io points to the Docker Hub.
* portal-nexus-releases points to my personal Nexus (see `<distributionManagement>` in the project's `pom.xml`)
