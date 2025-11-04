package ufpr.veiga.carteiravirtual

import android.os.Bundle
import android.util.Log
import android.util.Log.e
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ufpr.veiga.carteiravirtual.network.RetrofitClient
import ufpr.veiga.carteiravirtual.repository.AwesomeApiRepositoryImpl
import ufpr.veiga.carteiravirtual.repository.AwesomeRepository

class MainActivity : AppCompatActivity() {
    private val apiService = RetrofitClient.awesomeApi
    private val awesomeRepository: AwesomeRepository = AwesomeApiRepositoryImpl(apiService)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            try {
                awesomeRepository.obterCotacao("BRL", "USD")
            } catch (erro: Exception) {
                Log.e("CONVERSAO RRO", "ERRO: ${erro.message}", erro)
            }
        }
    }
}