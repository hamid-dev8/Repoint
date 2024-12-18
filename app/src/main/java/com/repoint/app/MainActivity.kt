package com.repoint.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.repoint.account.ui.AuthScreen
import com.repoint.dependencies.theme.RepointTheme
import com.repoint.app.ui.MainScreen
import com.repoint.app.ui.SplashScreen
import com.repoint.basics.logic.ScreenActions
import dagger.hilt.android.AndroidEntryPoint
import org.web3j.crypto.MnemonicUtils
import org.web3j.utils.Numeric
import java.security.SecureRandom


@AndroidEntryPoint
class MainActivity : ComponentActivity()  , ScreenActions {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            RepointTheme {
                SplashScreen {
                    AuthScreen(this)
                }
            }
        }


        //32 char hex!
        //val entropy :String = "2c0b4e6aaa47308803089bba616c9ec6"
        val entropy = hexTo16ByteArray("8bc458c2a94b1029f06b5186e6bce4de")
        val ent = ByteArray(16)
        Log.d("bitcoin", "ent is : $entropy")
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(ent)
        Log.d("bitcoin", "secure ent is : $entropy")

        val nmeonic = MnemonicUtils.generateMnemonic(entropy)

        val seed = MnemonicUtils.generateSeed(nmeonic, "")

        Log.d("bitcoin", nmeonic)


    }


    fun hexTo16ByteArray(hex: String): ByteArray {
        // Remove "0x" prefix if present
        val cleanHex = Numeric.cleanHexPrefix(hex)

        // Convert the hex string to a byte array
        val byteArray = Numeric.hexStringToByteArray(cleanHex)

        // Ensure the resulting array is exactly 16 bytes
        if (byteArray.size != 16) {
            throw IllegalArgumentException("Input hex must represent exactly 16 bytes (32 hex characters).")
        }

        return byteArray
    }

    override fun onButtonClick() {
        Toast.makeText(this,"button Clicked" , Toast.LENGTH_SHORT).show()
     }

    override fun onItemSelected(itemId: Int) {
        //TODO("Not yet implemented")
    }

    override fun onTabSelected(index: Int, title: String) {
        //TODO("Not yet implemented")
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RepointTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Android",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}