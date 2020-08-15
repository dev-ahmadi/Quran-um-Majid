package id.ahmadiyah.quran.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import id.ahmadiyah.quran.infrastucure.DatabaseHelper


class ActivitySplash : AppCompatActivity() {

    companion object {
        private const val WAIT_SPLASH = 1700L
    }
    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(id.ahmadiyah.quran.R.layout.activity_splash)

        initRemoteConfig()

        //initialize database
        DatabaseHelper.getInstance(applicationContext).readableDatabase
    }

    private fun initRemoteConfig() {
        Log.d("CONFIG", "initRemoteConfig()")

        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    private fun getVersionCode(): Long {
        return packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
    }

    override fun onResume() {
        super.onResume()

        val versionCode = getVersionCode()
        remoteConfig.setDefaultsAsync(mapOf("minimumVersion" to versionCode))
        remoteConfig.fetchAndActivate()
        Handler().postDelayed({
            try {
                Log.d("CONFIG", "minimumVersion ${remoteConfig.getLong("minimumVersion")}")
                if (remoteConfig.getLong("minimumVersion") <= versionCode) {
                    launchMain()
                } else {
                    showUpdateDialog()
                }
            } catch (ignored: Exception) {
                ignored.printStackTrace()
            }
        }, WAIT_SPLASH)

    }

    private fun showUpdateDialog() {

        val alertDialog: AlertDialog? = let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                builder.setTitle("Update")
                        .setMessage("Aplikasi Al-Quran perlu diperbarui untuk pengalaman yang lebih baik.")
                        .setPositiveButton("Perbarui") { _, _ -> launchUpdate() }
                        .setNegativeButton("Nanti Saja") { _, _ ->  launchMain() }
                        .setOnCancelListener { launchMain() }
            }

            builder.create()
        }

        alertDialog!!.show()
    }

    private fun launchMain() {
        Log.d("CONFIG", "showMain()")

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    private fun launchUpdate() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
        finish()
    }

}

