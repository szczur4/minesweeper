package szczur4;
import com.google.gson.*;
import com.google.gson.stream.*;

import java.io.*;
import java.util.zip.*;
public class Region extends File{
	public final Chunk[][]chunks=new Chunk[32][32];
	private final FormattingStyle style=FormattingStyle.COMPACT.withNewline("\n").withIndent("\t");
	final long regX,regY;
	public final Game parent;
	public Region(long regX,long regY,Game parent){
		String path=regX+" "+regY+".region";
		super(new File("world"),path);
		this.regX=regX;
		this.regY=regY;
		this.parent=parent;
	}
	public byte getTile(RegLoc loc){return chunks[loc.chunkX][loc.chunkY].tiles[loc.tileX][loc.tileY];}
	public Chunk getChunk(RegLoc loc){
		for(int x=loc.chunkX-1;x<loc.chunkX+2;x++)for(int y=loc.chunkY-1;y<loc.chunkY+2;y++){
			RegLoc tmp=new RegLoc(regX+(x>>5),regY+(y>>5),x&31,y&31);
			Region reg=parent.getRegion(tmp);
			if(reg.chunks[tmp.chunkX][tmp.chunkY]==null){
				reg.chunks[tmp.chunkX][tmp.chunkY]=new Chunk(tmp.chunkX,tmp.chunkY,reg);
				reg.chunks[tmp.chunkX][tmp.chunkY].generate();
			}
		}
		if(chunks[loc.chunkX][loc.chunkY].checkedEdges==-1)return chunks[loc.chunkX][loc.chunkY];
		for(int x=loc.chunkX-1;x<loc.chunkX+2;x++)for(int y=loc.chunkY-1;y<loc.chunkY+1;y++){
			int X=Math.clamp(x,0,31);
			if(y==-1)recalcHoriz(parent.getRegion(new RegLoc(regX,regY-1)).chunks[X][31],chunks[X][0]);
			else if(y==31)recalcHoriz(chunks[X][31],parent.getRegion(new RegLoc(regX,regY+1)).chunks[X][0]);
			else recalcHoriz(chunks[X][y],chunks[X][y+1]);
		}
		for(int x=loc.chunkX-1;x<loc.chunkX+1;x++){
			for(int y=loc.chunkY-1;y<loc.chunkY+2;y++){
				int Y=Math.clamp(y,0,31);
				if(x==-1)recalcVert(parent.getRegion(new RegLoc(regX-1,regY)).chunks[31][Y],chunks[0][Y]);
				else if(x==31)recalcVert(chunks[31][Y],parent.getRegion(new RegLoc(regX+1,regY)).chunks[0][Y]);
				else recalcVert(chunks[x][Y],chunks[x+1][Y]);
			}
			for(int y=loc.chunkY-1;y<loc.chunkY+1;y++){
				recalcCornerNW_SE(parent.getRegion(new RegLoc(regX+(x>>5),regY+(y>>5))).chunks[x&31][y&31],parent.getRegion(new RegLoc(regX+((x+1)>>5),regY+((y+1)>>5))).chunks[(x+1)&31][(y+1)&31]);
				recalcCornerNE_SW(parent.getRegion(new RegLoc(regX+((x+1)>>5),regY+(y>>5))).chunks[(x+1)&31][y&31],parent.getRegion(new RegLoc(regX+(x>>5),regY+((y+1)>>5))).chunks[x&31][(y+1)&31]);
			}
		}
		return chunks[loc.chunkX][loc.chunkY];
	}
	private void recalcHoriz(Chunk T,Chunk B){
		if((B.checkedEdges&8)==0)for(int X=0;X<16;X++)if((T.tiles[X][15]&15)==9)for(int x=-1;x<2;x++)try{if((B.tiles[X+x][0]&15)!=9)B.tiles[X+x][0]++;}catch(Exception ignored){}
		if((T.checkedEdges&2)==0)for(int X=0;X<16;X++)if((B.tiles[X][0]&15)==9)for(int x=-1;x<2;x++)try{if((T.tiles[X+x][15]&15)!=9)T.tiles[X+x][15]++;}catch(Exception ignored){}
		T.checkedEdges|=2;
		B.checkedEdges|=8;
	}
	private void recalcVert(Chunk L,Chunk R){
		if((R.checkedEdges&1)==0)for(int Y=0;Y<16;Y++)if((L.tiles[15][Y]&15)==9)for(int y=-1;y<2;y++)try{if((R.tiles[0][Y+y]&15)!=9)R.tiles[0][Y+y]++;}catch(Exception ignored){}
		if((L.checkedEdges&4)==0)for(int Y=0;Y<16;Y++)if((R.tiles[0][Y]&15)==9)for(int y=-1;y<2;y++)try{if((L.tiles[15][Y+y]&15)!=9)L.tiles[15][Y+y]++;}catch(Exception ignored){}
		L.checkedEdges|=4;
		R.checkedEdges|=1;
	}
	private void recalcCornerNW_SE(Chunk NW,Chunk SE){
		if((SE.checkedEdges&128)==0&&(NW.tiles[15][15]&15)==9&&(SE.tiles[0][0]&15)!=9)SE.tiles[0][0]++;
		if((NW.checkedEdges&32)==0&&(SE.tiles[0][0]&15)==9&&(NW.tiles[15][15]&15)!=9)NW.tiles[15][15]++;
		SE.checkedEdges|=-128;
		NW.checkedEdges|=32;
	}
	private void recalcCornerNE_SW(Chunk NE,Chunk SW){
		if((NE.checkedEdges&64)==0&&(SW.tiles[15][0]&15)==9&&(NE.tiles[0][15]&15)!=9)NE.tiles[0][15]++;
		if((SW.checkedEdges&16)==0&&(NE.tiles[0][15]&15)==9&&(SW.tiles[15][0]&15)!=9)SW.tiles[15][0]++;
		NE.checkedEdges|=64;
		SW.checkedEdges|=16;
	}
	public void load()throws IOException{
		if(!exists())throw new IOException("The requested file does not exist");
		InputStreamReader isr=new InputStreamReader(new GZIPInputStream(new FileInputStream(this)));
		JsonObject root=new Gson().fromJson(isr,JsonObject.class);
		isr.close();
		for(int X=0;X<32;X++){
			if(!root.has(X+""))continue;
			JsonObject column=root.getAsJsonObject(X+"");
			for(int Y=0;Y<32;Y++)if(column.has(Y+""))chunks[X][Y]=new Chunk(X,Y,this).load(column.getAsJsonObject(Y+""));
		}
	}
	public void save()throws IOException{
		if(!getParentFile().exists()&&!getParentFile().mkdirs())throw new IOException("Couldn't create world directory");
		if(!exists()&&!createNewFile())throw new IOException("Couldn't create file");
		JsonWriter jw=new JsonWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(this))));
		jw.beginObject().setFormattingStyle(style);
		for(int X=0;X<32;X++){
			boolean flag=false;
			for(int y=0;y<32;y++)if(chunks[X][y]!=null){
				flag=true;
				break;
			}
			if(flag){
				jw.name(X+"").beginObject();
				for(int Y=0;Y<32;Y++)if(chunks[X][Y]!=null)chunks[X][Y].save(jw);
				jw.endObject();
			}
		}
		jw.endObject().close();
		System.out.println("Saved "+getName());
	}
}