package net.cosmoway.sesame

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordUtil {

    /** パスワードを安全にするためのアルゴリズム  */
    private val ALGORITHM = "PBKDF2WithHmacSHA256"
    /** ストレッチング回数  */
    private val ITERATION_COUNT = 10000
    /** 生成される鍵の長さ  */
    private val KEY_LENGTH = 256

    /**
     * 　平文のパスワードとソルトから安全なパスワードを生成し返却。

     * @param password 平文のパスワード
     * *
     * @param salt ソルト
     * *
     * @return 安全なパスワード
     */
    fun getSafetyPassword(password: String, salt: String): String {

        val passCharAry = password.toCharArray()
        val hashedSalt = getHashedSalt(salt)

        val keySpec = PBEKeySpec(passCharAry, hashedSalt, ITERATION_COUNT, KEY_LENGTH)

        val skf: SecretKeyFactory
        try {
            skf = SecretKeyFactory.getInstance(ALGORITHM)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }

        val secretKey: SecretKey
        try {
            secretKey = skf.generateSecret(keySpec)
        } catch (e: InvalidKeySpecException) {
            throw RuntimeException(e)
        }

        val passByteAry = secretKey.encoded

        // 生成されたバイト配列を16進数の文字列に変換
        val sb = StringBuilder(64)
        for (b in passByteAry) {
            sb.append("%02x".format(b.toInt() and 0xff))
        }
        return sb.toString()
    }

    /**
     * ソルトをハッシュ化して返却。
     * ※ハッシュアルゴリズムはSHA-256を使用。

     * @param salt ソルト
     * *
     * @return ハッシュ化されたバイト配列のソルト
     */
    private fun getHashedSalt(salt: String): ByteArray {
        val messageDigest: MessageDigest
        try {
            messageDigest = MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }

        messageDigest.update(salt.toByteArray())
        return messageDigest.digest()
    }
}
