package com.programeparaandroid.svceear

import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
        binding.constraint.setBackgroundColor(resources.getColor(R.color.grey))

        binding.atualiza.setOnClickListener {
            //findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            zeraContadores()
            contaRegistros()
            binding.constraint.setBackgroundColor(resources.getColor(R.color.grey))
            progressBar.visibility = View.VISIBLE
            habilitaBotoes(false)
        }

        binding.apaga.setOnClickListener{

            var editText = EditText(context)
            editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

            val mBuilder = AlertDialog.Builder(context)
                .setTitle("Atenção")
                .setMessage("Tem certeza que deseja apagar todo o banco de dados?")
                .setView(editText)
                .setPositiveButton("Sim", null)
                .setNegativeButton("Não", null)
                .show()

            val mPositiveButton = mBuilder.getButton(AlertDialog.BUTTON_POSITIVE)

            mPositiveButton.setOnClickListener {
                limpaAcessos(mBuilder)

            }
        }
    }

    private fun limpaAcessos(mBuilder: AlertDialog) {
        zeraContadores()
        progressBar.visibility = View.VISIBLE
        habilitaBotoes(false)
        mBuilder.dismiss()
        db.collection("registros_acesso").get().addOnSuccessListener { result ->
            for (document in result) {
                db.collection("registros_acesso").document(document.id).delete()
                    .addOnSuccessListener {
                        db.collection("tentativas_acesso").get().addOnSuccessListener { result ->
                            for (document in result) {
                                db.collection("tentativas_acesso").document(document.id).delete()
                                    .addOnSuccessListener {
                                        contaRegistros()
                                    }
                            }
                        }
                    }
            }
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

        db.collection("convidados.fake")
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
        binding.constraint.setBackgroundColor(resources.getColor(R.color.white))

        progressBar.visibility = View.INVISIBLE
        habilitaBotoes(true)
    }

    private fun habilitaBotoes(habilita: Boolean){
        if(habilita){
            binding.atualiza.background.setTint(Color.parseColor("#FF03DAC5"))
            binding.atualiza.isEnabled = true
            binding.apaga.background.setTint(Color.parseColor("#FFFF0000"))
            binding.apaga.isEnabled = true
        } else {
            binding.atualiza.background.setTint(Color.parseColor("#a9a9a9"))
            binding.atualiza.isEnabled = false
            binding.apaga.background.setTint(Color.parseColor("#a9a9a9"))
            binding.apaga.isEnabled = false
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}