package com.song.middleware.server

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
open class GeneralApplication

fun main(args: Array<String>) {
    SpringApplication.run(GeneralApplication::class.java, *args)
}
