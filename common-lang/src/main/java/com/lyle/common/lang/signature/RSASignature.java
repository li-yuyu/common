package com.lyle.common.lang.signature;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;

public class RSASignature extends BaseSignature {

    public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    /**
     * 验证签名
     *
     * @param content 原始数据
     * @param sign 签名数据
     * @param publicKey 公钥
     * @param charset 编码集
     * @return True 签名验证通过 False 签名验证失败
     */
    boolean doCheck(String content, String sign, String publicKey,
                    String charset) throws SignatureException {
        try {
            PublicKey pubKey = KeyReader.getPublicKeyFromX509("RSA",
                new ByteArrayInputStream(publicKey.getBytes()));

            java.security.Signature signature = java.security.Signature
                .getInstance(SIGN_ALGORITHMS);

            signature.initVerify(pubKey);
            signature.update(getContentBytes(content, charset));

            return signature.verify(Base64.decodeBase64(sign.getBytes()));
        } catch (Exception e) {
            throw new SignatureException("RSA验证签名[content = " + content + "; charset = " + charset
                                         + "; signature = " + sign + "]发生异常!",
                e);
        }
    }

    /**
     * 使用privateKey对原始数据进行签名
     *
     * @param content 原始数据
     * @param privateKey 私钥
     * @param charset 编码集
     * @return 签名数据
     */
    String doSign(String content, String privateKey, String charset) throws SignatureException {
        try {
            PrivateKey priKey = KeyReader.getPrivateKeyFromPKCS8("RSA",
                new ByteArrayInputStream(privateKey.getBytes()));

            java.security.Signature signature = java.security.Signature
                .getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update(getContentBytes(content, charset));

            byte[] signed = signature.sign();

            return new String(Base64.encodeBase64(signed));
        } catch (Exception e) {
            throw new SignatureException(
                "RSA签名[content = " + content + "; charset = " + charset + "]发生异常!", e);
        }
    }
}
