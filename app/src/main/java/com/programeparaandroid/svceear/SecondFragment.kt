package com.programeparaandroid.svceear

import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.text.InputType
import android.text.format.Time
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.type.Date
import com.google.type.DateTime
import com.programeparaandroid.svceear.databinding.FragmentSecondBinding
import kotlin.time.Duration.Companion.seconds

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    val db = Firebase.firestore
    var portao : Long = 0
    var cte : Long = 0
    var total : Long = 0
    var indevido : Long= 0
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.title = "Administração"
        _binding = FragmentSecondBinding.inflate(inflater, container, false)


        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater!!.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.relatorio).isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contaRegistros()

        binding.infoDispositivos.setOnClickListener {
            val inflater = this.layoutInflater
            val view = inflater.inflate(R.layout.dialog_info_dispositivos, null)

            AlertDialog.Builder(context)
                .setView(view)
                .setTitle("Legenda")
                .setMessage("Tempo desde a última conexão:")
                .show()
        }

        binding.atualiza.setOnClickListener {
            zeraContadores()
            contaRegistros()

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

        val contagem = hashMapOf(
            "numero" to 0)

        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.dialog_carregando, null)

        val atualiza = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            //.setTitle("")
            .setMessage("Apagando registros de acesso")
            .show()

        habilitaBotoes(false)
        mBuilder.dismiss()
        db.collection("registros_acesso")
            .get()
            .addOnSuccessListener { result ->
            for (document in result) {
                db.collection("registros_acesso")
                    .document(document.id)
                    .delete()
                    .addOnSuccessListener {


                    }

            }
                db.collection("registros_acesso").document("contagem_cte").set(contagem)
                    .addOnSuccessListener { }
                    .addOnFailureListener { }

                db.collection("registros_acesso").document("contagem_principal").set(contagem)
                    .addOnSuccessListener { }
                    .addOnFailureListener { }

        }


        db.collection("tentativas_acesso")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    db.collection("tentativas_acesso")
                        .document(document.id)
                        .delete()
                        .addOnSuccessListener {

                        }
                }

                db.collection("tentativas_acesso").document("contagem_cte").set(contagem)
                    .addOnSuccessListener {
                        db.collection("tentativas_acesso").document("contagem_principal").set(contagem)
                            .addOnSuccessListener {
                                contaRegistros()
                                atualiza.dismiss()
                            }
                            .addOnFailureListener { }
                    }
                    .addOnFailureListener { }


            }


    }

    private fun contaRegistros() {
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.dialog_carregando, null)

        val atualiza = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            //.setTitle("")
            .setMessage("Verificando registros")
            .show()

        db.collection("registros_acesso")
            .document("contagem_principal")
            .get()
            .addOnSuccessListener { result ->
                portao = result.data?.get("numero") as Long

                db.collection("registros_acesso")
                    .document("contagem_cte")
                    .get()
                    .addOnSuccessListener { result ->
                        cte = result.data?.get("numero") as Long

                        contaConvidado(atualiza)

                    }
                    .addOnFailureListener { exception ->
                        Log.w(ContentValues.TAG, "Error getting documents.", exception)
                    }

            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }


    }

    private fun contaConvidado(atualiza:AlertDialog) {
        db.collection("convidados")
            .get()
            .addOnSuccessListener { result ->
                Log.d("Convidados", result.size().toString())
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }

        db.collection("convidados")
            .document("contagem")
            .get()
            .addOnSuccessListener { result ->
                total = result.data?.get("numero") as Long
                contaAcessoIndevido(atualiza)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }


    }

    private fun contaAcessoIndevido(atualiza: AlertDialog) {

        db.collection("tentativas_acesso")
            .document("contagem_principal")
            .get()
            .addOnSuccessListener { result ->
                indevido = result.data?.get("numero") as Long
                db.collection("tentativas_acesso")
                    .document("contagem_cte")
                    .get()
                    .addOnSuccessListener { result2 ->
                        indevido += result2.data?.get("numero") as Long
                        preencheContadores(atualiza)
                        dispositivosRegistrados()
                    }
                    .addOnFailureListener { exception ->
                        Log.w(ContentValues.TAG, "Error getting documents.", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }


    }

    private fun dispositivosRegistrados(){
        var online = 0
        var ausente = 0
        var offline = 0
        db.collection("dispositivos_registrados")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    binding.dispositivosTotal.text = result.size().toString()

                    var hora = document.data["data_hora"] as Timestamp

                    if((Timestamp.now().seconds -  hora.seconds) > 1800){
                        offline++
                    } else if((Timestamp.now().seconds -  hora.seconds) > 60) {
                        ausente++
                    } else if ((Timestamp.now().seconds -  hora.seconds) < 60){
                        online++
                    }

                }
                binding.dispositivosOnLine.text = online.toString()
                binding.dispositivosAusente.text = ausente.toString()
                binding.dispositivosOffLine.text = offline.toString()
            }
    }
    fun zeraContadores(){
        portao = 0
        cte = 0
        total = 0
        indevido = 0
    }

    fun preencheContadores(atualiza: AlertDialog){
        binding.convidadosPortaoPrincipal.text = portao.toString()
        binding.convidadosCte.text = cte.toString()
        binding.convidadosFaltam.text = (total - portao - cte).toString()
        binding.convidadosTotal.text = total.toString()
        binding.tentativasRegistro.text = indevido.toString()
        binding.tentativasRegistro.text = indevido.toString()
        atualiza.dismiss()
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