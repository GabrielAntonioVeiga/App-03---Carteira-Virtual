package ufpr.veiga.carteiravirtual

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ufpr.veiga.carteiravirtual.network.RetrofitClient
import java.text.NumberFormat
import java.util.*

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

    private val apiService = RetrofitClient.awesomeApi

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

        lifecycleScope.launch {
            try {
                val codOrigem = obterCodigoMoeda(indexOrigem)
                val codDestino = obterCodigoMoeda(indexDestino)


                val taxa = withContext(Dispatchers.IO) {
                    obterTaxaConversao(codOrigem, codDestino)
                }

                val valorConvertido = valor * taxa

                atualizarSaldos(valor, valorConvertido, indexOrigem, indexDestino)
                mostrarSucesso(valorConvertido, indexDestino)
            } catch (e: Exception) {
                mostrarErro("Erro ao obter cotação: ${e.message}")
            } finally {
                ocultarCarregamento()
            }
        }
    }

    private suspend fun obterTaxaConversao(origem: String, destino: String): Double {
        return try {
            val chave = "${origem}${destino}"
            val resposta = apiService.getCotacao(origem, destino)
            resposta[chave]?.bid?.toDouble() ?: throw Exception("Cotação não encontrada")
        } catch (e: Exception) {

            if (origem != "USD" && destino != "USD") {
                val viaUsd1 = apiService.getCotacao(origem, "USD")["${origem}USD"]?.bid?.toDouble()
                val viaUsd2 = apiService.getCotacao("USD", destino)["USD${destino}"]?.bid?.toDouble()
                if (viaUsd1 != null && viaUsd2 != null) viaUsd1 * viaUsd2
                else throw Exception("Conversão indireta não disponível")
            } else throw e
        }
    }

    private fun obterCodigoMoeda(index: Int): String {
        return when (index) {
            0 -> "BRL"
            1 -> "USD"
            2 -> "BTC"
            else -> "USD"
        }
    }

    private fun obterSaldo(index: Int): Double {
        return when (index) {
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
        spinnerMoedaOrigem.isEnabled = false
        spinnerMoedaDestino.isEnabled = false
        etValorConverter.isEnabled = false
    }

    private fun ocultarCarregamento() {
        progressBarConversao.visibility = View.GONE
        btnConfirmarConversao.isEnabled = true
        spinnerMoedaOrigem.isEnabled = true
        spinnerMoedaDestino.isEnabled = true
        etValorConverter.isEnabled = true
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
            2 -> "Resultado: ₿ %.4f".format(valorConvertido)
            else -> "Resultado: $valorConvertido"
        }
        tvResultadoConversao.text = textoResultado
        tvResultadoConversao.setTextColor(Color.parseColor("#00AA00"))
    }
}
