package com.programeparaandroid.svceear

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.CalendarContract
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.collection.LLRBNode
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.programeparaandroid.svceear.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {
    var backPressedTime: Long = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var navController : NavController
    private val db = Firebase.firestore
    private lateinit var sharedPref : SharedPreferences
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        auth = Firebase.auth
        sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

        /*
        if(!sharedPref.contains("primeiro_uso")){
            sincronismoGeral(sharedPref)
        }

        registraDispositivo()



         */
    }

    fun registraDispositivo() {
        val numero = hashMapOf(
            "data_hora" to Timestamp.now()
        )

        lateinit var uniqueID: String

        if (!sharedPref.contains("UUID")) {
            uniqueID = UUID.randomUUID().toString()
        } else {
            uniqueID = sharedPref.getString("UUID", Timestamp.now().toString()).toString()
        }


        db.collection("dispositivos_registrados")
            .document(uniqueID)
            .set(numero)
            .addOnSuccessListener {
                val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.putString("UUID", uniqueID)
                editor.commit()
            }
            .addOnFailureListener { }
    }

    private fun sincronismoGeral(sharedPref: SharedPreferences) {
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.dialog_baixar_tudo, null)
        val textAtualizando: TextView = view.findViewById(R.id.text_atualizando)

        val atualiza = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            //.setTitle("")
            .setMessage("Baixando dados de alunos e visitantes")
            .show()

        db.collection("convidados")
            .get()
            .addOnSuccessListener { result ->

                var total = 0
                var contagem = 0
                if (result.size() > 0) {
                    for (document in result) {
                        total = result.size()
                        textAtualizando.text = "$contagem de $total registros baixados"
                        contagem++
                        val convidado = Convidado()
                        try{
                            convidado.nome_completo = document.data["nome_completo"].toString()
                            convidado.cpf = document.data["cpf"].toString()
                            convidado.padrinho = document.data["padrinho"] as Boolean
                            convidado.esquadrao = document.data["esquadrao"].toString()

                            val docRef1: DocumentReference = document.data["aluno"] as DocumentReference
                            convidado.nome_guerra = docRef1.id

                            db.collection("alunos.fake")//.document(docRef1.id)
                                .get()
                                .addOnSuccessListener { result2 ->
                                    for (document2 in result2) {
                                        val aluno = Aluno()
                                        aluno.milhao = document2.data["milhao"].toString()
                                        aluno.nome_guerra = document2.data["nome_guerra"].toString()
                                        aluno.esquadrao = document2.data["esquadrao"].toString()
                                    }

                                    db.collection("registros_acesso")
                                        .get()
                                        .addOnSuccessListener {result3->
                                            for(document3 in result3){
                                                Log.d("Documento",document3.id)
                                            }

                                            db.collection("tentativas_acesso")
                                                .get()
                                                .addOnSuccessListener {result4->
                                                    for(document4 in result4){
                                                        Log.d("Documento",document4.id)
                                                    }
                                                }


                                        }



                                }
                        } catch (e: Exception) {
                            val numero = hashMapOf(
                                "numero" to 0)
                            db.collection("registros_acesso").document("contagem").set(numero)
                                .addOnSuccessListener { }
                                .addOnFailureListener { }

                        }

                    }
                    atualiza.dismiss()
                    val editor: SharedPreferences.Editor = sharedPref.edit()
                    editor.putBoolean("primeiro_uso", true)
                    editor.commit()

                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {

            R.id.sair ->{

                val mBuilder = AlertDialog.Builder(this)
                    .setTitle("Atenção!")
                    .setMessage("Tem certeza que deseja efetuar logoff do aplicativo?")
                    .setPositiveButton("Sim", null)
                    .setNegativeButton("Não", null)
                    .show()

                    val mPositiveButton = mBuilder.getButton(AlertDialog.BUTTON_POSITIVE)

                    mPositiveButton.setOnClickListener {
                        Firebase.auth.signOut()
                        auth.signOut()
                        navController.navigateUp()
                        mBuilder.dismiss()

                    }
                return true
            }
            R.id.relatorio ->{

                if(auth.currentUser?.email.isNullOrEmpty()){

                    val inflater = this.layoutInflater
                    val view = inflater.inflate(R.layout.dialog_signin, null)
                    val email: TextInputEditText = view.findViewById(R.id.user)
                    val senha: TextInputEditText = view.findViewById(R.id.password)

                    val mBuilder = AlertDialog.Builder(this)
                        .setTitle("ÁREA RESTRITA")
                        .setView(view)
                        .setMessage("Autenticação necessária")
                        .setPositiveButton("Confirmar", null)
                        .setNegativeButton("Cancelar", null)
                        .show()

                    val mPositiveButton = mBuilder.getButton(AlertDialog.BUTTON_POSITIVE)

                    mPositiveButton.setOnClickListener {
                        Log.d("botao",email.toString())
                        if(TextUtils.isEmpty(email.text)){
                            email.error =
                                "Campo usuário não pode estar em branco"
                        } else if (TextUtils.isEmpty(senha.text)) {
                            senha.error =
                                "Campo senha não pode estar em branco"
                        } else {
                            loginUsuarioESenha(email, senha, mBuilder)
                        }

                    }

                } else {
                    abreRelatorio(navController)

                }

                return true
            }
            R.id.alterar_senha ->{
                val inflater = this.layoutInflater
                val view = inflater.inflate(R.layout.update_password, null)
                val email: EditText = view.findViewById(R.id.password)
                val repetir_email: EditText = view.findViewById(R.id.repetir_password)

                val mBuilder = AlertDialog.Builder(this)
                    .setTitle("ATENÇÃO")
                    .setView(view)
                    .setMessage("Digite a sua nova senha")
                    .setPositiveButton("Alterar", null)
                    .setNegativeButton("Cancelar", null)
                    .show()

                val mPositiveButton = mBuilder.getButton(AlertDialog.BUTTON_POSITIVE)

                mPositiveButton.setOnClickListener {

                    if(TextUtils.isEmpty(email.text)){
                        email.error =
                            "Nova senha não pode estar em branco"
                    } else if (TextUtils.isEmpty(repetir_email.text) or (email.text.toString() != repetir_email.text.toString()) ) {
                        repetir_email.error =
                            "As senhas precisam ser iguais"
                    } else {
                        auth.currentUser!!.updatePassword(email.text.toString())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        baseContext, "Senha para " + auth.currentUser!!.email + " alterada com sucesso.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    mBuilder.dismiss()
                                }
                            }.addOnFailureListener { task->
                            Log.d("Erro", task.toString())

                            }
                    }

                }
                return true
            }
                else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loginUsuarioESenha(email: EditText, senha: EditText, mBuilder: AlertDialog) {
        auth.signInWithEmailAndPassword(email.text.toString(), senha.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser

                    mBuilder.dismiss()
                    abreRelatorio(navController)

                } else {
                    Toast.makeText(
                        baseContext, "Acesso não autorizado",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
    }

    private fun abreRelatorio(navController: NavController) {
        navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
    /*
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.email?.isNotEmpty() == true) {
                Toast.makeText(
                    baseContext, "Usuário " + currentUser.email + " logado",
                    Toast.LENGTH_SHORT
                ).show()
                abreRelatorio(navController)
            }
        }
    }

     */




    override fun onBackPressed() {
        if(backPressedTime + 3000 > System.currentTimeMillis()){
            finish()
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Pressione voltar novamente para fechar o aplicativo.", Toast.LENGTH_LONG).show()

        }
        backPressedTime = System.currentTimeMillis()
    }



}