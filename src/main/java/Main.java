import tra.v3.TraParser;
import tra.v3.TraLexer;

import java.io.*;

public class Main {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main(String[] args) {
        try {
            File file = new File("/home/keyhan/Desktop/Hans Zimmer - The Dark Knight Rises (2012) [www.vmusic.ir]/ECompiler/src/main/java/tra/v3/sample.eogen");
            String line, lastLine = null;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {
                lastLine = line;
            }
            if (lastLine == null || lastLine.length() > 0) {
                FileWriter fw = new FileWriter(file, true);
                fw.append('\n');
                fw.flush();
                fw.close();
            }
            FileReader fr = new FileReader(file);
            TraLexer lexer = new TraLexer(fr);
            TraParser traParser = new TraParser(lexer);
            File output = new File("/home/keyhan/Desktop/Hans Zimmer - The Dark Knight Rises (2012) [www.vmusic.ir]/ECompiler/src/main/java/tra/v3/output.tra");
            if (output.createNewFile()) {
                FileOutputStream stream = new FileOutputStream(output);
                byte[] bytes = (byte[]) traParser.parse();
                stream.write(bytes);
                stream.flush();
                stream.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
