package com.example.uniformes

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.uniformes.data.AuthRepository
import com.example.uniformes.data.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var tilUsuario: TextInputLayout
    private lateinit var tilClave: TextInputLayout
    private lateinit var etUsuario: EditText
    private lateinit var etClave: EditText
    private lateinit var cbRecordar: MaterialCheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay sesión, saltar directo al sistema.
        if (SessionManager.usuarioActual(this) != null) {
            irAlSistema()
            return
        }

        setContentView(R.layout.activity_login)
        tilUsuario = findViewById(R.id.tilUsuario)
        tilClave = findViewById(R.id.tilClave)
        etUsuario = findViewById(R.id.etUsuario)
        etClave = findViewById(R.id.etClave)
        cbRecordar = findViewById(R.id.cbRecordar)

        etUsuario.doAfterTextChanged { tilUsuario.error = null }
        etClave.doAfterTextChanged { tilClave.error = null }

        etClave.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                intentarIngresar()
                true
            } else {
                false
            }
        }

        findViewById<MaterialButton>(R.id.btnIngresar).setOnClickListener { intentarIngresar() }
        findViewById<android.view.View>(R.id.tvOlvido).setOnClickListener {
            Toast.makeText(this, R.string.login_olvido_msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun intentarIngresar() {
        val usuario = etUsuario.text.toString().trim()
        val clave = etClave.text.toString()

        var valido = true
        if (usuario.isEmpty()) {
            tilUsuario.error = getString(R.string.login_err_usuario)
            valido = false
        }
        if (clave.isEmpty()) {
            tilClave.error = getString(R.string.login_err_clave)
            valido = false
        }
        if (!valido) return

        ocultarTeclado()
        val encontrado = AuthRepository.autenticar(usuario, clave)
        if (encontrado == null) {
            tilClave.error = getString(R.string.login_err_credenciales)
            return
        }

        SessionManager.iniciar(this, encontrado, cbRecordar.isChecked)
        irAlSistema()
    }

    private fun ocultarTeclado() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun irAlSistema() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
