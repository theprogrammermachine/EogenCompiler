import java_cup.runtime.Symbol;

import java.io.File;
import java.io.FileReader;

public class Main {

    public static void main(String[] args) {
        try {
            File file = new File("D:\\My Documents\\Projects\\Java\\ECompiler\\src\\phase2_sample_code.eogen");
            FileReader fis = new FileReader(file);

            EogenLexer lexer = new EogenLexer(fis);
            EogenParser parser = new EogenParser(lexer);
            parser.parse();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
