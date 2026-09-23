package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import java.security.MessageDigest

object AppSignatureHelper {

    fun getSha1Fingerprint(context: Context): String {
        return try {
            val certBytes = getCertificateBytes(context) ?: return ""
            val md = MessageDigest.getInstance("SHA-1")
            val digest = md.digest(certBytes)
            digest.joinToString(":") { "%02X".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    fun getKeyHashBase64(context: Context): String {
        return try {
            val certBytes = getCertificateBytes(context) ?: return ""
            val md = MessageDigest.getInstance("SHA-1")
            val digest = md.digest(certBytes)
            Base64.encodeToString(digest, Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    private fun getCertificateBytes(context: Context): ByteArray? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            signatures?.firstOrNull()?.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}
