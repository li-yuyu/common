package com.lyle.common.lang.signature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

public class SignatureManager {

    private static final Logger logger = LoggerFactory
            .getLogger(SignatureManager.class);

    /**
     * 签名器
     */
    private static Map<String, Signature> signatures = new HashMap();

    /**
     * 默认字符集
     */
    private static final String DEFAULT_CHARSET = "GB2312";

    static {
        signatures.put("MD5", new MD5Signature());
        signatures.put("RSA", new RSASignature());
        signatures.put("DSA", new DSASignature());
        signatures.put("DSA.NET", new DSADotNetSignature());
    }

    /**
     * 签名
     *
     * @param algorithm
     * @param content
     * @param privateKey
     * @param charset
     * @return
     * @throws SignatureException
     */
    public static String sign(String algorithm, String content, String privateKey,
                              String charset) throws SignatureException {
        Signature signature = getSignature(algorithm);

        if (signature == null) {
            throw new SignatureException("找不到[algorithm=" + algorithm + "]签名校验器");
        }

        String sign = signature.sign(content, privateKey,
                charset == null ? DEFAULT_CHARSET : charset);

        logger.info("签名[algorithm=" + algorithm + "; content=" + content + "; charset=" + charset
                + "]结果：" + sign);
        return sign;
    }

    /**
     * 验签
     *
     * @param algorithm
     * @param content
     * @param sign
     * @param publicKey
     * @param charset
     * @return
     * @throws SignatureException
     */
    public static boolean check(String algorithm, String content, String sign, String publicKey,
                                String charset) throws SignatureException {
        Signature signature = getSignature(algorithm);

        if (signature == null) {
            throw new SignatureException("找不到[algorithm=" + algorithm + "]签名校验器");
        }

        boolean verify = signature.check(content, sign, publicKey,
                charset == null ? DEFAULT_CHARSET : charset);

        logger.info("验证签名[algorithm=" + algorithm + "; content=" + content + "; charset=" + charset
                + "; sign=" + sign + "]结果：" + (verify ? "通过" : "失败"));

        return verify;
    }

    /**
     * 添加signature
     *
     * @param algorithm
     * @param signature
     */
    public static void addSignature(String algorithm, Signature signature) {
        signatures.put(algorithm, signature);
    }

    /**
     * 根据注册名称获得签名工具
     *
     * @param algorithm
     * @return
     */
    private static Signature getSignature(String algorithm) {
        return signatures.get(algorithm);
    }
}
