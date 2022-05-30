package com.programeparaandroid.svceear

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.programeparaandroid.svceear.databinding.FragmentFirstBinding
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
        .baseUrl("http://10.232.18.xx/")
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

        /*
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }*/
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
                    // Converting the data to json format
                    val obj = JSONObject(result.contents)

                    val visitante = Visitante()
                    visitante.cpf = obj.getString("cpf")


                    chamaAPI(visitante)


                } catch (e: JSONException) {
                    e.printStackTrace()
                    exibeToast(false)
                    // Data not in the expected format. So, whole object as toast message.
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun chamaAPI(visitante: Visitante) {
        retrofit.setVisitante(
            //visitante.cpf
        ).enqueue(object : Callback<Visitante> {
            override fun onFailure(
                call: Call<Visitante>,
                t: Throwable
            ) {
                Log.d("Erro: ", t.toString())
            }

            override fun onResponse(
                call: Call<Visitante>,
                response: Response<Visitante>
            ) {

                if (response.isSuccessful) {
                    response.body()?.let {
                        if (response.body()!!.cpf.equals("vazio")) {
                            exibeToast(false)
                        } else {
                            binding.nomeVisitante.setText(response.body()!!.nome)
                            binding.cpfVisitante.setText(response.body()!!.cpf)
                            binding.nomeAluno.setText(response.body()!!.nome_aluno)
                            binding.milhaoAluno.setText(response.body()!!.milhao_aluno)
                            binding.esquadraoAluno.setText(response.body()!!.esquadrao_aluno)
                            binding.constraint.setBackgroundColor(Color.parseColor("#008000"))
                            exibeToast(true)
                        }
                    }
                }
            }
        })
    }


    private fun exibeToast(respostaServidor:Boolean){
        if(respostaServidor){
            Toast.makeText(activity, "Convidado relacionado", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(activity, "Convidado não relacionado", Toast.LENGTH_LONG).show()
            binding.constraint.setBackgroundColor(Color.RED)

        }
    }

}