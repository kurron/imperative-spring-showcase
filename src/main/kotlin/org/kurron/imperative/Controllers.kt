package org.kurron.imperative

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/echo")
class Echo {

    @GetMapping("/hello", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hello(): Map<String,String> {
        return mapOf( "code" to randomHexString(), "operation" to randomHexString(), "trace-id" to randomHexString() )
    }
}