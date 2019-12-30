import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.List;


public class CommonUtil {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Hashtable split(String tag, String message, int length) {
		Hashtable newMsg = new Hashtable();

		int position = 0;

		for (int i = 0; i < message.length(); i+= length) {
			newMsg.put(tag+"["+position+"]",message.substring(i, Math.min(i + length, message.length())));
			position += 1;
		}

		return newMsg;
	}
	
	public static String getValueFromList(List<String> list){
		if(list == null || list.size() == 0){
			return "";
		}
		
		StringBuilder values = new StringBuilder();
		for(String _val : list){
			values.append(_val).append("|");
		}
		String value = values.substring(0,values.length()-1);
		return value;
	}
	 
	public static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
}
