package szczur4;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import javax.imageio.ImageIO;
public class Chunk{
	private static final FormattingStyle style=FormattingStyle.COMPACT.withNewline("\n").withIndent("\t");
	private static final Point[]edgeLocations=new Point[]{new Point(0,-1),new Point(1,0),new Point(0,1),new Point(-1,0)};
	private static final BufferedImage bigMine=new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB),bigCheck=new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
	private static final BufferedImage[]edgeIcons=new BufferedImage[4];
	static{
		BufferedImage toCopy=new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
		toCopy.createGraphics().drawImage(Main.ss16img.get(9),0,0,null);
		for(int x=0;x<16;x++)for(int y=0;y<16;y++)bigMine.setRGB(x,y,0x54ffffff&toCopy.getRGB(x,y));
		toCopy=new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
		toCopy.createGraphics().drawImage(Main.ss16img.get(10),0,0,null);
		for(int x=0;x<16;x++)for(int y=0;y<16;y++)bigCheck.setRGB(x,y,0x54ffffff&toCopy.getRGB(x,y));
		try{edgeIcons[0]=ImageIO.read(Chunk.class.getResourceAsStream("edge.png"));}catch(Exception _){
			System.err.println("Missing texture: edge.png");
			edgeIcons[0]=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
		}
		for(int i=1;i<4;i++)edgeIcons[i]=rotateImg(edgeIcons[i-1]);
	}
	public final byte[][]tiles=new byte[16][16];
	public byte checkedEdges;
	int chunkX,chunkY,mines,correctFlags,wrongFlags;
	protected boolean locked,lost;
	protected Region parent;
	Rectangle2D unlockRect;
	String unlockStr;
	public Chunk(int chunkX,int chunkY,Region parent){
		this.chunkX=chunkX;
		this.chunkY=chunkY;
		this.parent=parent;
	}
	public void generate(){
		mines=Main.rand.nextInt(50)+22;
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
		createUnlockInfo();
	}
	public ArrayList<RegLoc>fill(RegLoc loc,boolean flag){
		ArrayList<RegLoc>toReturn=new ArrayList<>();
		if(flag){if((tiles[loc.tileX][loc.tileY]&32)==32){
			if(tiles[loc.tileX][loc.tileY]==57)correctFlags--;
			else if((tiles[loc.tileX][loc.tileY]&48)==48)wrongFlags--;
			tiles[loc.tileX][loc.tileY]^=16;
			if(tiles[loc.tileX][loc.tileY]==57)correctFlags++;
			else if((tiles[loc.tileX][loc.tileY]&48)==48)wrongFlags++;
			RegLoc tmp;
			byte tile,flags=0;
			if(parent.parent.checkEnabled||parent.parent.uncoverEnabled)for(int x=-1;x<2;x++)for(int y=-1;y<2;y++){
				for(int a=-1;a<2;a++)for(int b=-1;b<2;b++){
					tmp=loc.add(x+a,y+b);
					if((parent.parent.getRegion(tmp).getTile(tmp)&48)==48)flags++;
				}
				tmp=loc.add(x,y);
				Region reg=parent.parent.getRegion(tmp);
				tile=reg.getTile(tmp);
				if((tile&15)==flags&&(tile&15)!=0&&(tile&48)!=48){
					if(parent.parent.checkEnabled&&parent.parent.uncoverEnabled)reg.getChunk(tmp).tiles[tmp.tileX][tmp.tileY]&=~32;
					if(parent.parent.checkEnabled&&(reg.getTile(tmp)&48)==0)reg.getChunk(tmp).tiles[tmp.tileX][tmp.tileY]|=16;
					if(parent.parent.uncoverEnabled)for(int a=-1;a<2;a++)for(int b=-1;b<2;b++)toReturn.add(loc.add(x+a,y+b));
				}
				else if((tile&48)==16&&parent.parent.checkEnabled)reg.getChunk(tmp).tiles[tmp.tileX][tmp.tileY]^=16;
				flags=0;
			}
		}}
		else if(tiles[loc.tileX][loc.tileY]==41){
			tiles[loc.tileX][loc.tileY]=9;
			if(!lost)Stats.lost=Stats.lost.add(BigInteger.ONE);
			locked=lost=true;
		}
		else if(tiles[loc.tileX][loc.tileY]<41&&(tiles[loc.tileX][loc.tileY]&16)==0){
			tiles[loc.tileX][loc.tileY]&=15;
			if(tiles[loc.tileX][loc.tileY]==0)for(int x=-1;x<2;x++)for(int y=-1;y<2;y++){
				RegLoc tmp=loc.add(x,y);
				byte tile=parent.parent.getRegion(tmp).getTile(tmp);
				if(tile>31&&tile<41)toReturn.add(tmp);
			}
		}
		clear();
		Collections.shuffle(toReturn);
		return toReturn;
	}
	public void clear(){
		if(!locked&&mines==correctFlags&&wrongFlags==0){
			locked=true;
			Stats.flags=Stats.flags.add(BigInteger.valueOf(mines));
			Stats.cleared=Stats.cleared.add(BigInteger.ONE);
		}
	}
	public void unlock(int rx,int ry){
		if(Stats.flags.compareTo(BigInteger.valueOf(mines))>0&&rx>=unlockRect.getX()&&rx<=unlockRect.getX()+unlockRect.getWidth()&&ry>=unlockRect.getY()&&ry<=unlockRect.getY()+unlockRect.getHeight()){
			for(int x=0;x<16;x++)for(int y=0;y<16;y++){
				if(tiles[x][y]==9)tiles[x][y]|=32;
				if((tiles[x][y]&48)==48&&(tiles[x][y]&15)!=9){
					RegLoc tmp=new RegLoc(parent.regX,parent.regY,chunkX,chunkY,x,y);
					fill(tmp,true);
					fill(tmp,false);
					tiles[x][y]&=15;
				}
			}
			Stats.flags=Stats.flags.subtract(BigInteger.valueOf(mines));
			locked=lost=false;
		}
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
					if((tiles[x][y]&48)!=48){
						g.setColor(dirt);
						g.fillRect(dx,dy,16,16);
					}
					if(parent.parent.scale>.2){
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
						g.fillRect(dx+4,dy+4,8,8);
					}
				}
				else if(parent.parent.scale<=1.5){
					if(tiles[x][y]==0){
						g.setColor(dirt);
						g.fillRect(dx,dy,17,17);
					}
					else{
						if(tiles[x][y]==9||(tiles[x][y]&48)==16){
							g.setColor(dirt);
							g.fillRect(dx,dy,17,17);
						}
						g.drawImage(switch(tiles[x][y]&48){
							case(48)->Main.ss16img.get(12);
							case(16)->Main.ss16img.get(10);
							default->Main.ss16img.get(tiles[x][y]);
						},dx,dy,16,16,null);
					}
				}
				else{
					if(tiles[x][y]==9||(tiles[x][y]&48)==16)g.drawImage(Main.ss16img.getFirst(),dx,dy,16,16,null);
					g.drawImage(switch(tiles[x][y]&48){
						case(48)->Main.ss16img.get(12);
						case(16)->Main.ss16img.get(10);
						default->Main.ss16img.get(tiles[x][y]);
					},dx,dy,16,16,null);
				}
				loc.tileX=x;
				if(parent.parent.scale>.75)for(int i=0;i<4;i++)if((parent.parent.getTile(loc.add(edgeLocations[i].x,edgeLocations[i].y))&32)!=tile)g.drawImage(edgeIcons[i],dx,dy,16,16,null);
			}
		}
		if(locked){
			g.setColor(new Color(0x40000000,true));
			g.fillRect(X<<8,Y<<8,256,256);
			Stroke s=g.getStroke();
			g.setStroke(new BasicStroke(2));
			g.drawRect((X<<8)+1,(Y<<8)+1,254,254);
			g.setStroke(s);
			g.drawImage(lost?bigMine:bigCheck,X<<8,Y<<8,256,256,null);
			if(lost)drawPrice(g,X<<8,Y<<8);
		}
	}
	void drawPrice(Graphics2D g,int x,int y){
		g.setColor(Game.shadow);
		g.fillRect((int)unlockRect.getX()+x,(int)unlockRect.getY()+y,(int)unlockRect.getWidth(),(int)unlockRect.getHeight());
		g.drawString(unlockStr,(int)unlockRect.getX()+5+x,(int)unlockRect.getY()-3+(int)unlockRect.getHeight()+y);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(unlockStr,(int)unlockRect.getX()+3+x,(int)unlockRect.getY()-5+(int)unlockRect.getHeight()+y);
		g.drawImage(Main.ss16img.get(12),(int)unlockRect.getX()-18+(int)unlockRect.getWidth()+x,(int)unlockRect.getY()+2+y,16,16,null);
	}
	void createUnlockInfo(){
		unlockRect=parent.parent.getFontMetrics(parent.parent.getFont()).getStringBounds(unlockStr="Unlock for "+mines,null);
		unlockRect.setRect((int)(234-unlockRect.getWidth())>>1,150,unlockRect.getWidth()+22,unlockRect.getHeight()+4);
	}
	public Chunk load(JsonObject data){
		JsonArray tiles=data.getAsJsonArray("tiles");
		for(int x=0;x<16;x++){
			JsonArray column=tiles.get(x).getAsJsonArray();
			for(int y=0;y<16;y++){
				byte tile=column.get(y).getAsByte();
				if((tile&15)==9)mines++;
				if(tile==9){
					Stats.lost=Stats.lost.add(BigInteger.ONE);
					locked=lost=true;
				}
				else if(tile==57)correctFlags++;
				else if((tile&48)==48)wrongFlags++;
				this.tiles[x][y]=tile;
			}
		}
		createUnlockInfo();
		checkedEdges=data.get("edges").getAsByte();
		locked|=(mines==correctFlags&&wrongFlags==0);
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