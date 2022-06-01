package com.programeparaandroid.svceear

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.programeparaandroid.svceear.databinding.FragmentFirstBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var qrScanIntegrator: IntentIntegrator



    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://192.168.0.148/visitanteseear/public/webservices/")
        .build()
        .create(MainActivity.enviaVisitante::class.java)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupScanner()
        setOnClickListener()
        val visitante = Visitante()
        visitante.cpf = ("cpf")

        /*
        val visitantedb2 = VisitanteDB(2,"Jorge Luis Silva dos Santos","01337453765","JORGE SANTOS","N/I","CFS")
        val visitantedb3 = VisitanteDB(3,"Debora Gomes de Andrade","08520805736","JORGE SANTOS","N/I","CFS")
        val visitantedb4 = VisitanteDB(4,"Margarete Raimunda dos Santos Ferreira","01276231750","MATHEUS","N/I","EAGS")
        val visitantedb5 = VisitanteDB(5,"Gilmar Alves Ferreira","02557361771","MATHEUS","N/I","EAGS")
        val visitantedb6 = VisitanteDB(6,"THAIS FERREIRA CAMPOS ","18122134769","MATHEUS","N/I","EAGS")
        val visitantedb7 = VisitanteDB(7,"LUCINEIA FERREIRA ABRAHÃO","00952102706","MATHEUS","N/I","EAGS")
        val visitantedb8 = VisitanteDB(8,"Lais Alves Ferreira","01419097733","MATHEUS","N/I","EAGS")
        val visitantedb9 = VisitanteDB(9,"Maria Alves de souza","54886457720","MATHEUS","N/I","EAGS")
        val visitantedb10 = VisitanteDB(10,"Vitória  dos Santos Ferreira","19806143779","MATHEUS","N/I","EAGS")

        MyApplication.database?.userDao()?.insertAll(visitantedb2,visitantedb3, visitantedb4,
        visitantedb5, visitantedb6, visitantedb7, visitantedb8, visitantedb9, visitantedb10)
        */


        binding.btnCancel.setOnClickListener {
            limpaCampos()
        }

        binding.cpfVisitante.setOnKeyListener { v, keyCode, event ->

            when {

                //Check if it is the Enter-Key,      Check if the Enter Key was pressed down
                ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) -> {
                    try{
                        val visitante1: VisitanteDB? = buscaNoBanco(binding.cpfVisitante.text.toString())
                        preencheCampos(visitante1)
                        exibeToast("relacionado")
                    } catch (e: Exception){
                        exibeToast("nao_relacionado")
                    }




                    hideKeyboard()

                    //return true
                    return@setOnKeyListener true
                }
                else -> false
            }


        }

        binding.btnSave.setOnClickListener{
            exibeToast("registrado")

        }


        /*
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }*/
    }

    private fun limpaCampos() {
        binding.nomeVisitante.text.clear()
        binding.cpfVisitante.text.clear()
        binding.nomeAluno.text.clear()
        binding.milhaoAluno.text.clear()
        binding.esquadraoAluno.text.clear()


    }

    private fun setupScanner() {
        qrScanIntegrator = IntentIntegrator.forSupportFragment(this)
        qrScanIntegrator.setOrientationLocked(false)
    }

    private fun setOnClickListener() {
        binding.btnScan.setOnClickListener { performAction() }
    }

    private fun performAction() {
        // Code to perform action when button is clicked.
        qrScanIntegrator.initiateScan()



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            // If QRCode has no data.
            if (result.contents == null) {
                Toast.makeText(activity, R.string.result_not_found, Toast.LENGTH_LONG).show()
            } else {
                // If QRCode contains data.
                try {

                    val visitante1: VisitanteDB? = buscaNoBanco(result.contents)
                    try{
                        preencheCampos(visitante1)
                        exibeToast("relacionado")
                        habilitaBotoes(true)
                    } catch (e: Exception){
                        habilitaBotoes(false)
                        exibeToast("nao_relacionado")
                    }




                    val visitante = Visitante()
                    visitante.cpf = "cpf"


                    chamaAPI(visitante)


                } catch (e: JSONException) {
                    e.printStackTrace()
                    exibeToast("nao_relacionado")
                    habilitaBotoes(false)
                    // Data not in the expected format. So, whole object as toast message.
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun buscaNoBanco(result: String): VisitanteDB? {
        val visitante1: VisitanteDB? = MyApplication.database?.userDao()?.findByCPF(result)
        return visitante1
    }

    private fun preencheCampos(visitante1: VisitanteDB?) {
        binding.nomeVisitante.setText(visitante1!!.nome_completo)
        binding.cpfVisitante.setText(visitante1.cpf)
        binding.nomeAluno.setText(visitante1.nome_guerra)
        binding.milhaoAluno.setText(visitante1.milhao)
        binding.esquadraoAluno.setText(visitante1.esquadrao)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun chamaAPI(visitante: Visitante) {
        retrofit.setVisitante(
            0
        ).enqueue(object : Callback<ArrayList<Visitante>> {
            override fun onFailure(
                call: Call<ArrayList<Visitante>>,
                t: Throwable
            ) {
                Log.d("Erro1: ", t.toString())
            }

            override fun onResponse(
                call: Call<ArrayList<Visitante>>,
                response: Response<ArrayList<Visitante>>
            ) {

                if (response.isSuccessful) {

                    response.body()?.let {

                        Log.d("resposta", response.body()!!.get(1).nome_completo)

/*
                        if (response.body()!!.cpf.equals("vazio")) {
                            //exibeToast(false)
                        } else {

                            binding.nomeVisitante.setText(response.body()!!.nome)
                            binding.cpfVisitante.setText(response.body()!!.cpf)
                            binding.nomeAluno.setText(response.body()!!.nome_aluno)
                            binding.milhaoAluno.setText(response.body()!!.milhao_aluno)
                            binding.esquadraoAluno.setText(response.body()!!.esquadrao_aluno)
                            binding.constraint.setBackgroundColor(Color.parseColor("#008000"))
                            //exibeToast(true)
                        }
                        */

                    }
                } else {
                    Log.d("erro",response.toString())
                }
            }
        })
    }


    private fun exibeToast(respostaServidor:String){
        if(respostaServidor.equals("relacionado")){


            view?.let {
                Snackbar.make(it, "Convidado relacionado", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setBackgroundTint(Color.parseColor("#008000"))
                    .setTextColor(Color.WHITE)
                    .show()
            }

            habilitaBotoes(true)

        } else if(respostaServidor.equals("nao_relacionado")){

            view?.let {
                Snackbar.make(it, "Convidado não relacionado", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setBackgroundTint(Color.parseColor("#FF0000"))
                    .setTextColor(Color.WHITE)
                    .show()
            }

        } else if(respostaServidor.equals("limpo")){
            habilitaBotoes(false)
            exibeToast("limpo")

        } else if(respostaServidor.equals("ja_entou")){
            view?.let {
                Snackbar.make(it, "Convidado não relacionado", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setBackgroundTint(Color.parseColor("#FFFF00"))
                    .show()
            }

        } else if(respostaServidor.equals("registrado")){
            Toast.makeText(activity, "Registro efetuado com sucesso", Toast.LENGTH_LONG).show()
            limpaCampos()
            habilitaBotoes(false)

        }
    }

    private fun habilitaBotoes(habilita: Boolean){
        if(habilita){
            binding.btnCancel.background.setTint(Color.parseColor("#FF03DAC5"))
            binding.btnSave.background.setTint(Color.parseColor("#FF03DAC5"))
        } else {
            binding.btnCancel.background.setTint(Color.parseColor("#a9a9a9"))
            binding.btnSave.background.setTint(Color.parseColor("#a9a9a9"))
        }

    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}