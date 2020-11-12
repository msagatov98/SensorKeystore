package com.example.sensorkeystore

import java.util.*
import javax.crypto.Cipher
import java.math.BigInteger
import java.security.KeyStore
import android.content.Context
import java.security.KeyPairGenerator
import android.security.KeyPairGeneratorSpec
import javax.security.auth.x500.X500Principal

class CryptographyRSA(context: Context): Cryptography() {

    private var mKeyStore: KeyStore = KeyStore.getInstance(PROVIDER)
    private val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"

    init {

        mKeyStore.load(null)

        val start = Calendar.getInstance()
        val end = Calendar.getInstance()

        end.add(Calendar.YEAR, 1)

        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(ALIAS)
            .setSubject(X500Principal("CN=$ALIAS"))
            .setSerialNumber(BigInteger.ONE)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        val keyPairGenerator = KeyPairGenerator.getInstance("RSA", PROVIDER)
        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
    }

    override fun encryptData(data: String): Pair<ByteArray, ByteArray> {

        var temp = data

        while (temp.toCharArray().size % 16 != 0)
            temp += "\u0020"

        val privateKeyEntry = mKeyStore.getEntry(ALIAS, null) as KeyStore.PrivateKeyEntry
        val publicKey = privateKeyEntry.certificate.publicKey

        val cipher = Cipher.getInstance(RSA_TRANSFORMATION, "AndroidOpenSSL")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encryptedBytes = cipher.doFinal(temp.toByteArray(Charsets.UTF_8))

        return Pair(temp.toByteArray(Charsets.UTF_8), encryptedBytes)
    }

    override fun decryptData(ivBytes: ByteArray, data: ByteArray): String {

        val privateKeyEntry = mKeyStore.getEntry(ALIAS, null) as KeyStore.PrivateKeyEntry
        val privateKey = privateKeyEntry.privateKey

        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        return cipher.doFinal(data).toString(Charsets.UTF_8).trim()
    }
}