package filefunctions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for FileFunctions.
 */
public class FileFunctionsTest {

    @TempDir
    Path tempDir;

    // --- readFileFullyIntoBuffer ---

    @Test
    public void readFileFullyIntoBuffer_readsKnownBytes() throws IOException {
        byte[] expected = {0x01, 0x02, 0x03, 0x04, 0x05};
        File f = tempDir.resolve("test.bin").toFile();
        writeBytesToFile(f, expected);

        byte[] actual = FileFunctions.readFileFullyIntoBuffer(f.getAbsolutePath());
        assertArrayEquals(expected, actual);
    }

    @Test
    public void readFileFullyIntoBuffer_readsSingleByte() throws IOException {
        byte[] expected = {0x7F};
        File f = tempDir.resolve("single.bin").toFile();
        writeBytesToFile(f, expected);

        assertArrayEquals(expected, FileFunctions.readFileFullyIntoBuffer(f.getAbsolutePath()));
    }

    @Test
    public void readFileFullyIntoBuffer_readsEmptyFile() throws IOException {
        File f = tempDir.resolve("empty.bin").toFile();
        writeBytesToFile(f, new byte[0]);

        byte[] actual = FileFunctions.readFileFullyIntoBuffer(f.getAbsolutePath());
        assertArrayEquals(new byte[0], actual);
    }

    @Test
    public void readFileFullyIntoBuffer_throwsOnMissingFile() {
        assertThrows(FileNotFoundException.class,
                () -> FileFunctions.readFileFullyIntoBuffer(
                        tempDir.resolve("nonexistent.bin").toAbsolutePath().toString()));
    }

    @Test
    public void readFileFullyIntoBuffer_reads256ByteFile() throws IOException {
        byte[] expected = new byte[256];
        for (int i = 0; i < 256; i++) expected[i] = (byte)(i & 0xFF);
        File f = tempDir.resolve("256bytes.bin").toFile();
        writeBytesToFile(f, expected);

        assertArrayEquals(expected, FileFunctions.readFileFullyIntoBuffer(f.getAbsolutePath()));
    }

    // --- readFullyIntoBuffer ---

    @Test
    public void readFullyIntoBuffer_readsFromByteArrayStream() throws IOException {
        byte[] data = {0x10, 0x20, 0x30};
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        byte[] result = FileFunctions.readFullyIntoBuffer(bais, 3);
        assertArrayEquals(data, result);
    }

    @Test
    public void readFullyIntoBuffer_readsExactBytes() throws IOException {
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        // Only read 3 bytes
        byte[] result = FileFunctions.readFullyIntoBuffer(bais, 3);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, result);
    }

    // --- writeBytesToFile ---

    @Test
    public void writeBytesToFile_writesAndReadBack() throws IOException {
        byte[] data = {(byte)0xAA, (byte)0xBB, (byte)0xCC};
        File f = tempDir.resolve("write.bin").toFile();
        FileFunctions.writeBytesToFile(f.getAbsolutePath(), data);
        assertArrayEquals(data, FileFunctions.readFileFullyIntoBuffer(f.getAbsolutePath()));
    }

    @Test
    public void writeBytesToFile_overwritesExistingFile() throws IOException {
        File f = tempDir.resolve("overwrite.bin").toFile();
        FileFunctions.writeBytesToFile(f.getAbsolutePath(), new byte[]{0x01, 0x02});
        FileFunctions.writeBytesToFile(f.getAbsolutePath(), new byte[]{0x03});
        byte[] result = FileFunctions.readFileFullyIntoBuffer(f.getAbsolutePath());
        assertArrayEquals(new byte[]{0x03}, result);
    }

    // --- helper ---

    private void writeBytesToFile(File f, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(data);
        }
    }
}
