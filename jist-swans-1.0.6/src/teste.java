import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class teste {
	public static void main(String[] args) {
		try {
			File file = new File("teste.txt");

			PrintWriter pr = new PrintWriter(file);
			for (int i = 0; i < 20; i++) {
			String nome = "fagemr";
			System.err.println(nome);
				pr.println(nome);
			
			}
			pr.flush();
			pr.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
