package szczur4;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
public class Chunk{
	private static final FormattingStyle style=FormattingStyle.COMPACT.withNewline("\n").withIndent("\t");
	private static final Point[]edgeLocations=new Point[]{new Point(0,-1),new Point(1,0),new Point(0,1),new Point(-1,0)};
	private static final BufferedImage bigMine=new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
	private static final BufferedImage[]edgeIcons=new BufferedImage[4];
	static{
		BufferedImage toCopy=new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
		toCopy.createGraphics().drawImage(Main.ss16img.get(9),0,0,null);
		for(int x=0;x<16;x++)for(int y=0;y<16;y++)bigMine.setRGB(x,y,0x54ffffff&toCopy.getRGB(x,y));
		try{edgeIcons[0]=ImageIO.read(Chunk.class.getResourceAsStream("edge.png"));}catch(Exception _){
			System.err.println("Missing texture: edge.png");
			edgeIcons[0]=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
		}
		for(int i=1;i<4;i++)edgeIcons[i]=rotateImg(edgeIcons[i-1]);
		Graphics2D g=Main.ss16img.get(9).createGraphics();
		g.drawImage(Main.ss16img.getFirst(),0,0,null);
		g.drawImage(toCopy,0,0,null);
		g.dispose();
	}
	public final byte[][]tiles=new byte[16][16];
	public byte checkedEdges;
	int chunkX,chunkY,mines;
	protected boolean locked,lost,checkEnabled=true,uncoverEnabled=true;
	protected Region parent;
	public Chunk(int chunkX,int chunkY,Region parent){
		this.chunkX=chunkX;
		this.chunkY=chunkY;
		this.parent=parent;
	}
	public void generate(){
		mines=Main.rand.nextInt(38)+22;
		for(int i=0;i<mines;i++){
			int x=Main.rand.nextInt(16),y=Main.rand.nextInt(16);
			while(tiles[x][y]==9){
				x=Main.rand.nextInt(16);
				y=Main.rand.nextInt(16);
			}
			tiles[x][y]=9;
			for(int X=-1;X<2;X++)for(int Y=-1;Y<2;Y++)try{if(tiles[X+x][Y+y]<9)tiles[X+x][Y+y]++;}catch(Exception ignored){}
		}
		for(int x=0;x<16;x++)for(int y=0;y<16;y++)tiles[x][y]|=32;
	}
	public ArrayList<RegLoc>fill(RegLoc loc,boolean flag){
		ArrayList<RegLoc>toReturn=new ArrayList<>();
		if(flag){if((tiles[loc.tileX][loc.tileY]&32)==32){
			tiles[loc.tileX][loc.tileY]^=16;
			RegLoc tmp;
			byte tile,flags=0;
			if(checkEnabled||uncoverEnabled)for(int x=-1;x<2;x++)for(int y=-1;y<2;y++){
				for(int a=-1;a<2;a++)for(int b=-1;b<2;b++){
					tmp=loc.add(x+a,y+b);
					if((parent.parent.getRegion(tmp).getTile(tmp)&48)==48)flags++;
				}
				tmp=loc.add(x,y);
				Region reg=parent.parent.getRegion(tmp);
				tile=reg.getTile(tmp);
				if((tile&15)==flags){
					if(checkEnabled&&uncoverEnabled)reg.getChunk(tmp).tiles[tmp.tileX][tmp.tileY]&=~32;
					if(checkEnabled&&(reg.getTile(tmp)&48)==0)reg.getChunk(tmp).tiles[tmp.tileX][tmp.tileY]|=16;
					if(uncoverEnabled)for(int a=-1;a<2;a++)for(int b=-1;b<2;b++)toReturn.add(loc.add(x+a,y+b));
				}
				else if((tile&48)==16&&checkEnabled)reg.getChunk(tmp).tiles[tmp.tileX][tmp.tileY]^=16;
				flags=0;
			}
		}}
		else if(tiles[loc.tileX][loc.tileY]==41){
			tiles[loc.tileX][loc.tileY]=9;
			locked=lost=true;
		}
		else if(tiles[loc.tileX][loc.tileY]<41){
			tiles[loc.tileX][loc.tileY]&=15;
			if(tiles[loc.tileX][loc.tileY]==0)for(int x=-1;x<2;x++)for(int y=-1;y<2;y++){
				RegLoc tmp=loc.add(x,y);
				byte tile=parent.parent.getRegion(tmp).getTile(tmp);
				if(tile>31&&tile<41)toReturn.add(tmp);
			}
		}
		Collections.shuffle(toReturn);
		return toReturn;
	}
	Color dirt=new Color(0xa25100),three=new Color(0x0096ff),four=new Color(0x14c800),eight=new Color(0x500000);
	public void render(Graphics2D g,int X,int Y){
		RegLoc loc=new RegLoc(parent.regX,parent.regY,chunkX,chunkY);
		for(int y=0;y<16;y++){
			loc.tileY=y;
			for(int x=0;x<16;x++){
				if((tiles[x][y]&48)==32)continue;
				int tile=tiles[x][y]&32,dx=(x<<4)+(X<<8),dy=(y<<4)+(Y<<8);
				if(parent.parent.scale<=.5){
					if(parent.parent.scale>.2){
						g.setColor(dirt);
						g.fillRect(dx,dy,16,16);
					}
					g.setColor(switch(tiles[x][y]&48){
						case(48)->Color.RED.darker();
						case(16)->Color.GREEN.darker();
						default->switch(tiles[x][y]){
							case(1)->Color.WHITE;
							case(2)->Color.YELLOW;
							case(3)->three;
							case(4)->four;
							case(5),(9)->Color.RED;
							case(6)->Color.GREEN;
							case(7)->Color.BLUE;
							case(8)->eight;
							default->dirt;
						};
					});
					if(parent.parent.scale>.2)g.fillRect(dx+3,dy+3,10,10);
					else g.fillRect(dx,dy,16,16);
				}
				else if(parent.parent.scale<=1.5){
					if(tiles[x][y]==0){
						g.setColor(dirt);
						g.fillRect(dx,dy,17,17);
					}
					else g.drawImage(switch(tiles[x][y]&48){
						case(48)->Main.ss16img.get(12);
						case(16)->Main.ss16img.get(10);
						default->Main.ss16img.get(tiles[x][y]);
					},dx,dy,16,16,null);
				}
				else g.drawImage(switch(tiles[x][y]&48){
					case(48)->Main.ss16img.get(12);
					case(16)->Main.ss16img.get(10);
					default->Main.ss16img.get(tiles[x][y]);
				},dx,dy,16,16,null);
				loc.tileX=x;
				if(parent.parent.scale>.75)for(int i=0;i<4;i++)if((parent.parent.getTile(loc.add(edgeLocations[i].x,edgeLocations[i].y))&32)!=tile)g.drawImage(edgeIcons[i],dx,dy,16,16,null);
			}
		}
		if(locked){
			g.setColor(new Color(0x40000000,true));
			g.fillRect(X<<8,Y<<8,256,256);
			g.drawImage(lost?bigMine:Main.ss16img.get(10),X<<8,Y<<8,256,256,null);
		}
	}
	public Chunk load(JsonObject data){
		JsonArray tiles=data.getAsJsonArray("tiles");
		for(int x=0;x<tiles.size();x++){
			JsonArray column=tiles.get(x).getAsJsonArray();
			for(int y=0;y<column.size();y++)this.tiles[x][y]=column.get(y).getAsByte();
		}
		checkedEdges=data.get("edges").getAsByte();
		return this;
	}
	public void save(JsonWriter jw)throws IOException{
		jw.name(chunkY+"").beginObject().name("tiles").beginArray();
		for(int x=0;x<16;x++){
			jw.beginArray().setFormattingStyle(FormattingStyle.COMPACT);
			for(int y=0;y<16;y++)jw.value(tiles[x][y]);
			jw.endArray().setFormattingStyle(style);
		}
		jw.endArray().name("edges").value(checkedEdges).endObject();
	}
	static BufferedImage rotateImg(BufferedImage img){
		int w=img.getWidth(),h=img.getHeight();
		BufferedImage rotated=new BufferedImage(h,w,BufferedImage.TYPE_INT_ARGB);
		for(int x=0;x<w;x++)for(int y=0;y<h;y++)rotated.setRGB(h-1-y,x,img.getRGB(x,y));
		return rotated;
	}
}