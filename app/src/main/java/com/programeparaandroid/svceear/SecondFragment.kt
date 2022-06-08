package com.programeparaandroid.svceear

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.programeparaandroid.svceear.databinding.FragmentSecondBinding

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    val db = Firebase.firestore
    var portao = 0
    var cte = 0
    var total = 0
    var indevido = 0
    lateinit var progressBar : ProgressBar
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = "Acessos"
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contaRegistros()

        progressBar = binding.progressBar



        binding.atualiza.setOnClickListener {
            //findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            zeraContadores()
            contaRegistros()

            progressBar.visibility = View.VISIBLE
        }
    }

    private fun contaRegistros() {

        db.collection("registros_acesso")
            .get()
            .addOnSuccessListener { result ->
                for (consulta in result){
                    if(consulta["posto"].toString().equals("Portão Principal")){
                        portao++
                    }

                    if(consulta["posto"].toString().equals("CTE")){
                        cte++
                    }

                }
                contaConvidado()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }
    private fun contaConvidado() {

        db.collection("convidados")
            .get()
            .addOnSuccessListener { result ->
                total = result.size()
                contaAcessoIndevido()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    private fun contaAcessoIndevido() {

        db.collection("tentativas_acesso")
            .get()
            .addOnSuccessListener { result ->
                indevido = result.size()

                preencheContadores()
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    fun zeraContadores(){
        portao = 0
        cte = 0
        total = 0
        indevido = 0
    }
    fun preencheContadores(){
        binding.convidadosPortaoPrincipal.text = portao.toString()
        binding.convidadosCte.text = cte.toString()
        binding.convidadosFaltam.text = (total - portao - cte).toString()
        binding.convidadosTotal.text = total.toString()
        binding.tentativasRegistro.text = indevido.toString()
        binding.tentativasRegistro.text = indevido.toString()
        progressBar.visibility = View.INVISIBLE
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}