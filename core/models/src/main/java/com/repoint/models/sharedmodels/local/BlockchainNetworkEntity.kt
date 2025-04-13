package com.repoint.models.sharedmodels.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


//the purpose for this is to link "id" in this database  to Foriegn key that is in the BlockChainNetwork Model
@Entity(tableName = "networks")
data class BlockchainNetworkEntity(
   @PrimaryKey val id : Int,
   val name: String,
   val chainId: Int,
   val coinType : Int,
   val rpcUrl: String,
   val explorerUrl: String,
   val nativeToken: String,
   val dexRouter: String)