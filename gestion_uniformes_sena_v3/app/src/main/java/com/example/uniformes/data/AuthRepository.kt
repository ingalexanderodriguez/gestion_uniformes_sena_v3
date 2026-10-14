package com.example.uniformes.data

import java.security.MessageDigest

/**
 * Autenticación de PRUEBA (sin servidor). Las contraseñas se guardan como hash SHA-256,
 * nunca en texto plano. En un sistema real esto lo valida un backend con bcrypt/argon2 y sal.
 */
object AuthRepository {

    private const val CLAVE_DEMO = "Uniformes2026"

    fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private val usuarios = listOf(
        Usuario("admin", "Administrador del sistema", Rol.ADMIN, hash(CLAVE_DEMO)),
        Usuario("ventas", "Juanita (Ventas)", Rol.VENTAS, hash(CLAVE_DEMO)),
        Usuario("inventario", "Encargado de Inventario", Rol.INVENTARIO, hash(CLAVE_DEMO)),
        Usuario("jefe", "Jefe de Producción", Rol.JEFE_PRODUCCION, hash(CLAVE_DEMO)),
        Usuario("operario", "Operario de Calidad", Rol.OPERARIO, hash(CLAVE_DEMO)),
        Usuario("contador", "Contador", Rol.CONTADOR, hash(CLAVE_DEMO))
    )

    fun buscar(usuario: String): Usuario? =
        usuarios.firstOrNull { it.usuario.equals(usuario.trim(), ignoreCase = true) }

    fun autenticar(usuario: String, clave: String): Usuario? {
        val u = buscar(usuario) ?: return null
        return if (u.claveHash == hash(clave)) u else null
    }
}
