package com.lyle.common.lang.signature;

import com.lyle.common.lang.util.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;

public class MD5Signature extends BaseSignature {

    /**
     * 使用privateKey对原始数据进行签名
     *
     * @param content 原始数据
     * @param privateKey 私钥
     * @param charset 编码集
     * @return 签名数据
     */
    String doSign(String content, String privateKey, String charset) throws SignatureException {
        String tosign = (content == null ? "" : content) + privateKey;

        try {
            return DigestUtils.md5Hex(getContentBytes(tosign, charset));
        } catch (UnsupportedEncodingException e) {
            throw new SignatureException(
                "MD5签名[content = " + content + "; charset = " + charset + "]发生异常!", e);
        }
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
    boolean doCheck(String content, String signature, String publicKey,
                    String charset) throws SignatureException {
        String tosign = (content == null ? "" : content) + publicKey;

        try {
            String mySign = DigestUtils.md5Hex(getContentBytes(tosign, charset));

            return StringUtils.equals(mySign, signature) ? true : false;
        } catch (UnsupportedEncodingException e) {
            throw new SignatureException("MD5验证签名[content = " + content + "; charset = " + charset
                                         + "; signature = " + signature + "]发生异常!",
                e);
        }
    }

}
