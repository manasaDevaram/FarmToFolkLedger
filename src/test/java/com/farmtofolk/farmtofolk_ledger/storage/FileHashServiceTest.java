package com.farmtofolk.farmtofolk_ledger.storage;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileHashServiceTest {

    @Test
    void hashesActualFileBytesAsLowercaseSha256() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.txt", "text/plain", "farm-to-folk".getBytes()
        );

        String hash = new FileHashService().sha256Hex(file);

        assertEquals("df85ffafdbe01838fbeb8ac8f86bd9cec40a528c488c451ce44ef29a4c7a513e", hash);
    }
}
