package com.repoint.basics.logic

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


fun formatDate(inputDate : String) : String{
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val parseDate = ZonedDateTime.parse(inputDate , formatter.withZone(ZoneId.of("UTC")))

    val outputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    return parseDate.format(outputFormatter)
}

