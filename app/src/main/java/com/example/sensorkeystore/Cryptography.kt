package com.example.sensorkeystore

import android.os.Build
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class Cryptography {
    companion object {

        val IV_PARAM = byteArrayOf(
            0x00, 0x01, 0x02, 0x03,
            0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x0A, 0x0B,
            0x0C, 0x0D, 0x0E, 0x0F
        )
        private val AES_TRANSFORMATION =  "AES/CBC/PKCS7Padding"

        private val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"


        fun encryptData(data: String) : Pair<ByteArray, ByteArray> {

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                encryptDataAES(data)
            else
                encryptDataRSA(data)
        }

        fun decryptData(ivBytes: ByteArray, data: ByteArray): String {

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                decryptDataAES(ivBytes, data)
            else
                decryptDataRSA(ivBytes, data)
        }

        private fun encryptDataRSA(data: String) : Pair<ByteArray, ByteArray> {

            val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
            var temp = data

            while (temp.toCharArray().size % 16 != 0)
                temp += "\u0020"

            cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey())

            val ivBytes = IvParameterSpec(IV_PARAM).iv
            val encryptedBytes = cipher.doFinal(temp.toByteArray(Charsets.UTF_8))

            return Pair(ivBytes, encryptedBytes)
        }

        private fun encryptDataAES(data: String) : Pair<ByteArray, ByteArray> {

            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            var temp = data

            while (temp.toCharArray().size % 16 != 0)
                temp += "\u0020"

            cipher.init(Cipher.ENCRYPT_MODE, getKey())

            val ivBytes = cipher.iv
            val encryptedBytes = cipher.doFinal(temp.toByteArray(Charsets.UTF_8))

            return Pair(ivBytes, encryptedBytes)
        }


        private fun decryptDataRSA(ivBytes: ByteArray, data: ByteArray): String {

            val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
            val spec = IvParameterSpec(ivBytes)

            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(), spec)

            return cipher.doFinal(data).toString(Charsets.UTF_8).trim()
        }

        private fun decryptDataAES(ivBytes: ByteArray, data: ByteArray): String {

            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val spec = IvParameterSpec(ivBytes)

            cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

            return cipher.doFinal(data).toString(Charsets.UTF_8).trim()
        }

        private fun getKey() : SecretKey {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val secretKeyEntry = keyStore.getEntry("Key", null) as KeyStore.SecretKeyEntry
            return secretKeyEntry.secretKey
        }

        private fun getPrivateKey(): PrivateKey {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val privateKeyEntry = keyStore.getEntry("Key", null) as KeyStore.PrivateKeyEntry
            return privateKeyEntry.privateKey
        }

        private fun getPublicKey(): PublicKey {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val privateKeyEntry = keyStore.getEntry("Key", null) as KeyStore.PrivateKeyEntry
            val privateKey = privateKeyEntry.privateKey

            return keyStore.getCertificate("Key").publicKey
        }
    }
}