package grails.plugins.crm.task.google

import com.google.api.client.googleapis.auth.oauth2.OAuth2Utils
import com.google.api.client.util.PemReader
import com.google.api.client.util.SecurityUtils

import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Created by goran on 2016-02-06.
 */
class GoogleApi {
    String clientId
    String clientSecret
    String serviceName
    String serviceUser
    String clientEmail
    String privateKeyPem
    String privateKeyId

    public PrivateKey getPrivateKey() {
        StringReader reader = new StringReader(privateKeyPem)
        PemReader.Section section = PemReader.readFirstSectionAndClose(reader, "PRIVATE KEY")
        if (section == null) {
            throw new IOException("Invalid PKCS8 data.")
        }
        byte[] bytes = section.getBase64DecodedBytes()
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes)

        KeyFactory exception = SecurityUtils.getRsaKeyFactory()
        PrivateKey privateKey = exception.generatePrivate(keySpec)
        return privateKey
    }
}
