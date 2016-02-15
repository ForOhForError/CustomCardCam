import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class FileOperations {

	public static ArrayList<String> getMultiIds(String cardname){
		try{

			ArrayList<String> m_ids = new ArrayList<>();

			String escapedName = URLEncoder.encode(cardname, "UTF-8");

			URL oracle = new URL("http://api.deckbrew.com/mtg/cards?name="+escapedName);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(oracle.openStream()));

			String inputLine;
			String foundName = "";
			while ((inputLine = in.readLine()) != null){
				String cleanline = new String(inputLine.getBytes(),"UTF-8").trim();
				if(cleanline.startsWith("\"name\"")){
					int l = cleanline.length();
					foundName = cleanline.substring(cleanline.indexOf(":")+3, l-2);
				}else if(cleanline.startsWith("\"multiverse_id\"")){
					if(foundName.trim().equalsIgnoreCase(cardname.trim())){
						String num = cleanline.substring(cleanline.indexOf(":")+2,cleanline.indexOf(","));
						if(!num.equals("0")){
							m_ids.add(num);
						}
					}
				}
			}
			in.close();

			if(m_ids.isEmpty()){
				System.out.println("Card not found: "+cardname);
			}

			return m_ids;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static ArrayList<String> decklistToIds(String decklist){
		ArrayList<String> m_ids = new ArrayList<>();
		ArrayList<String> added = new ArrayList<>();
		ArrayList<String> ignore = new ArrayList<>();

		ignore.add("plains");
		ignore.add("island");
		ignore.add("swamp");
		ignore.add("mountain");
		ignore.add("forest");

		for(String cardname:decklist.split("\n")){
			cardname = cardname.trim();
			
			if(cardname.startsWith("SB:	")){
				cardname = cardname.replace("SB: ", "");
			}
			
			try{
				Integer.parseInt(cardname);
				m_ids.add(cardname);
				System.out.println(cardname);
			}catch(Exception e){
				if(cardname.contains("\t")){
					cardname = cardname.split("\t")[1];
				}
				
				try{
					Integer.parseInt(cardname);
					m_ids.add(cardname);
					System.out.println(cardname);
				}catch(Exception ex){}
				
				if(!added.contains(cardname)){
					added.add(cardname);
					if(!ignore.contains(cardname.toLowerCase())){
						m_ids.addAll(getMultiIds(cardname));
					}
				}
			}
		}
		return m_ids;
	}

	private static int readIntLittle(byte[] bytes, int start){	
		int res = ((bytes[start+3] & 0xff) << 24) | ((bytes[start+2] & 0xff) << 16) |
				((bytes[start+1] & 0xff) << 8)  | (bytes[start+0] & 0xff);
		return res;
	}

	private static byte[] writeIntLittle(int a){
		byte[] bytes = new byte[4];

		bytes[3] = (byte) ((a >> 24) & 0xFF);
		bytes[2] = (byte) ((a >> 16) & 0xFF);
		bytes[1] = (byte) ((a >> 8) & 0xFF);
		bytes[0] = (byte) (a & 0xFF);

		return bytes;
	}

	private static byte[] loadFile(String filename)
	{
		try{
			byte[] data = Files.readAllBytes(Paths.get(filename));
			return data;
		}catch(IOException e){
			return new byte[0];
		}
	}

	private static void saveFile(byte[] sav,String strFilePath){
		try{
			FileOutputStream fos = new FileOutputStream(strFilePath);
			fos.write(sav);
			fos.close();
		}catch(Exception e){e.printStackTrace();}
	}

	public static void readOrb(String filename){
		byte[] datbig = loadFile(filename);
		int loc = 0;
		ArrayList<String> multiids = new ArrayList<>();
		ArrayList<Integer> indexes = new ArrayList<>();
		int size = readIntLittle(datbig,loc);
		loc+=4;
		for(int loop=0;loop<size;loop++){
			String multiid = "";
			int multilen = readIntLittle(datbig,loc);
			loc+=4;

			for(int j=0;j<multilen;j++){
				multiid += (char) (datbig[loc] & 0xFF);
				loc++;
			}
			multiids.add(multiid);
		}	
		loc+=4;
		for(int loop=0;loop<size;loop++){
			indexes.add(loc+1);
			loc+=12;
			loc+=readIntLittle(datbig,loc);
			loc+=4;
		}
		
		indexes.add(datbig.length+1);
		
		for(int i=0;i<indexes.size()-1;i++){
			String orbname = "./Cards/"+multiids.get(i)+".cardorb";
			byte[] subarr = Arrays.copyOfRange(datbig, indexes.get(i)-1, indexes.get(i+1)-1);
			saveFile(subarr,orbname);
		}
	}

	public static void writeSet(ArrayList<String> m_ids,String filename){
		FileOutputStream fos = null;

		ArrayList<String> check_ids = m_ids;
		m_ids = new ArrayList<>();

		for(String m_id:check_ids){
			File f = new File("./Cards/"+m_id+".cardorb");
			if(f.exists() && !f.isDirectory()) { 
				m_ids.add(m_id);
			}
		}

		try{
			fos = new FileOutputStream(filename);
		}catch(Exception e){
			e.printStackTrace();
		}

		try{
			byte[] sizebytes = writeIntLittle(m_ids.size());
			fos.write(sizebytes);
			for(String m_id:m_ids){
				fos.write(writeIntLittle(m_id.length()));
				fos.write(m_id.getBytes());
			}
			fos.write(sizebytes);
			for(String m_id:m_ids){
				fos.write(loadFile("./Cards/"+m_id+".cardorb"));
			}
			fos.flush();
			fos.close();

		}catch(IOException e){
			e.printStackTrace();
			return;
		}
	}


}
