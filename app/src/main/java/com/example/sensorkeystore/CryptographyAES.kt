package com.example.sensorkeystore

import android.os.Build
import javax.crypto.Cipher
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.KeyGenerator
import androidx.annotation.RequiresApi
import javax.crypto.spec.IvParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec

@RequiresApi(Build.VERSION_CODES.M)
class CryptographyAES: Cryptography() {

    private val AES_TRANSFORMATION =  "AES/CBC/PKCS7Padding"

    private var keyGenParameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
        ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        .build()


    private var keyGenerator: KeyGenerator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        PROVIDER
    )

    init {
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    override fun encryptData(data: String): Pair<ByteArray, ByteArray> {

        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        var temp = data

        while (temp.toCharArray().size % 16 != 0)
            temp += "\u0020"

        cipher.init(Cipher.ENCRYPT_MODE, getKey())


        val ivBytes = cipher.iv
        val encryptedBytes = cipher.doFinal(temp.toByteArray(Charsets.UTF_8))

        return Pair(ivBytes, encryptedBytes)
    }

    override fun decryptData(ivBytes: ByteArray, data: ByteArray): String {

        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val spec = IvParameterSpec(ivBytes)

        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

        return cipher.doFinal(data).toString(Charsets.UTF_8).trim()
    }

    private fun getKey(): SecretKey {

        val keyStore = KeyStore.getInstance( PROVIDER)
        keyStore.load(null)

        val secretKeyEntry = keyStore.getEntry(ALIAS, null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey

    }
}