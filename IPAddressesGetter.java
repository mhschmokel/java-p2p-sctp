import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class IPAddressesGetter {
	public ArrayList<String> getIPsFromFile() {
		ArrayList<String> ipsAddresses = new ArrayList<String>();

		try {
			File file = new File("addresses.txt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line;

			while((line=br.readLine())!= null)
			{  
				ipsAddresses.add(line);
			}  
			
			fr.close();  
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return ipsAddresses;
	}
}
