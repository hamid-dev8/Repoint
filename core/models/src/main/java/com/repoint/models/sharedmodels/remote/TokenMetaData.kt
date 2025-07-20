package com.repoint.models.sharedmodels.remote

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenMetaData(
    val id: Int,
    val name: String,
    val symbol: String,
    val category : String,
    val slug: String,
    val logo : String?,
    val description: String?,
    val notice: String?,
    val tags: List<String>,
    @SerializedName("tag-names") val tagNames: List<String>,
    @SerializedName("tag-groups") val tagGroups: List<String>,
    val urls : UrlLinks?,
    val platform : CmcPlatforms?, //nullable or custom class if not always null
    @SerializedName("date_added") val dateAdded : String,
    @SerializedName("twitter_username")val twitterUsername : String,
    @SerializedName("is_hidden")val isHidden : Int,
    @SerializedName("date_launched")val dateLaunched : String?,
    @SerializedName("contract_address")val contractAddress : List<ContractAddress>,
    @SerializedName("self_reported_circulating_supply")val selfReportedCirculatingSupply : Double?,
    @SerializedName("self_reported_tags")val selfReportedTags : List<String>?,
    @SerializedName("self_reported_market_cap")val selfReportedMarketCap : Double?,
    @SerializedName("infinite_supply")val infiniteSupply : Boolean
    ) : Parcelable
