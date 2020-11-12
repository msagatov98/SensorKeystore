package com.example.sensorkeystore

abstract class Cryptography {

    val ALIAS = "Key"
    val PROVIDER = "AndroidKeyStore"

    abstract fun encryptData(data: String) : Pair<ByteArray, ByteArray>

    abstract fun decryptData(ivBytes: ByteArray, data: ByteArray): String

}