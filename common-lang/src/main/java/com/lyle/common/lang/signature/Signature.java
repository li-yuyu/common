package com.lyle.common.lang.signature;

import java.security.SignatureException;

public interface Signature {

    /**
     * 使用privateKey对原始数据进行签名
     *
     * @param content 原始数据
     * @param privateKey 私钥
     * @param charset 编码集
     * @return 签名数据
     */
    String sign(String content, String privateKey, String charset) throws SignatureException;

    /**
     * 验证签名
     *
     * @param content 原始数据
     * @param signature 签名数据
     * @param publicKey 公钥
     * @param charset 编码集
     * @return True 签名验证通过 False 签名验证失败
     */
    boolean check(String content, String signature, String publicKey,
                  String charset) throws SignatureException;
}
