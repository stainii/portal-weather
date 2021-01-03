FROM adoptopenjdk:11-jre-hotspot as builder
WORKDIR application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM adoptopenjdk:11-jre-hotspot
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT exec java ${JAVA_OPTS_WEATHER} org.springframework.boot.loader.JarLauncher \
 --be.stijnhooft.portal.weather.cache.path=${CACHE_PATH} \
 --be.stijnhooft.portal.weather.cache.forecasts.hours-considered-up-to-date=${CACHE_FORECASTS_HOURS_CONSIDERED_UP_TO_DATE} \
 --be.stijnhooft.portal.weather.cache.forecasts.max-mb=${CACHE_FORECASTS_MAX_MB} \
 --be.stijnhooft.portal.weather.cache.forecasts.max-no-of-entries=${CACHE_FORECASTS_MAX_NO_OF_ENTRIES} \
 --be.stijnhooft.portal.weather.cache.locations.max-mb=${CACHE_LOCATIONS_MAX_MB} \
 --be.stijnhooft.portal.weather.cache.locations.max-no-of-entries=${CACHE_LOCATIONS_MAX_NO_OF_ENTRIES} \
 --be.stijnhooft.portal.weather.service.OpenWeatherMap.enabled=${OPEN_WEATHER_MAP_ENABLED} \
 --be.stijnhooft.portal.weather.service.OpenWeatherMap.order=${OPEN_WEATHER_MAP_ORDER} \
 --be.stijnhooft.portal.weather.service.OpenWeatherMap.api-key=${OPEN_WEATHER_MAP_API_KEY} \
 --eureka.client.service-url.defaultZone=${EUREKA_SERVICE_URL}