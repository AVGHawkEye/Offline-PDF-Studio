package com.example.model

enum class ProtectMode(val title: String, val description: String) {
    ENCRYPT("Protect Document", "Set a password to encrypt this PDF with AES encryption"),
    DECRYPT("Unlock Document", "Provide the existing password to remove encryption")
}
