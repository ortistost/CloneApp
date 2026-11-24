package com.chavix.cloneapp

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class CloneAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val dpm = context.getSystemService(DevicePolicyManager::class.java)
        val admin = getComponentName(context)
        dpm.setProfileName(admin, "Clone")
        val filter = IntentFilter(Const.ACTION_CLONE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        dpm.addCrossProfileIntentFilter(admin, filter, DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED)
        dpm.setProfileEnabled(admin)
    }
}
