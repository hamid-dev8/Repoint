package com.repoint.models.sharedmodels.remote

data class PendingProposal(
    val name: String,
    val url: String,
    val chains: List<String>,
    val methods: List<String>,
    val events: List<String>
)
