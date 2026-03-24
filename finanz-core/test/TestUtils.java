package test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class TestUtils {

    private TestUtils() {
    }

    public static String tempCsvPath() {
        String fileName = "finanzapp-test-" + UUID.randomUUID() + ".csv";
        return System.getProperty("java.io.tmpdir") + File.separator + fileName;
    }

    public static void deleteIfExists(String path) {
        File file = new File(path);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("No se pudo eliminar archivo temporal: " + path);
        }
    }

    public static void touchFile(String path) {
        try {
            File file = new File(path);
            if (!file.createNewFile()) {
                throw new RuntimeException("No se pudo crear archivo temporal: " + path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creando archivo temporal: " + path, e);
        }
    }
}
