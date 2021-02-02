package common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Security {

    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    public static String encode(String text) {
        byte[] bytes = text.getBytes(CHARSET);
        return ENCODER.encodeToString(bytes);
    }

    public static String decode(String text) {
        byte[] bytes = text.getBytes(CHARSET);
        bytes = DECODER.decode(bytes);
        return new String(bytes, CHARSET);
    }
}
