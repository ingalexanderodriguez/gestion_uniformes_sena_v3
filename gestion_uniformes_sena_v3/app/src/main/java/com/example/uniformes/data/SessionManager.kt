package com.example.uniformes.data

import android.content.Context

/** Guarda quién inició sesión. Solo se guarda el nombre de usuario, nunca la contraseña. */
object SessionManager {
    private const val PREFS = "sesion"
    private const val KEY_USUARIO = "usuario"

    // Sesión solo en memoria cuando NO se marca "Mantener sesión iniciada".
    private var enMemoria: Usuario? = null

    fun iniciar(context: Context, usuario: Usuario, recordar: Boolean) {
        enMemoria = usuario
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        if (recordar) prefs.putString(KEY_USUARIO, usuario.usuario) else prefs.remove(KEY_USUARIO)
        prefs.apply()
    }

    fun usuarioActual(context: Context): Usuario? {
        enMemoria?.let { return it }
        val guardado = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_USUARIO, null)
        return guardado?.let { AuthRepository.buscar(it) }?.also { enMemoria = it }
    }

    fun cerrar(context: Context) {
        enMemoria = null
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_USUARIO).apply()
    }
}
