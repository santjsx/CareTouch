package com.example.amma.cloud.r2

/**
 * Cloudflare R2 Storage Configuration
 *
 * S3-Compatible, $0 egress fees object storage for CareTouch Contact Photos.
 */
data class R2Config(
    val accountId: String = "",
    val accessKeyId: String = "",
    val secretAccessKey: String = "",
    val bucketName: String = "caretouch-contacts",
    val publicDomain: String = "" // e.g. "https://pub-xxxx.r2.dev" or custom domain "https://media.caretouch.app"
) {
    val endpoint: String
        get() = if (accountId.isNotBlank()) "https://$accountId.r2.cloudflarestorage.com" else ""

    val isConfigured: Boolean
        get() = accountId.isNotBlank() && accessKeyId.isNotBlank() && secretAccessKey.isNotBlank() && bucketName.isNotBlank()
}
