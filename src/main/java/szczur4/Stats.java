package szczur4;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import java.io.*;
import java.math.BigInteger;
import java.util.zip.*;
public class Stats extends File{
	public static BigInteger flags=BigInteger.ZERO,cleared=BigInteger.ZERO,lost=BigInteger.ZERO;
	public Stats()throws IOException{
		super("world"+separator+"stats.json");
		load();
	}
	public void reset(){
		flags=BigInteger.ZERO;
		cleared=BigInteger.ZERO;
		lost=BigInteger.ZERO;
	}
	private void load()throws IOException{
		if(!exists())return;
		InputStreamReader isr=new InputStreamReader(new GZIPInputStream(new FileInputStream(this)));
		JsonObject root=new Gson().fromJson(isr,JsonObject.class);
		isr.close();
		flags=root.get("mines").getAsBigInteger();
		cleared=root.get("cleared").getAsBigInteger();
		lost=root.get("lost").getAsBigInteger();
	}
	public void save()throws IOException{
		new JsonWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(this)))).beginObject().name("mines").value(flags).name("cleared").value(cleared).name("lost").value(lost).endObject().close();
		System.out.println("Saved "+getName());
	}
}