package ufpr.veiga.carteiravirtual

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.util.Locale

class ConverterActivity : AppCompatActivity() {

    private var saldoBRL: Double = 0.0
    private var saldoUSD: Double = 0.0
    private var saldoBTC: Double = 0.0

    private lateinit var spinnerMoedaOrigem: Spinner
    private lateinit var spinnerMoedaDestino: Spinner
    private lateinit var etValorConverter: EditText
    private lateinit var btnConfirmarConversao: Button
    private lateinit var progressBarConversao: ProgressBar
    private lateinit var tvResultadoConversao: TextView

    private val taxaBRLparaUSD = 0.20
    private val taxaUSDparaBRL = 5.0
    private val taxaBTCparaUSD = 50000.0
    private val taxaUSDparaBTC = 0.00002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_converter)

        saldoBRL = intent.getDoubleExtra("saldoBRL", 0.0)
        saldoUSD = intent.getDoubleExtra("saldoUSD", 0.0)
        saldoBTC = intent.getDoubleExtra("saldoBTC", 0.0)

        spinnerMoedaOrigem = findViewById(R.id.spinnerMoedaOrigem)
        spinnerMoedaDestino = findViewById(R.id.spinnerMoedaDestino)
        etValorConverter = findViewById(R.id.etValorConverter)
        btnConfirmarConversao = findViewById(R.id.btnConfirmarConversao)
        progressBarConversao = findViewById(R.id.progressBarConversao)
        tvResultadoConversao = findViewById(R.id.tvResultadoConversao)

        configurarSpinners()

        btnConfirmarConversao.setOnClickListener {
            realizarConversao()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ConverterActivity, MainActivity::class.java)
                intent.putExtra("saldoBRL", saldoBRL)
                intent.putExtra("saldoUSD", saldoUSD)
                intent.putExtra("saldoBTC", saldoBTC)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        })
    }

    private fun configurarSpinners() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.moedas_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerMoedaOrigem.adapter = adapter
        spinnerMoedaDestino.adapter = adapter
    }

    private fun realizarConversao() {
        tvResultadoConversao.text = ""

        val valorTexto = etValorConverter.text.toString()
        if (valorTexto.isEmpty()) {
            mostrarErro(getString(R.string.erro_valor_invalido))
            return
        }

        val valor = valorTexto.toDoubleOrNull()
        if (valor == null || valor <= 0) {
            mostrarErro(getString(R.string.erro_valor_invalido))
            return
        }

        val indexOrigem = spinnerMoedaOrigem.selectedItemPosition
        val indexDestino = spinnerMoedaDestino.selectedItemPosition

        if (indexOrigem == indexDestino) {
            mostrarErro(getString(R.string.erro_mesma_moeda))
            return
        }

        val saldoOrigem = obterSaldo(indexOrigem)
        if (valor > saldoOrigem) {
            mostrarErro(getString(R.string.erro_saldo_insuficiente))
            return
        }

        mostrarCarregamento()

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)

            val valorConvertido = calcularConversao(valor, indexOrigem, indexDestino)

            atualizarSaldos(valor, valorConvertido, indexOrigem, indexDestino)

            ocultarCarregamento()
            mostrarSucesso(valorConvertido, indexDestino)
        }
    }

    private fun calcularConversao(valor: Double, indexOrigem: Int, indexDestino: Int): Double {
        val valorEmUSD = when (indexOrigem) {
            0 -> valor * taxaBRLparaUSD
            1 -> valor
            2 -> valor * taxaBTCparaUSD
            else -> valor
        }

        return when (indexDestino) {
            0 -> valorEmUSD * taxaUSDparaBRL
            1 -> valorEmUSD
            2 -> valorEmUSD * taxaUSDparaBTC
            else -> valorEmUSD
        }
    }

    private fun obterSaldo(indexMoeda: Int): Double {
        return when (indexMoeda) {
            0 -> saldoBRL
            1 -> saldoUSD
            2 -> saldoBTC
            else -> 0.0
        }
    }

    private fun atualizarSaldos(valorOrigem: Double, valorDestino: Double, indexOrigem: Int, indexDestino: Int) {
        when (indexOrigem) {
            0 -> saldoBRL -= valorOrigem
            1 -> saldoUSD -= valorOrigem
            2 -> saldoBTC -= valorOrigem
        }

        when (indexDestino) {
            0 -> saldoBRL += valorDestino
            1 -> saldoUSD += valorDestino
            2 -> saldoBTC += valorDestino
        }
    }

    private fun mostrarCarregamento() {
        progressBarConversao.visibility = View.VISIBLE
        btnConfirmarConversao.isEnabled = false
    }

    private fun ocultarCarregamento() {
        progressBarConversao.visibility = View.GONE
        btnConfirmarConversao.isEnabled = true
    }

    private fun mostrarErro(mensagem: String) {
        tvResultadoConversao.text = mensagem
        tvResultadoConversao.setTextColor(Color.RED)
    }

    private fun mostrarSucesso(valorConvertido: Double, indexDestino: Int) {
        val textoResultado = when (indexDestino) {
            0 -> {
                val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                "Resultado: ${formatador.format(valorConvertido)}"
            }
            1 -> {
                val formatador = NumberFormat.getCurrencyInstance(Locale.US)
                "Resultado: ${formatador.format(valorConvertido)}"
            }
            2 -> "Resultado: ₿ %.8f".format(valorConvertido)
            else -> "Resultado: $valorConvertido"
        }

        tvResultadoConversao.text = textoResultado
        tvResultadoConversao.setTextColor(Color.parseColor("#00AA00"))
    }
}
