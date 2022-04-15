package com.example.webexandroid

import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.webexandroid.utils.PermissionsHelper
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

open class BaseActivity : AppCompatActivity() {
    var tag = "BaseActivity"
    private val permissionsHelper: PermissionsHelper by inject()
    val webexViewModel: WebexViewModel by viewModel()

    fun showErrorDialog(errorMessage: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.error_occurred)
        val message = TextView(this)
        message.setPadding(10, 10, 10, 10)
        message.text = errorMessage

        builder.setView(message)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}