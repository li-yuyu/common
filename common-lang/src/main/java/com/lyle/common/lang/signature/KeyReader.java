package com.lyle.common.lang.signature;

import com.lyle.common.lang.io.StreamUtil;
import com.lyle.common.lang.util.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyReader {

    private static final Logger logger = LoggerFactory.getLogger(KeyReader.class);

    /**
     * 将X509格式的输入流转换成Certificate对象。
     *
     * @param ins
     * @return
     */
    public static Certificate getCertificateFromX509(InputStream ins) {
        try {
            Certificate certificate = CertificateFactory.getInstance("X.509")
                    .generateCertificate(ins);

            return certificate;
        } catch (CertificateException ex) {
            logger.error("获取证书时发生异常：", ex);
            return null;
        }
    }

    /**
     * @param algorithm
     * @param ins
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static PublicKey getPublicKeyFromX509(String algorithm,
                                                 InputStream ins) throws NoSuchAlgorithmException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

            StringWriter writer = new StringWriter();
            StreamUtil.io(new InputStreamReader(ins), writer);

            byte[] encodedKey = writer.toString().getBytes();

            // 先base64解码
            encodedKey = Base64.decodeBase64(encodedKey);

            return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (IOException ex) {
            //不可能发生
            logger.error("获取公钥时发生异常：", ex);
        } catch (InvalidKeySpecException ex) {
            //不可能发生
            logger.error("获取公钥时发生异常：", ex);
        }

        return null;
    }

    /**
     * @param algorithm
     * @param ins
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static PrivateKey getPrivateKeyFromPKCS8(String algorithm,
                                                    InputStream ins) throws NoSuchAlgorithmException {
        if (ins == null || StringUtils.isBlank(algorithm)) {
            return null;
        }

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

            byte[] encodedKey = StreamUtil.readText(ins).getBytes();
            logger.info("KeyReader.encodedKey:" + encodedKey);
            // 先base64解码
            encodedKey = Base64.decodeBase64(encodedKey);
            logger.info("KeyReader.encodedKey2:" + encodedKey);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (IOException ex) {
            //不可能发生
            logger.error("获取私钥时发生异常：", ex);
        } catch (InvalidKeySpecException ex) {
            //不可能发生
            logger.error("获取私钥时发生异常：", ex);
        }

        return null;
    }

}
