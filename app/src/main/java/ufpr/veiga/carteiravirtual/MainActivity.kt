package ufpr.veiga.carteiravirtual

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ufpr.veiga.carteiravirtual.network.RetrofitClient
import java.text.NumberFormat
import java.util.*
import kotlin.text.get

class MainActivity : AppCompatActivity() {

    private var saldoBRL: Double = 100000.0
    private var saldoUSD: Double = 50000.0
    private var saldoBTC: Double = 0.5

    private lateinit var tvSaldoReais: TextView
    private lateinit var tvSaldoDolares: TextView
    private lateinit var tvSaldoBitcoin: TextView
    private lateinit var btnIrParaConversao: Button

    private val apiService = RetrofitClient.awesomeApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        tvSaldoReais = findViewById(R.id.tvSaldoReais)
        tvSaldoDolares = findViewById(R.id.tvSaldoDolares)
        tvSaldoBitcoin = findViewById(R.id.tvSaldoBitcoin)
        btnIrParaConversao = findViewById(R.id.btnIrParaConversao)

        atualizarSaldos()


        btnIrParaConversao.setOnClickListener {
            val intent = Intent(this, ConverterActivity::class.java).apply {
                putExtra("saldoBRL", saldoBRL)
                putExtra("saldoUSD", saldoUSD)
                putExtra("saldoBTC", saldoBTC)
            }
            startActivity(intent)
        }


        lifecycleScope.launch {
            try {
                val resposta = apiService.getCotacao("USD", "BRL")
                Log.d("COTAÇÃO", "USD/BRL: ${resposta["USDBRL"]?.bid}")
            } catch (e: Exception) {
                Log.e("API", "Erro ao acessar API: ${e.message}", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        intent?.let {
            saldoBRL = it.getDoubleExtra("saldoBRL", saldoBRL)
            saldoUSD = it.getDoubleExtra("saldoUSD", saldoUSD)
            saldoBTC = it.getDoubleExtra("saldoBTC", saldoBTC)
            atualizarSaldos()
        }
    }

    private fun atualizarSaldos() {
        val formatadorBRL = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val formatadorUSD = NumberFormat.getCurrencyInstance(Locale.US)

        tvSaldoReais.text = "Saldo BRL: ${formatadorBRL.format(saldoBRL)}"
        tvSaldoDolares.text = "Saldo USD: ${formatadorUSD.format(saldoUSD)}"
        tvSaldoBitcoin.text = "Saldo BTC: ₿ %.4f".format(saldoBTC)
    }
}
