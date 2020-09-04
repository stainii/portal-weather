Narrative:
The weather app should be able to execute flexbile forecast queries.
However, it should be economical on the usage of external services.
Also, if not all information can be retrieved, it should return what it could find (even if it is just an empty response).

To keep the tests short and simple, we assume a positive scenario. Every defined location service and forecast service
can find results for the defined queries, unless otherwise specified.

Scenario: I want to know the forecast for Zottegem on 2020-08-30 for the first time

Given I have a forecast service A which expects location type OpenWeatherMapCityId with order 1
And I have a forecast service B which expects location type LatitudeLongitude with order 2
And I have a location service 1 that provides location type OpenWeatherMapCityId
And I have a location service 2 that provides location type LatitudeLongitude
When I query the forecast for Zottegem between 2020-08-30T00:00:00 and 2020-08-31T00:00:00
Then I get forecast results:
 |location|date|source|
 |Zottegem|2020-08-30|A|
And forecast service B has not been tried
And location service 2 has not been tried


Scenario: I want to know the forecast for Zottegem on 2020-08-30 for the second time.
The previous time was 5 minutes ago, so my results should have been cached.

Given I have a forecast service A which expects location type OpenWeatherMapCityId with order 1
And I have a forecast service B which expects location type LatitudeLongitude with order 2
And I have a location service 1 that provides location type OpenWeatherMapCityId
And I have a location service 2 that provides location type LatitudeLongitude
And I have queried a forecast for Zottegem between 2020-08-30T00:00:00 and 2020-08-31T00:00:00  and I got a forecast result on 2020-08-30
When I query the forecast for Zottegem between 2020-08-30T00:00:00 and 2020-08-31T00:00:00
Then I get forecast results:
 |location|date|source|
 |Zottegem|2020-08-30|A (cached)|
And forecast service A has not been tried
And forecast service B has not been tried
And location service 1 has not been tried
And location service 2 has not been tried


Scenario: I want to know the forecast for Zottegem between 2020-08-30 and 2020-08-31.
I've queried for Zottegem on 2020-08-30 5 minutes ago, so that day should be cached while 2020-08-31 should be queried from a weather service.

Given I have a forecast service A which expects location type OpenWeatherMapCityId with order 1
And I have a forecast service B which expects location type LatitudeLongitude with order 2
And I have a location service 1 that provides location type OpenWeatherMapCityId
And I have a location service 2 that provides location type LatitudeLongitude
And I have queried a forecast for Zottegem between 2020-08-30T00:00:00 and 2020-08-31T00:00:00 and I got a forecast result on 2020-08-30
When I query the forecast for Zottegem between 2020-08-30T00:00:00 and 2020-09-01T00:00:00
Then I get forecast results:
 |location|date|source|
 |Zottegem|2020-08-30|A (cached)|
 |Zottegem|2020-08-31|A|
And forecast service B has not been tried
And location service 2 has not been tried


Scenario: I want to know the forecast for a misspelled place at 2020-08-30.
I'm not retrieving any results since the location services could not map the location

Given I have a forecast service A which expects location type OpenWeatherMapCityId with order 1
And I have a forecast service B which expects location type LatitudeLongitude with order 2
And I have a location service 1 that provides location type OpenWeatherMapCityId
And I have a location service 2 that provides location type LatitudeLongitude
And Gekkegem cannot be mapped by location service 1
And Gekkegem cannot be mapped by location service 2
When I query the forecast for Gekkegem between 2020-08-30T00:00:00 and 2020-08-31T00:00:00
Then I get no forecast results
And location service 1 has been tried for Gekkegem
And location service 2 has been tried for Gekkegem
And forecast service A has not been tried
And forecast service B has not been tried


Scenario: I want to know the forecast for Zottegem at 2021-08-30.
I'm not retrieving any results because I query too far in the future, no forecasts are available

Given I have a forecast service A which expects location type OpenWeatherMapCityId with order 1
And I have a forecast service B which expects location type LatitudeLongitude with order 2
And I have a location service 1 that provides location type OpenWeatherMapCityId
And I have a location service 2 that provides location type LatitudeLongitude
And no forecast for Zottegem at 2021-08-30 can be provided by forecast service A
And no forecast for Zottegem at 2021-08-30 can be provided by forecast service B
When I query the forecast for Zottegem between 2021-08-30T00:00:00 and 2021-08-31T00:00:00
Then I get no forecast results
And forecast service A has been tried for Zottegem between 2021-08-30T00:00:00 and 2021-08-31T00:00:00
And forecast service B has been tried for Zottegem between 2021-08-30T00:00:00 and 2021-08-31T00:00:00
And location service 1 has been tried for Zottegem
And location service 2 has been tried for Zottegem



