package com.example.redisexample

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
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.leftPopAndAwait
import org.springframework.data.redis.core.leftPushAllAndAwait
import reactor.core.publisher.Flux

@SpringBootApplication
@Configuration
class RedisExampleApplication {

    @Bean
    fun geography(template: ReactiveRedisTemplate<String, String>) = ApplicationRunner {
        val sicily = "Sicily"
        val geoTemplate = template.opsForGeo()
        val mapOfPoints = mapOf(
            "Arigento" to Point(13.361389, 38.11555556),
            "Catania" to Point(15.0876269, 37.502669),
            "Palermo" to Point(13.5833333, 37.316667),
        )
        Flux.fromIterable(mapOfPoints.entries)
            .flatMap { geoTemplate.add(sicily, it.value, it.key) }
            .thenMany(geoTemplate.radius(sicily, Circle(Point(13.583333, 37.31667), Distance(10.0, RedisGeoCommands.DistanceUnit.KILOMETERS))))
            .map { it.content.name }
            .doOnNext(::println)
            .blockLast()
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
