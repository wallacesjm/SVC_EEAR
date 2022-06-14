package com.programeparaandroid.svceear

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.programeparaandroid.svceear.databinding.FragmentFirstBinding
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream
import kotlin.random.Random


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var qrScanIntegrator: IntentIntegrator
    val db = Firebase.firestore
    var acessoDuplicado : Boolean = false
    lateinit var progressBar : ProgressBar

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater!!.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.sair).isVisible = false
        menu.findItem(R.id.alterar_senha).isVisible = false

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupScanner()
        setOnClickListener()
        progressBar = binding.progressBar2
        val spinner: Spinner = binding.spinner

        val cacheFile = File(requireContext().dataDir, "lista20.xls")

        // - Add alunos

        for (i in 1..200){
            lerExcelParaAddAluno(cacheFile, i)
        }




        // - Add convidados
        /*
        for (i in 1..1){
            lerExcelParaAddConvidado(cacheFile, i)
        }

         */









        // - Add convidados fake
        /*
        for (i in 51..99){
            criaConvidadoFake("000000000$i")
        }*/


        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.postos,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }



        binding.cpfVisitante.setOnKeyListener { v, keyCode, event ->

            when {

                ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) -> {

                    (activity as MainActivity?)?.registraDispositivo()
                    verificaConvidado(binding.cpfVisitante.text.toString())



                    hideKeyboard()

                    return@setOnKeyListener true
                }

                else -> false
            }

        }

        binding.btnSave.setOnClickListener{
            registraAcesso(acessoDuplicado)
        }
    }

    private fun verificaConvidado(qrcode: String) {

        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.dialog_carregando, null)



        val atualiza = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            //.setTitle("")
            .setMessage("Verificando convidado")
            .show()

        habilitaBotoes(false)



        db.collection("convidados")
            .whereEqualTo("cpf", qrcode)
            .get()
            .addOnSuccessListener { result ->
                if (result.size() > 0) {
                    for (document in result) {

                        val convidado = Convidado()
                        convidado.nome_completo = document.data["nome_completo"].toString()
                        convidado.cpf = document.data["cpf"].toString()
                        convidado.padrinho = document.data["padrinho"] as Boolean

                        val docRef1: DocumentReference = document.data["aluno"] as DocumentReference
                        convidado.nome_guerra = docRef1.id

                        db.collection("alunos").document(docRef1.id)
                            .get()
                            .addOnSuccessListener { result2 ->
                                convidado.nome_guerra = result2.data?.get("nome_guerra").toString()
                                convidado.esquadrao = result2.data?.get("esquadrao").toString()
                                verificaRegistro(convidado)
                            }
                    }
                } else {
                    habilitaBotoes(false)
                    exibeToast("nao_relacionado")
                    registraTentativaAcesso()
                }
                atualiza.dismiss()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun contaConvidado() {
        db.collection("convidados")
            .get()
            .addOnSuccessListener { result ->
                for(document in result){
                    if(document["cpf"].toString().length < 11){
                        Log.d("CPF",document["cpf"].toString())

                    }
                }

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun verificaRegistro(convidado: Convidado){

        var contagem = 0
        db.collection("registros_acesso")
            .whereEqualTo("convidado_cpf", convidado.cpf)
            .whereEqualTo("posto",binding.spinner.selectedItem.toString())
            .get()
            .addOnSuccessListener { result ->
                if(result.size() > 0){
                    for(document in result){
                        if(contagem == 0){
                            exibeToast("registro_anterior_existente")
                        }
                        contagem++
                    }
                } else {
                    if(convidado.padrinho){
                        exibeToast("relacionado")
                    } else {

                        if(binding.spinner.selectedItem.toString().equals("CTE")){
                            exibeToast("nao_autorizado")
                        } else {
                            exibeToast("relacionado")
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        preencheCampos(convidado)
        habilitaBotoes(true)
    }

    private fun registraAcesso(acessoDuplicado : Boolean){

        val acesso = hashMapOf(
            "convidado_cpf" to binding.cpfVisitante.text.toString(),
            "posto" to binding.spinner.selectedItem.toString(),
            "data_hora" to Timestamp.now()
        )

        var posto_selecionado = ""
        if(binding.spinner.selectedItem.toString() == "Portão Principal"){
            posto_selecionado = "contagem_principal"
        } else {
            posto_selecionado = "contagem_cte"
        }

        db.collection("registros_acesso")
            .add(acesso)
            .addOnSuccessListener {
                exibeToast("registro_efetuado")
                if(!acessoDuplicado){
                    db.collection("registros_acesso")
                        .document(posto_selecionado)
                        .update("numero", FieldValue.increment(1))
                }
            }
            .addOnFailureListener { exibeToast("registro_nao_efetuado") }

        if(!isOnline()){
            exibeToast("registro_efetuado_offline")


        }



    }

    private fun registraTentativaAcesso(){
        val acesso = hashMapOf(
            "qrcode" to binding.cpfVisitante.text.toString(),
            "posto" to binding.spinner.selectedItem.toString(),
            "data_hora" to Timestamp.now()
        )

        var posto_selecionado = ""
        if(binding.spinner.selectedItem.toString() == "Portão Principal"){
            posto_selecionado = "contagem_principal"
        } else {
            posto_selecionado = "contagem_cte"
        }

        db.collection("tentativas_acesso")
            .add(acesso)
            .addOnSuccessListener {
                db.collection("tentativas_acesso")
                    .document(posto_selecionado)
                    .update("numero", FieldValue.increment(1))
            }

    }

    private fun limpaCampos() {
        binding.nomeVisitante.text.clear()
        binding.cpfVisitante.text.clear()
        binding.nomeAluno.text.clear()
        binding.esquadraoAluno.text.clear()
        habilitaBotoes(false)
    }

    private fun limpaCamposExcetoCPF() {
        binding.nomeVisitante.text.clear()
        binding.nomeAluno.text.clear()
        binding.esquadraoAluno.text.clear()
        habilitaBotoes(false)
    }

    private fun setupScanner() {
        qrScanIntegrator = IntentIntegrator.forSupportFragment(this).setBeepEnabled(false)
        qrScanIntegrator.setOrientationLocked(false)
    }

    private fun setOnClickListener() {
        binding.btnScan.setOnClickListener { performAction() }
    }

    private fun performAction() {
        qrScanIntegrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(activity, R.string.result_not_found, Toast.LENGTH_LONG).show()
            } else {
                verificaConvidado(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun preencheCampos(convidado: Convidado) {
        binding.nomeVisitante.setText(convidado!!.nome_completo)
        binding.cpfVisitante.setText(convidado.cpf)
        binding.nomeAluno.setText(convidado.nome_guerra)
        binding.esquadraoAluno.setText(convidado.esquadrao)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun exibeToast(respostaServidor:String){


        if(respostaServidor.equals("relacionado")){
            acessoDuplicado = false
            view?.let {
                Snackbar.make(it, "Convidado relacionado. \n Verifique o documento de identificação", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setBackgroundTint(Color.parseColor("#008000"))
                    .setTextColor(Color.WHITE)
                    .show()
            }

            habilitaBotoes(true)
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)

        } else if(respostaServidor.equals("nao_relacionado")){
            acessoDuplicado = false
            view?.let {
                Snackbar.make(it, "Convidado não relacionado", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setBackgroundTint(Color.parseColor("#FF0000"))
                    .setTextColor(Color.WHITE)
                    .show()
            }
            habilitaBotoes(false)
            limpaCamposExcetoCPF()
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 100)

        } else if(respostaServidor.equals("limpo")){
            acessoDuplicado = false
            habilitaBotoes(false)
            exibeToast("limpo")

        }  else if(respostaServidor.equals("nao_autorizado")){
            acessoDuplicado = false
            view?.let {
                Snackbar.make(it, "Convidado não autorizado neste ponto", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setBackgroundTint(Color.BLUE)
                    .show()
            }
            habilitaBotoes(true)
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 100)
        } else if(respostaServidor.equals("registro_efetuado")) {
            Toast.makeText(activity, "Registro efetuado com sucesso", Toast.LENGTH_LONG).show()
            limpaCampos()
            habilitaBotoes(false)

        } else if(respostaServidor.equals("registro_efetuado_offline")) {
            Toast.makeText(activity, "Registro efetuado com sucesso", Toast.LENGTH_LONG).show()
            limpaCampos()
            habilitaBotoes(false)

        } else if(respostaServidor.equals("registro_nao_efetuado")) {
            acessoDuplicado = false
            Toast.makeText(activity, "Registro não efetuado. Tente novamente.", Toast.LENGTH_LONG).show()
        } else if(respostaServidor.equals("registro_anterior_existente")){
            acessoDuplicado = true
            view?.let {
                Snackbar.make(it, "QRCode já registrado nesse ponto.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setBackgroundTint(Color.YELLOW)
                    .setTextColor(Color.BLACK)
                    .show()
            }

            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 100)
        }


    }

    private fun habilitaBotoes(habilita: Boolean){
        if(habilita){
            binding.btnCancel.background.setTint(Color.parseColor("#FF03DAC5"))
            binding.btnCancel.isEnabled = true
            binding.btnSave.background.setTint(Color.parseColor("#FF03DAC5"))
            binding.btnSave.isEnabled = true
        } else {
            binding.btnCancel.background.setTint(Color.parseColor("#a9a9a9"))
            binding.btnCancel.isEnabled = false
            binding.btnSave.background.setTint(Color.parseColor("#a9a9a9"))
            binding.btnSave.isEnabled = false
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


    fun lerExcelParaAddAluno(filepath: File, linha: Int) {
        val inputStream = FileInputStream(filepath)
        var xlWb = WorkbookFactory.create(inputStream)

        val xlWs = xlWb.getSheetAt(0)

        val aluno = Aluno()
        aluno.esquadrao = xlWs.getRow(linha).getCell(0).toString()
        aluno.nome_guerra = xlWs.getRow(linha).getCell(1).toString()
        aluno.milhao = xlWs.getRow(linha).getCell(2).toString()
        registroAluno(aluno)
        Log.d("Aluno", "Nome:"+aluno.nome_guerra + " Milhão:"+aluno.milhao + " Esquadrão:"+aluno.esquadrao)
    }

    fun lerExcelParaAddConvidado(filepath: File, linha: Int) {

        val inputStream = FileInputStream(filepath)
        var xlWb = WorkbookFactory.create(inputStream)

        val xlWs = xlWb.getSheetAt(0)
        //Log.d("CPF", xlWs.getRow(linha).getCell(6).toString())



        if(xlWs.getRow(linha).getCell(3).toString() != ""){
            db.collection("alunos")
                .whereEqualTo("milhao",xlWs.getRow(linha).getCell(2).toString())
                .get()
                .addOnSuccessListener { result ->
                    if (result.size() > 0) {
                        for (document in result) {

                            val convidado = hashMapOf(
                                "cpf" to xlWs.getRow(linha).getCell(3).toString(),
                                "nome_completo" to xlWs.getRow(linha).getCell(2).toString(),
                                "aluno" to db.collection("alunos")
                                    .document(document.id),
                                "padrinho" to true
                            )
                            //Log.d("Convidado ->",convidado.toString())
                            //Log.d("Convidado", convidado.toString())

                            db.collection("convidados")
                                .add(convidado)
                                .addOnSuccessListener { exibeToast("registro_efetuado") }
                                .addOnFailureListener { exibeToast("registro_nao_efetuado") }
                            /*
                            db.collection("convidados")
                                .document("contagem")
                                .update("numero", FieldValue.increment(1))

                             */






                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }


    }

    private fun criaConvidadoFake(contagem: String){
        val allowedChars = ('A'..'Z') + ('a'..'z')

        val convidado = Convidado()
        convidado.cpf = contagem
        convidado.nome_completo = (1..10).map { allowedChars.random() }.joinToString("")
        convidado.padrinho = Random.nextBoolean()

        registroConvidadoFake(convidado)

    }

    private fun registroConvidadoFake(convidado: Convidado){

        val convidado = hashMapOf(
            "cpf" to convidado.cpf,
            "nome_completo" to convidado.nome_completo,
            "aluno" to db.collection("alunos.fake")
                .document(
                        "lgH9CnLk0xeYff0Fv9Z2"),
            "padrinho" to convidado.padrinho
        )

        db.collection("convidados.fake")
            .add(convidado)
            .addOnSuccessListener { exibeToast("registro_efetuado") }
            .addOnFailureListener { exibeToast("registro_nao_efetuado") }

    }

    private fun registroAluno(aluno: Aluno){

        val convidado = hashMapOf(
            "milhao" to aluno.milhao,
            "nome_guerra" to aluno.nome_guerra,
            "esquadrao" to aluno.esquadrao
        )

        db.collection("alunos")
            .add(convidado)
            .addOnSuccessListener {
                Log.d("Cadastrado", "OK")
                //exibeToast("registro_efetuado")
            }.addOnFailureListener {
                Log.d("Cadastrado", "Não")
            }

    }

    fun isOnline(): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }


}