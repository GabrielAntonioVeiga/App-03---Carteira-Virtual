package ufpr.veiga.carteiravirtual

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var saldoBRL: Double = 100000.0
    private var saldoUSD: Double = 50000.0
    private var saldoBTC: Double = 0.5

    private lateinit var tvSaldoReais: TextView
    private lateinit var tvSaldoDolares: TextView
    private lateinit var tvSaldoBitcoin: TextView
    private lateinit var btnIrParaConversao: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSaldoReais = findViewById(R.id.tvSaldoReais)
        tvSaldoDolares = findViewById(R.id.tvSaldoDolares)
        tvSaldoBitcoin = findViewById(R.id.tvSaldoBitcoin)
        btnIrParaConversao = findViewById(R.id.btnIrParaConversao)

        atualizarSaldos()

        btnIrParaConversao.setOnClickListener {
            val intent = Intent(this, ConverterActivity::class.java)
            intent.putExtra("saldoBRL", saldoBRL)
            intent.putExtra("saldoUSD", saldoUSD)
            intent.putExtra("saldoBTC", saldoBTC)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        saldoBRL = intent.getDoubleExtra("saldoBRL", saldoBRL)
        saldoUSD = intent.getDoubleExtra("saldoUSD", saldoUSD)
        saldoBTC = intent.getDoubleExtra("saldoBTC", saldoBTC)
        atualizarSaldos()
    }

    private fun atualizarSaldos() {
        val formatadorBRL = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        tvSaldoReais.text = "Saldo BRL: ${formatadorBRL.format(saldoBRL)}"

        val formatadorUSD = NumberFormat.getCurrencyInstance(Locale.US)
        tvSaldoDolares.text = "Saldo USD: ${formatadorUSD.format(saldoUSD)}"

        tvSaldoBitcoin.text = "Saldo BTC: ₿ %.4f".format(saldoBTC)
    }
}