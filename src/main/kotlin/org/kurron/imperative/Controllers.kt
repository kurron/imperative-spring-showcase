package org.kurron.imperative

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Stuff( val zulu: String, val hotel: String, val alpha: String )

@RestController
@RequestMapping("/echo")
class Echo {

    @GetMapping("/hello", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hello() = mapOf( "operation" to randomHexString(), "code" to randomHexString(), "should not be sent" to "" )

    @GetMapping("/sorted", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sorted() = Stuff( randomHexString(), randomHexString(), randomHexString() )
}