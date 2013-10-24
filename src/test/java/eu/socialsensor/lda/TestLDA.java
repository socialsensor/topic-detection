package eu.socialsensor.lda;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import eu.socialsensor.framework.common.domain.Item;

public class TestLDA
{
	/**
	 * Tester for the LDA class
	 * @param args no args required
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		System.out.println("Reading input");
		BufferedReader reader = new BufferedReader(new FileReader("./resources/docs-test"));
		String line = reader.readLine();
		ArrayList<Item> documents = new ArrayList<Item>();
		while (line!=null)
		{
			Item item = new Item();
			item.setText(line);
			line = reader.readLine();
			documents.add(item);
		}
		reader.close();
		
		System.out.println("Computing LDA");
		LDA lda = new LDA();
		List<LDATopic> topics = lda.run(documents,10,10,10);
		for (LDATopic t : topics)
		{
			System.out.println(t);
		}
		System.out.println("Done!");
	}
}
