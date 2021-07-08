package com.lyle.common.lang.signature;

import com.lyle.common.lang.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;

public abstract class BaseSignature implements Signature {

    /**
     * 使用privateKey对原始数据进行签名
     *
     * @param content 原始数据
     * @param privateKey 私钥
     * @param charset 编码集
     * @return 签名数据
     */
    public String sign(String content, String privateKey,
                       String charset) throws SignatureException {
        if (content == null) {
            throw new SignatureException("内容为空!");
        }

        if (StringUtils.isBlank(privateKey)) {
            throw new SignatureException("私钥为空!");
        }

        return doSign(content, privateKey, charset);
    }

    /**
     * 验证签名
     *
     * @param content 原始数据
     * @param signature 签名数据
     * @param publicKey 公钥
     * @param charset 编码集
     * @return True 签名验证通过 False 签名验证失败
     */
    public boolean check(String content, String signature, String publicKey,
                         String charset) throws SignatureException {
        if (content == null) {
            throw new SignatureException("内容为空!");
        }

        if (StringUtils.isBlank(publicKey)) {
            throw new SignatureException("公钥为空!");
        }

        if (StringUtils.isBlank(signature)) {
            throw new SignatureException("签名为空!");
        }

        return doCheck(content, signature, publicKey, charset);
    }

    /**
     * 使用privateKey对原始数据进行签名
     *
     * @param content 原始数据
     * @param privateKey 私钥
     * @param charset 编码集
     * @return 签名数据
     */
    abstract String doSign(String content, String privateKey,
                           String charset) throws SignatureException;

    /**
     * 验证签名
     *
     * @param content 原始数据
     * @param signature 签名数据
     * @param publicKey 公钥
     * @param charset 编码集
     * @return True 签名验证通过 False 签名验证失败
     */
    abstract boolean doCheck(String content, String signature, String publicKey,
                             String charset) throws SignatureException;

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException
     */
    protected byte[] getContentBytes(String content,
                                     String charset) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(charset)) {
            return content.getBytes();
        }

        return content.getBytes(charset);
    }

}
