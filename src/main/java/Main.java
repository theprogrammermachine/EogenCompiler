import tra.v3.TraLexer;
import tra.models.Parser;

import java.io.*;

public class Main {

    public static void main(String[] args) {
        try {
            File file = new File("D:\\My Documents\\Projects\\Java\\ECompiler\\src\\main\\java\\tra\\sample.eogen");
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
            Parser parser = new Parser(lexer);
            System.out.println(((byte[])parser.parse()).length);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
