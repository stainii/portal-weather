# Portal Weather
A REST service providing weather functionality. At this moment, it has one endpoint to retrieve forecasts.

## Supported external weather APIs
* OpenWeatherMap

## Contract
### /forecasts

#### Request
* Method: POST 
* Body: 
```
{
    requests: [
        {
                location: text,
                startDateTime: datetime,
                endDateTime: datetime
        }, ...
    ]
}
```


#### Response
TODO

## Configuration
### External APIs
The usage of external weather APIs can be configured with Spring Boot properties:

```
be.stijnhooft.portal.weather.service.name-of-the-service.enabled = true
be.stijnhooft.portal.weather.service.name-of-the-service.order = 1
```

##### Enabled
When not providing these properties for a certain service, it will assume it is enabled.
 
##### Order
APIs are called in a certain order, just until all requests have been fulfilled. In a perfect world, only the API with order = 1 is called, because it can serve all calls; the other API's shouldn't be bothered.

When not providing an order for a certain service, it will assume an order of 1.

When multiple services have the same order number, the order of these specific services cannot be guaranteed.

#### Example
An example for OpenWeatherMap:
```
be.stijnhooft.portal.weather.service.OpenWeatherMap.enabled = true
be.stijnhooft.portal.weather.service.OpenWeatherMap.order = 1
```

### Cache
```
be.stijnhooft.portal.weather.cache.path=/Users/stijnhooft/app/portal/weather/
be.stijnhooft.portal.weather.cache.forecasts.time-to-live-in-hours=1
be.stijnhooft.portal.weather.cache.forecasts.max-no-of-entries=1000
be.stijnhooft.portal.weather.cache.locations.max-no-of-entries=1000
```

#### Path
When provided, the cache will be saved in that directory. 

When not provided, the cache is kept in-memory only.

#### time-to-live-in-hours
How many hours is a retrieved forecast seen as up to date? Default is 1.

Locations are always kept indefinitely, or until the max number of entries have been surpassed.

#### max-no-of-entries
How many entries can the cache keep? Default is 1000.

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
A representation of the location. Can be a class with one property called id, could be a class with latitude and longitude properties, ...

### WeatherFacade
Queries its services, one by one, until all required information has been found.

An example for a forecast:

1. A user makes a request to get the weather report for Zottegem for the coming 5 days.
2. The WeatherFacade checks in its cache if it has the forecast for any of the days for Zottegem.

3. If certain forecasts are still missing, it asks the first ForecastService to provide the forecasts for these days.

    3.1. To do that, it first needs to ask the ForecastService in which format it wants the location. The WeatherFacade checks whether it has that location in that format in its cache.
         If not, it asks every LocationService if they can provide that format. The first LocationService which can provide the format, is asked to do it.
         
    3.2. Finally, the ForecastService can be called.
    
   This process is repeated until forecasts for every day has been found.
4. A result is returned containing the found Forecasts.

The WeatherFacade is expected to always producing an answer, even if parts of (or no) information could be gathered. Yes, worst case you get an empty answer.

#### Cache
Also, it manages the caches for locations and forecasts.

Locations are kept in a cache that never expires. The key is location + location type. For example:
```
[
    {
        location: Zottegem,
        locationType: OpenWeatherMapCityId,
        value: "java serialization for OpenWeatherMapCityId class that contains the value 2783176"
    }, {
        location: Zottegem,
        locationType: LatitudeLongitude,
        value: "java serialization for LatitudeLongitude class that contains the value latitude and longitude"
    }
]
```

Forecasts are kept in cache for an hour.


### ForecastService
A ForecastService queries a specific external weather API. For example, you could have an OpenWeatherMapService, a ClimaCellService, ...
It is responsible for the communication with the specific API and translation of our generic request/response to the API specific request/response.

A ForecastService does not have to worry about the format of the location, however. It specifies the format it needs its location to be in, and the WeatherFacade will pass through the location in that correct format.

```java
public interface ForecastService<L extends Location> {

    Class<L> supportedLocationType();
    Collection<Forecast> query(Location location, Collection<Interval> intervals);
    ...

}
``` 


A ForecastService is expected to manage its own rate limiting and return an empty list when too many calls are made.


### LocationService
A LocationService translated the user's location description (plain text) into a specific format.

It could look in a downloaded list what an external API's internal location id is.
Or it could call a geocode API to get a latitude and longitude.

```java
public interface LocationService<L extends Location> {

    boolean canProvide(Class<? extends Location> locationType);
    Optional<Location> map(String location, Class<? extends Location> locationType);
    ...

}
```


A LocationService is expected to manage its own rate limiting and return an empty Optional when too many calls are made.
