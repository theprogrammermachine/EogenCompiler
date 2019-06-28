import java_cup.runtime.Symbol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Main {

    public static void main(String[] args) {
        try {
            File file = new File("D:\\My Documents\\Projects\\Java\\ECompiler\\src\\main\\java\\phase2_sample_code_new.eogen");
            String line, lastLine = null;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) lastLine = line;
            if (lastLine == null || lastLine.length() > 0) {
                FileWriter fw = new FileWriter(file, true);
                fw.append('\n');
                fw.flush();
                fw.close();
            }
            FileReader fr = new FileReader(file);
            EogenLexer lexer = new EogenLexer(fr);
            EogenParser parser = new EogenParser(lexer);
            parser.parse();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
