package com.chavix.cloneapp

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.widget.Toast

class CloneInstallActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pkg = intent.getStringExtra(Const.EXTRA_PKG)
        val dpm = getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(this, CloneAdminReceiver::class.java)
        val msg = when {
            pkg.isNullOrBlank() -> "Paket adı yok"
            !dpm.isProfileOwnerApp(packageName) -> "Bu kopya çalışma profilinde değil"
            Build.VERSION.SDK_INT < 28 -> "Android 9+ gerekli"
            else -> try {
                if (dpm.installExistingPackage(admin, pkg)) "Klonlandı: $pkg" else "Klonlanamadı: $pkg"
            } catch (e: Exception) {
                "Hata: ${e.javaClass.simpleName}: ${e.message}"
            }
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        finish()
    }
}
