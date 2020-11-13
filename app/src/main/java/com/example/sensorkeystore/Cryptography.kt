package com.example.sensorkeystore

abstract class Cryptography {

    protected val ALIAS = "Key"
    protected val PROVIDER = "AndroidKeyStore"

    abstract fun encryptData(data: String) : Pair<ByteArray, ByteArray>

    abstract fun decryptData(ivBytes: ByteArray, data: ByteArray): String

}