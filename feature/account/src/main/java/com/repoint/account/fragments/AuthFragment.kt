package com.repoint.account.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.repoint.account.ui.AuthScreen
import com.repoint.basics.logic.ScreenActions
import com.repoint.database.dao.AuthDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AuthFragment : Fragment(), ScreenActions {

    @Inject
    lateinit var authDao: AuthDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
               // AuthScreen(this@AuthFragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onButtonClick() {
        TODO("Not yet implemented")
    }

    override fun onItemSelected(itemId: Int) {
        TODO("Not yet implemented")
    }

    override fun onTabSelected(index: Int, title: String) {
        TODO("Not yet implemented")
    }
}