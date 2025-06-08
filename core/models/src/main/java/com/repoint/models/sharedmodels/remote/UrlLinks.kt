package com.repoint.models.sharedmodels.remote

import com.google.gson.annotations.SerializedName

data class UrlLinks(
    val website: List<String>?,
    val twitter: List<String>?,
    @SerializedName("message_board")val messageBoard: List<String>?,
    val chat: List<String>?,
    val facebook: List<String>?,
    val explorer: List<String>?,
    val reddit: List<String>?,
    @SerializedName("technical_doc")val technicalDoc: List<String>?,
    @SerializedName("source_code")val sourceCode: List<String>?,
    val announcement: List<String>?
)
