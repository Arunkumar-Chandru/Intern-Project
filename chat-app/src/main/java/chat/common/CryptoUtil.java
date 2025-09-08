package chat.common;

public class CryptoUtil {
    public static String encrypt(String text, String key) {
        return xor(text, key);
    }

    public static String decrypt(String text, String key) {
        return xor(text, key);
    }

    private static String xor(String text, String key) {
        char[] result = new char[text.length()];
        for (int i = 0; i < text.length(); i++) {
            result[i] = (char) (text.charAt(i) ^ key.charAt(i % key.length()));
        }
        return new String(result);
    }
}