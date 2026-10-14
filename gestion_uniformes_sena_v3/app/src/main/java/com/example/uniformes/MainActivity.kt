package com.example.uniformes

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.uniformes.data.SessionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sin sesión -> volver al login.
        val usuario = SessionManager.usuarioActual(this)
        if (usuario == null) {
            irAlLogin()
            return
        }

        setContentView(R.layout.activity_main)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        val modulos = listOf(
            R.id.dashboardFragment, R.id.pedidosFragment, R.id.produccionFragment,
            R.id.nuevoPedidoFragment, R.id.controlCalidadFragment, R.id.facturacionFragment
        )

        val config = AppBarConfiguration(modulos.toSet(), drawer)
        toolbar.setupWithNavController(navController, config)
        navView.setupWithNavController(navController)

        // Control de acceso por rol: solo se muestran los módulos permitidos.
        modulos.forEach { id -> navView.menu.findItem(id)?.isVisible = id in usuario.rol.modulos }

        // Datos del usuario en la cabecera del menú.
        val cabecera = navView.getHeaderView(0)
        cabecera.findViewById<TextView>(R.id.tvNavUsuario).text = usuario.nombre
        cabecera.findViewById<TextView>(R.id.tvNavRol).text = usuario.rol.etiqueta

        // Navegación + cerrar sesión.
        navView.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.menu_logout) {
                confirmarCierre()
                true
            } else {
                val ok = NavigationUI.onNavDestinationSelected(item, navController)
                if (ok) drawer.closeDrawers()
                ok
            }
        }

        if (savedInstanceState == null) {
            Toast.makeText(this, getString(R.string.bienvenida, usuario.nombre), Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarCierre() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout_titulo)
            .setMessage(R.string.logout_mensaje)
            .setNegativeButton(R.string.logout_no, null)
            .setPositiveButton(R.string.logout_si) { _, _ ->
                SessionManager.cerrar(this)
                irAlLogin()
            }
            .show()
    }

    private fun irAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
