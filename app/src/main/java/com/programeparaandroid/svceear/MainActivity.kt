package com.programeparaandroid.svceear

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.programeparaandroid.svceear.databinding.ActivityMainBinding
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {


    private fun enablePersistence() {
        // [START rtdb_enable_persistence]
        Firebase.database.setPersistenceEnabled(true)
        // [END rtdb_enable_persistence]
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var total_convidados  : Integer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {

            R.id.sair ->{

                val mBuilder = AlertDialog.Builder(this)
                    .setTitle("Atenção!")
                    .setMessage("Tem certeza que deseja sair?")
                    .setPositiveButton("Sim", null)
                    .setNegativeButton("Não", null)
                    .show()

                    val mPositiveButton = mBuilder.getButton(AlertDialog.BUTTON_POSITIVE)

                    mPositiveButton.setOnClickListener {
                        exitProcess(0)
                    }
                return true
            }
            R.id.relatorio ->{
                val inflater = this.layoutInflater
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                var editText = EditText(this)
                editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

                val mBuilder = AlertDialog.Builder(this)
                    .setTitle("Digite sua senha")
                    .setView(inflater.inflate(R.layout.dialog_signin, null))
                    .setView(editText)
                    .setPositiveButton("Confirmar", null)
                    .setNegativeButton("Cancelar", null)
                    .show()

                val mPositiveButton = mBuilder.getButton(AlertDialog.BUTTON_POSITIVE)

                mPositiveButton.setOnClickListener {

                    if(editText.text.toString().equals("teste")){
                        mBuilder.dismiss()
                        navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
                    } else {
                        mBuilder.dismiss()
                        Toast.makeText(this, "Senha incorreta.", Toast.LENGTH_LONG).show()

                    }
                }
                return true
            }
                else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

}