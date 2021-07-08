package com.lyle.common.lang.signature;

import com.lyle.common.lang.io.ByteArrayOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;

public class DSADotNetSignature extends com.lyle.common.lang.signature.BaseSignature {

    private static final Logger logger = LoggerFactory
            .getLogger(com.lyle.common.lang.signature.DSADotNetSignature.class);

    /**
     * DSA算法名
     */
    private static final String DSA_ALGORITHM_NAME = "DSA";

    private static final int START_TAG = 0x30;

    private static final int SEP_TAG = 0x02;

    /**
     * 将base64中的大写字母escape的char。
     */
    char BASE64_ESCAPE_CHAR = '_';

    /**
     * 使用privateKey对原始数据进行签名
     *
     * @param content    原始数据
     * @param privateKey 私钥
     * @param charset    编码集
     * @return 签名数据
     */
    String doSign(String content, String privateKey, String charset) throws SignatureException {
        try {
            PrivateKey prikey = KeyReader.getPrivateKeyFromPKCS8("DSA",
                    new ByteArrayInputStream(privateKey.getBytes()));

            java.security.Signature signature = java.security.Signature
                    .getInstance(DSA_ALGORITHM_NAME);

            signature.initSign(prikey);
            signature.update(getContentBytes(content, charset));

            byte[] signed = signature.sign();
            byte[] signedRS = signatureToRS(signed);

            if (logger.isDebugEnabled()) {
                logger.debug("Java signature[length=" + signed.length + "]: " + dumpBytes(signed));
                logger.debug(
                        "  in RS format[length=" + signedRS.length + "]: " + dumpBytes(signedRS));
            }

            byte[] bf = Base64.encodeBase64(signedRS, false);

            return encodeUpperCase(bf);
        } catch (Exception e) {
            throw new SignatureException(
                    "DSA签名[content = " + content + "; charset = " + charset + "]发生异常！", e);
        }
    }

    /**
     * 验证签名
     *
     * @param content   原始数据
     * @param sign      签名数据
     * @param publicKey 公钥
     * @param charset   编码集
     * @return True 签名验证通过 False 签名验证失败
     */
    boolean doCheck(String content, String sign, String publicKey,
                    String charset) throws SignatureException {
        try {
            PublicKey pubKey = KeyReader.getPublicKeyFromX509("DSA",
                    new ByteArrayInputStream(publicKey.getBytes()));

            byte[] signedRS = Base64.decodeBase64(decodeUpperCase(sign));
            byte[] signBytes = rsToSignature(signedRS);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Java signature[length=" + signBytes.length + "]: " + dumpBytes(signBytes));
                logger.debug(
                        "  in RS format[length=" + signedRS.length + "]: " + dumpBytes(signedRS));
            }

            java.security.Signature signature = java.security.Signature
                    .getInstance(DSA_ALGORITHM_NAME);

            signature.initVerify(pubKey);
            signature.update(getContentBytes(content, charset));

            return signature.verify(signBytes);
        } catch (Exception e) {
            throw new SignatureException("DSA验证签名[content = " + content + "; charset = " + charset
                    + "; signature = " + sign + "]发生异常！",
                    e);
        }
    }

    /**
     * 将固定的40字节长的格式转换为Sun provider提供的signature的格式。
     */
    private byte[] rsToSignature(byte[] rs) throws SignatureException {
        if ((rs == null) || (rs.length != 40)) {
            throw new SignatureException("Invalid signature format");
        }

        int length = 46;
        int offsetR = 4;
        int lengthR = 20;

        if ((rs[0] & 0x80) != 0) {
            length++;
            offsetR++;
            lengthR++;
        }

        int offsetS = offsetR + 22;
        int lengthS = 20;

        if ((rs[20] & 0x80) != 0) {
            length++;
            offsetS++;
            lengthS++;
        }

        byte[] signature = new byte[length];

        signature[0] = START_TAG;
        signature[1] = (byte) (length - 2);
        signature[2] = SEP_TAG;
        signature[3] = (byte) lengthR;
        System.arraycopy(rs, 0, signature, offsetR, 20);
        signature[offsetR + 20] = SEP_TAG;
        signature[offsetR + 21] = (byte) lengthS;
        System.arraycopy(rs, 20, signature, offsetS, 20);

        return signature;
    }

    /**
     * 将Sun provider提供的signature的格式转换为固定的40字节长的格式。
     */
    private byte[] signatureToRS(byte[] signature) throws SignatureException {
        if ((signature == null) || (signature[0] != START_TAG)) {
            throw new SignatureException("Invalid signature format");
        }

        byte[] rs = new byte[40];

        // offsetR - 原始signature中R值的起始位移
        // lengthR - 原始signature中R值的长度
        // startR  - 目标signature中R值的起始位移
        int offsetR = 4;
        int lengthR = signature[offsetR - 1];
        int startR = 0;

        if (signature[offsetR - 2] != SEP_TAG) {
            throw new SignatureException("Invalid signature format");
        }

        if (lengthR > 20) {
            offsetR += (lengthR - 20);
            lengthR = 20;
        } else if (lengthR < 20) {
            startR += (20 - lengthR);
        }

        // offsetS - 原始signature中S值的起始位移
        // lengthS - 原始signature中S值的长度
        // startS  - 目标signature中S值的起始位移
        int offsetS = signature[3] + 6;
        int lengthS = signature[offsetS - 1];
        int startS = 20;

        if (signature[offsetS - 2] != SEP_TAG) {
            throw new SignatureException("Invalid signature format");
        }

        if (lengthS > 20) {
            offsetS += (lengthS - 20);
            lengthS = 20;
        } else if (lengthS < 20) {
            startS += (20 - lengthS);
        }

        System.arraycopy(signature, offsetR, rs, startR, lengthR);
        System.arraycopy(signature, offsetS, rs, startS, lengthS);

        return rs;
    }

    /**
     * 将"_"+小写字母转换成大写字母。
     */
    private byte[] decodeUpperCase(String str) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(str.length());

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            try {
                if ((ch == BASE64_ESCAPE_CHAR) && (i < (str.length() - 1))) {
                    char nextChar = Character.toUpperCase(str.charAt(++i));

                    baos.write((int) nextChar);
                } else {
                    baos.write((int) ch);
                }
            } catch (IOException e) {
            }
        }

        baos.close();

        return baos.toByteArray().toByteArray();
    }

    /**
     * 将大写字母转换成"_"+小写字母。
     */
    private String encodeUpperCase(byte[] bytes) {
        StringBuffer buffer = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            char ch = (char) bytes[i];

            if (Character.isUpperCase(ch)) {
                buffer.append(BASE64_ESCAPE_CHAR).append(Character.toLowerCase(ch));
            } else {
                buffer.append(ch);
            }
        }

        return buffer.toString();
    }

    /**
     * 将字节显示出来。
     */
    private String dumpBytes(byte[] bytes) {
        StringBuffer buffer = new StringBuffer(bytes.length * 3);

        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            String hex = Integer.toHexString(value).toUpperCase();

            if (hex.length() < 2) {
                buffer.append('0');
            }

            buffer.append(hex).append(' ');
        }

        return buffer.toString();
    }
}
