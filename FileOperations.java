import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class FileOperations {

	public static void insertHook(){
		ArrayList<String> lines = new ArrayList<String>();
		File settings = new File(System.getenv("LOCALAPPDATA")+"\\deckedbuilder\\deckedbuilder.xml");
		try{
			Scanner scan = new Scanner(settings);
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(!line.contains("OrbCam_match_url")){
					if(line.equals("</config>")){
						lines.add("  <v key=\"OrbCam_match_url\" value=\"http://localhost:7777/{0}\" />");
					}
					lines.add(line);
				}
			}
			scan.close();

			FileOutputStream fos = new FileOutputStream(settings);
			PrintWriter out = new PrintWriter(fos);
			for(String line:lines){
				out.println(line);
			}
			out.flush();
			out.close();

		}catch(FileNotFoundException e){
		}
	}

	public static String getOrbifiedName(String setname){
		return setname.toLowerCase().replace(" ", "_").replace("'", "");
	}

	public static String getSetTag(String setname,String blockname){
		return "<cardset name=\""+setname+
				"\" code=\"CUSTOM\" releasedate=\"2121-11-11\" block=\""+blockname+
				"\" standard=\"NO\" extended=\"NO\" modern=\"NO\" hd=\"NO\" custom=\"YES\"/>";
	}

	public static void updateSetlistFile(){
		File setlist = getSetlistFile();
		HashMap<String,String> sets = new HashMap<String,String>();
		ArrayList<String> lines = new ArrayList<String>();



		try{
			File file = new File(System.getenv("LOCALAPPDATA")+"\\deckedbuilder\\customsets.txt");
			Scanner scan = new Scanner(file);
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				try{
					String setname = line.split("/~/")[0];
					String blockname = line.split("/~/")[1];
					sets.put(setname, blockname);
				}catch(Exception ex){
				}
			}
			scan.close();
		}catch(FileNotFoundException e){
		}

		try{
			Scanner scan = new Scanner(setlist);
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(line.equals("</cardsets>")){
					for(String setname:sets.keySet()){
						String tag = getSetTag(setname,sets.get(setname));
						lines.add("\t"+tag);
					}
				}
				lines.add(line);
				if(line.contains("name=\"")){
					line = line.split("name=\"")[1];
					line = line.substring(0, line.indexOf('"'));
					if(sets.containsKey(line)){
						sets.remove(line);
					}
				}
			}
			scan.close();

			FileOutputStream fos = new FileOutputStream(setlist);
			PrintWriter out = new PrintWriter(fos);
			for(String line:lines){
				out.println(line);
			}
			out.flush();
			out.close();

		}catch(FileNotFoundException e){
		}
	}

	public static File getSetlistFile(){
		String path = System.getenv("LOCALAPPDATA")+"\\deckedbuilder\\dbdir";
		File file = new File(path);
		String[] dirs = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		int max = 0;
		for(String dir:dirs){
			if(dir.startsWith("dbdir")){
				String num = dir.split("-")[1];
				int n = Integer.parseInt(num);
				if(n>max){
					max = n;
				}
			}
		}
		return new File(path+"\\dbdir-"+max+"\\setlist.xml");
	}

	public static void addToCustomSetFile(String setname,String blockname){
		try{
			File file = new File(System.getenv("LOCALAPPDATA")+"\\deckedbuilder\\customsets.txt");
			Scanner scan = new Scanner(file);
			ArrayList<String> lines = new ArrayList<>();
			while(scan.hasNextLine()){
				lines.add(scan.nextLine());
			}
			lines.add(setname+"/~/"+blockname);
			scan.close();
			FileOutputStream fos = new FileOutputStream(file);
			PrintWriter out = new PrintWriter(fos);
			for(String line:lines){
				out.println(line);
			}
			out.flush();
			out.close();
		}catch(FileNotFoundException e){
			File file = new File(System.getenv("LOCALAPPDATA")+"\\deckedbuilder\\customsets.txt");
			try {
				FileOutputStream fos = new FileOutputStream(file);
				PrintWriter out = new PrintWriter(fos);
				out.println(setname+"/~/"+blockname);
				out.flush();
				out.close();
			} catch (FileNotFoundException e1) {
			}
		}
	}

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
				JOptionPane.showMessageDialog(null, "Card not found: "+cardname);
			}

			return m_ids;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static String removeLeadingNumber(String line){
		int lastNum = 0;
		while(lastNum < line.length() && Character.isDigit(line.charAt(lastNum))){
			lastNum++;
		}
		return line.substring(lastNum).trim();
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
			}catch(Exception e){
				cardname = removeLeadingNumber(cardname);
				if(cardname.contains("\t")){
					cardname = cardname.split("\t")[1];
				}

				try{
					Integer.parseInt(cardname);
					m_ids.add(cardname);
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