Scenario: I want to know the forecast for Zottegem between 2020-08-30 and 2020-08-31.
Forecast service A has the results for 2020-08-30, but not for 2020-08-31.
Forecast service B has the results for both dates.
This means that for the 2020-08-30 the forecast result comes from A, while for 2020-08-31 the forecast result comes from B.

Given I have a forecast service A which expects location type OpenWeatherMapCityId with order 1
And I have a forecast service B which expects location type LatitudeLongitude with order 2
And I have a location service 1 that provides location type OpenWeatherMapCityId
And I have a location service 2 that provides location type LatitudeLongitude
And no forecast for Zottegem at 2020-08-31 can be provided by forecast service A
When I query the forecast for Zottegem between 2020-08-30T00:00:00 and 2020-09-01T00:00:00
Then I get forecast results:
 |location|date|source|
 |Zottegem|2020-08-30|A|
 |Zottegem|2020-08-31|B|


Scenario: I want to know the forecast for Zottegem between 2020-08-30 and 2020-09-01.
The forecast for Zottegem at 2020-08-31 is already cached, but for 2020-08-30 and 2020-09-01 not.
Forecast service A should only be queried for 2020-08-30 and 2020-09-01, but not for 2020-08-31.

Given I have a forecast service A which expects location type OpenWeatherMapCityId with order 1
And I have a forecast service B which expects location type LatitudeLongitude with order 2
And I have a location service 1 that provides location type OpenWeatherMapCityId
And I have a location service 2 that provides location type LatitudeLongitude
And no forecast for Zottegem at 2020-08-31 can be provided by forecast service A
When I query the forecast for Zottegem between 2020-08-30T00:00:00 and 2020-09-02T00:00:00
Then I get forecast results:
 |location|date|source|
 |Zottegem|2020-08-30|A|
 |Zottegem|2020-08-31|A (cached)|
 |Zottegem|2020-09-01|A|

Scenario: I want to know the forecast for Zottegem between 2020-08-30 and 2020-08-31.
I'm retrieving a result for 2020-08-30 but not for 2020-08-31, since the latter is too far away in the future.

Given I have a forecast service A which expects location type OpenWeatherMapCityId with order 1
And I have a forecast service B which expects location type LatitudeLongitude with order 2
And I have a location service 1 that provides location type OpenWeatherMapCityId
And I have a location service 2 that provides location type LatitudeLongitude
And no forecast for Zottegem at 2020-08-31 can be provided by forecast service A
And no forecast for Zottegem at 2020-08-31 can be provided by forecast service B
When I query the forecast for Zottegem between 2020-08-30T00:00:00 and 2020-09-01T00:00:00
Then I get forecast results:
 |location|date|source|
 |Zottegem|2020-08-30|A|
And forecast service A has been tried for Zottegem between 2020-08-30T00:00:00 and 2020-09-01T00:00:00
And forecast service B has been tried for Zottegem between 2020-08-31T00:00:00 and 2020-09-01T00:00:00
And location service 1 has been tried for Zottegem
And location service 2 has been tried for Zottegem


Scenario: I query for a start date time that is greater than the end date time. I get no results

Given I have a forecast service A which expects location type OpenWeatherMapCityId with order 1
And I have a location service 1 that provides location type OpenWeatherMapCityId
When I query the forecast for Zottegem between 2021-08-31T00:00:00 and 2021-08-30T00:00:00
Then I get no forecast results
And forecast service A has not been tried
And location service 1 has not been tried


Scenario: I query for a start date time that is equal to the end date time. I get a result for that day.

Given I have a forecast service A which expects location type OpenWeatherMapCityId with order 1
And I have a location service 1 that provides location type OpenWeatherMapCityId
When I query the forecast for Zottegem between 2020-08-30T00:00:00 and 2020-08-30T00:00:00
Then I get forecast results:
 |location|date|source|
 |Zottegem|2020-08-30|A|