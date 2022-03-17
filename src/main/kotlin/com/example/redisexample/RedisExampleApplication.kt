package com.example.redisexample

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.*

@SpringBootApplication
@Configuration
class RedisExampleApplication {

    @Bean
    fun geography(template: ReactiveRedisTemplate<String, String>) = ApplicationRunner {
        runBlocking {
            val sicily = "Sicily"
            val geoTemplate = template.opsForGeo()
            val mapOfPoints = mapOf(
                "Arigento" to Point(13.361389, 38.11555556),
                "Catania" to Point(15.0876269, 37.502669),
                "Palermo" to Point(13.5833333, 37.316667),
            )
            mapOfPoints.entries.asFlow()
                .collect { geoTemplate.addAndAwait(sicily, it.value, it.key) }
            val circle = Circle(Point(13.583333, 37.31667), Distance(10.0, RedisGeoCommands.DistanceUnit.KILOMETERS))
            geoTemplate.radiusAsFlow(sicily, circle)
                .map { it.content.name }
                .collect(::println)
        }
    }

    @Bean
    fun list(template: ReactiveRedisTemplate<String, String>) = ApplicationRunner {
        runBlocking {
            val listTemplate = template.opsForList()
            val listName = "spring-team"
            listTemplate.leftPushAllAndAwait(listName, "Madhura", "Josh", "Stephane", "Dr. Syer", "Yuxin", "Olga", "Violetta")
            println(listTemplate.leftPopAndAwait(listName))
            println(listTemplate.leftPopAndAwait(listName))
        }
    }
}

fun main(args: Array<String>) {
    runApplication<RedisExampleApplication>(*args)
}
