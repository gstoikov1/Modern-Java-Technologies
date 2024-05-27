package bg.sofia.uni.fmi.mjt.space.algorithm;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class RijndaelTest {
    @Test
    void decryptInvalidKey() throws CipherException {
        Rijndael rijndael = new Rijndael(null);
        assertThrows(CipherException.class,
            () -> rijndael.encrypt(new ByteArrayInputStream("test".getBytes()), new ByteArrayOutputStream(100)),
            "CipherException has to be throws should any error occur during decryption");
    }

    @Test
    void encryptInvalidKey() throws CipherException {
        Rijndael rijndael = new Rijndael(null);
        assertThrows(CipherException.class,
            () -> rijndael.encrypt(new ByteArrayInputStream("test".getBytes()), new ByteArrayOutputStream(100)),
            "CipherException has to be throws should any error occur during encryption");
    }
}
