# Portal Weather
A REST service providing weather functionality. At this moment, it has one endpoint to retrieve forecasts.

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


## Internal working
The app is built around these concepts:
##### Data
* Forecast
* Location
##### Logic
* WeatherFacade
* WeatherService
* LocationService

### Forecast
The forecast of **one specific day** in **one specific location**.

### Location
A representation of the location. Can be a class with one property called id, could be a class with latitude and longitude properties, ...

### WeatherFacade
Queries its services, one by one, until all required information is found.

1. A user makes a request to get the weather report for Zottegem for the coming 5 days.
2. The WeatherFacade checks in its cache if it has the forecast for any of the days for Zottegem.

3. If certain forecasts are still missing, it asks the first WeatherService to provide the forecasts for these days.

    3.1. To do that, it first needs to ask the WeatherService in which format it wants the location. The WeatherFacade checks whether it has that location in that format in its cache.
         If not, it asks every LocationService if they can provide that format. The first LocationService which can provide the format, is asked to do it.
         
    3.2. Finally, the WeatherService can be called.
    
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
        locationType: OpenWeatherAPILocationId,
        value: "java serialization for OpenWeatherAPILocationId class that contains the value 2783176"
    }, {
        location: Zottegem,
        locationType: LatitudeLongitude,
        value: "java serialization for LatitudeLongitude class that contains the value latitude and longitude"
    }
]
```

Forecasts are kept in cache for an hour.


### WeatherService
A WeatherService queries a specific external weather API. For example, you could have an OpenWeatherAPIService, a ClimaCellService, ...
It is responsible for the communication with the specific API and translation of our generic request/response to the API specific request/response.

A WeatherService does not have to worry about the format of the location, however. It specifies the format it needs its location to be in, and the WeatherFacade will pass through the location in that correct format.

```
{
    Collection<L> supportedLocationFormats()
    Collection<Forecast> query(L location, LocalDateTime startDateTime, LocalDateTime endDateTime)
}
``` 


A WeatherService is expected to manage its own rate limiting and return an empty list when too many calls are made.


### LocationService
A LocationService translated the user's location description (plain text) into a specific format.

It could look in a downloaded list what a WeatherService's internal location id is.
Or it could call a geocode API to get a latitude and longitude.

A LocationService is expected to manage its own rate limiting and return an empty Optional when too many calls are made.
