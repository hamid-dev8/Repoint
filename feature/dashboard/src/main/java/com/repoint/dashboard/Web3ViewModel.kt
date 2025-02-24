package com.repoint.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.repoint.basics.logic.TokenERC20
import com.repoint.network.datasource.Web3DataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.contracts.token.ERC20Interface
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject


@HiltViewModel
class Web3ViewModel @Inject constructor(
    private val repository : Web3DataSource
) : ViewModel()
{
    private val _balanceWei = MutableLiveData<BigInteger>()
    val balanceWei : LiveData<BigInteger> get() = _balanceWei

    //optionally expose the balance in ether
    private val _balanceEther = MutableLiveData<BigDecimal>()
    val balanceEther : LiveData<BigDecimal> get() = _balanceEther

    suspend fun fetchNativeWalletBalance(walletAddress : String) : BigDecimal?{

            return withContext(Dispatchers.IO) {
                try {
                    val weiBalance = repository.getWalletBalance(walletAddress)
                    // Convert wei to ether and return it
                    Convert.fromWei(weiBalance.toString(), Convert.Unit.ETHER)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    BigDecimal.ZERO  // or throw the exception further
                }
            }
        }

    suspend fun sendTokenOnChain(receiverAddress : String,amount : String) :  String{


    return "felan"
    }

}