package com.chavix.cloneapp

import android.app.Activity
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private data class AppItem(val label: String, val pkg: String) {
        override fun toString() = "$label\n$pkg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dpm = getSystemService(DevicePolicyManager::class.java)
        val inWorkProfile = dpm.isProfileOwnerApp(packageName)

        // Kişisel tarafta klon alıcı activity kapatılır; böylece intent sadece çalışma profiline gider.
        if (!inWorkProfile) {
            packageManager.setComponentEnabledSetting(
                ComponentName(this, CloneInstallActivity::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 48, 24, 24)
        }
        setContentView(root)

        if (inWorkProfile) {
            root.addView(TextView(this).apply {
                text = "Bu kopya çalışma profilinde çalışıyor. Klonlamak için kişisel taraftaki Clone App'i aç."
                textSize = 16f
            })
            return
        }

        root.addView(Button(this).apply {
            text = "1) Çalışma profili oluştur (bir kere)"
            setOnClickListener { startProvisioning() }
        })
        root.addView(TextView(this).apply {
            text = "2) Klonlamak için uygulamaya dokun"
            setPadding(0, 24, 0, 12)
        })

        val pm = packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val items = pm.queryIntentActivities(launcher, 0)
            .map { AppItem(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
            .filter { it.pkg != packageName }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }

        val list = ListView(this)
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        list.setOnItemClickListener { _, _, pos, _ -> confirmClone(items[pos]) }
        root.addView(list)
    }

    private fun startProvisioning() {
        val i = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).putExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
            ComponentName(this, CloneAdminReceiver::class.java)
        )
        try {
            startActivityForResult(i, 1)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Bu cihaz çalışma profilini desteklemiyor", Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "İzin hatası: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmClone(item: AppItem) {
        AlertDialog.Builder(this)
            .setTitle("Klonla")
            .setMessage("${item.label} çalışma profiline kurulsun mu?")
            .setPositiveButton("Evet") { _, _ -> sendClone(item.pkg) }
            .setNegativeButton("Hayır", null)
            .show()
    }

    private fun sendClone(pkg: String) {
        val i = Intent(Const.ACTION_CLONE).putExtra(Const.EXTRA_PKG, pkg)
        try {
            startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Önce çalışma profilini oluştur", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val ok = resultCode == RESULT_OK
            Toast.makeText(this, if (ok) "Profil hazır" else "Profil oluşturma iptal/başarısız", Toast.LENGTH_LONG).show()
        }
    }
}
