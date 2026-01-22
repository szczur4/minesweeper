package szczur4;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.concurrent.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
public class Game extends JPanel implements MouseMotionListener,MouseWheelListener{
	public static AffineTransform DEFAULT_TRANSFORM;
	public static final int CORES=Runtime.getRuntime().availableProcessors();
	public static final Color highlight=new Color(0x3fffffff,true),shadow=new Color(0x54000000,true),bg=new Color(0x21ac00),on=new Color(0xa000ff00,true),off=new Color(0xa0ff0000,true);
	public boolean checkEnabled=true,uncoverEnabled=true;
	int mouseX=-1,mouseY=-1,tx,ty,WIDTH,HEIGHT;
	final Map<RegLoc,Region>regions=new HashMap<>();
	RegLoc scrLoc=new RegLoc(),scrEndLoc=new RegLoc();
	final Rectangle resetRect=new Rectangle(5,85,100,20);
	Rectangle checkRect=new Rectangle(),uncoverRect=new Rectangle();
	final String coordFormat="Region %d %d, Chunk %d %d, Tile %d %d";
	String coords=String.format(coordFormat,0,0,0,0,0,0);
	ExecutorService executor=Executors.newWorkStealingPool(CORES);
	double scale=1;
	boolean resetting;
	Stats stats=new Stats();
	/// covered, flag, mines
	/// 0b 0_0_0000;
	Game()throws IOException{
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			for(Region region:regions.values())try{region.save();}catch(Exception _){}
			try{stats.save();}catch(Exception _){}
		}));
		File dir=new File("world");
		if(dir.exists())for(String name:dir.list()){try{
			String[]coords=name.split(" |\\..*");
			int X=Integer.parseInt(coords[0]),Y=Integer.parseInt(coords[1]);
			regions.put(new RegLoc(X,Y),new Region(X,Y,this).load());
		}catch(Exception _){}}
		setLayout(null);
		addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){
			if(e.getX()>resetRect.x&&e.getX()<resetRect.x+resetRect.width&&e.getY()>resetRect.y&&e.getY()<resetRect.y+resetRect.height){
				resetting=true;
				executor.shutdownNow();
				for(Region region:regions.values())if(region.exists()){
					if(!region.delete())System.err.println("Failed to delete "+region.getName()+" during reset");
					else System.out.println("Deleted "+region.getName());
				}
				regions.clear();
				stats.reset();
				scrLoc=new RegLoc();
				scrEndLoc=scrLoc.add((WIDTH+16)>>4,(HEIGHT+16)>>4);
				tx=0;ty=0;
				executor=Executors.newWorkStealingPool(CORES);
				resetting=false;
				repaint();
			}
			else if(e.getX()>getWidth()-45&&e.getX()<getWidth()-5){
				if(e.getY()>=5&&e.getY()<25)checkEnabled=!checkEnabled;
				else if(e.getY()>=25&&e.getY()<45)uncoverEnabled=!uncoverEnabled;
			}
			else executor.execute(()->fill(scrLoc.add(((mouseX=(int)(e.getX()/scale))+tx)>>4,((mouseY=(int)(e.getY()/scale))+ty)>>4),e.getButton()==3));
			repaint();
		}});
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addComponentListener(new ComponentAdapter(){public void componentResized(ComponentEvent e){
			checkRect=new Rectangle(getWidth()-165,5,120,20);
			uncoverRect=new Rectangle(getWidth()-165,25,120,20);
			WIDTH=(int)Math.ceil(getWidth()/scale);
			HEIGHT=(int)Math.ceil(getHeight()/scale);
			scrEndLoc=scrLoc.add((WIDTH>>4)+1,(HEIGHT>>4)+1);
			repaint();
		}});
		setBackground(Color.BLACK);
		setLocation(5,5);
		setDoubleBuffered(true);
	}
	public Region getRegion(RegLoc loc){
		Region reg=regions.get(loc);
		if(reg==null)regions.put(loc,reg=new Region(loc.regX,loc.regY,this));
		return reg;
	}
	public byte getTile(RegLoc loc){return getRegion(loc).getTile(loc);}
	public static boolean areLocsEqual(RegLoc loc1,RegLoc loc2){return loc1.regX==loc2.regX&&loc1.regY==loc2.regY&&loc1.chunkX==loc2.chunkX&&loc1.chunkY==loc2.chunkY&&loc1.tileX==loc2.tileX&&loc1.tileY==loc2.tileY;}
	public void fill(RegLoc loc,boolean flag){
		if(getRegion(loc).getChunk(loc).locked){
			if(getRegion(loc).getChunk(loc).lost)getRegion(loc).getChunk(loc).unlock(mouseX-((loc.chunkX-scrLoc.chunkX+(int)((loc.regX-scrLoc.regX)<<5))<<8)+tx+(scrLoc.tileX<<4),mouseY-((loc.chunkY-scrLoc.chunkY+(int)((loc.regY-scrLoc.regY)<<5))<<8)+ty+(scrLoc.tileY<<4));
			return;
		}
		ArrayList<RegLoc>locations=new ArrayList<>();
		locations.add(loc);
		while(!locations.isEmpty()&&!resetting){
			RegLoc tmp=switch(Main.rand.nextInt(4)){
				case(0)->locations.removeFirst();
				case(1)->locations.remove(Main.rand.nextInt(locations.size()));
				default->locations.removeLast();
			};
			if(resetting)break;
			for(RegLoc tmpLoc1:getRegion(tmp).getChunk(tmp).fill(tmp,flag)){
				if((getRegion(tmpLoc1).getTile(tmpLoc1)&48)==16)continue;
				boolean add=true;
				for(RegLoc tmpLoc2:locations){
					if(resetting)break;
					if(areLocsEqual(tmpLoc2,tmpLoc1)){
						add=false;
						break;
					}
				}
				if(resetting)break;
				if(add)locations.add(tmpLoc1);
			}
			flag=false;
			repaint();
			try{Thread.sleep((long)Math.max(1,5*(1-locations.size()/100f)));}catch(InterruptedException _){Thread.currentThread().interrupt();}
		}
		System.gc();
	}
	public void paint(Graphics gr){
		Graphics2D g=(Graphics2D)gr;
		if(DEFAULT_TRANSFORM==null)DEFAULT_TRANSFORM=g.getTransform();
		render(g);
		g.setColor(highlight);
		g.scale(scale,scale);
		g.fillRect(mouseX-(mouseX+tx)%16,mouseY-(mouseY+ty)%16,16,16);
		g.setTransform(DEFAULT_TRANSFORM);
		drawStringWithShadow(g,coords,5,5,0,20,Color.lightGray,shadow);
		g.setColor(shadow);
		g.fillRect(5,25,15,60);
		g.drawImage(Main.ss16img.get(12),7,27,16,16,null);
		g.drawImage(Main.ss16img.get(10),7,47,16,16,null);
		g.drawImage(Main.ss16img.get(9),7,67,16,16,null);
		drawStringWithShadow(g,": "+Stats.flags,20,25,0,20,Color.lightGray,shadow);
		drawStringWithShadow(g,": "+Stats.cleared,20,45,0,20,Color.lightGray,shadow);
		drawStringWithShadow(g,": "+Stats.lost,20,65,0,20,Color.lightGray,shadow);
		g.setColor(shadow);
		g.fill(resetRect);
		drawStringWithShadow(g,"Reset",resetRect,Color.LIGHT_GRAY,shadow);
		drawStringWithShadow(g,"Enable visual Check",checkRect,Color.LIGHT_GRAY,shadow);
		drawSwitch(g,getWidth()-45,5,checkEnabled);
		drawStringWithShadow(g,"Enable Auto Uncover",uncoverRect,Color.LIGHT_GRAY,shadow);
		drawSwitch(g,getWidth()-45,25,uncoverEnabled);
		g.dispose();
	}
	public void drawSwitch(Graphics2D g,int x,int y,boolean val){
		g.setColor(shadow);
		g.fillRect(x,y,40,20);
		y+=3;
		g.fillRect(x+(val?3:20),y,17,14);
		g.setColor(val?on:off);
		g.fillRect(x+(val?20:3),y,17,14);
	}
	public void render(Graphics2D g){
		g.scale(scale,scale);
		if(scale<1){
			g.setColor(bg);
			g.fillRect(0,0,WIDTH,HEIGHT);
		}
		else for(int x=0;x<WIDTH+17;x+=16)for(int y=0;y<HEIGHT+17;y+=16)g.drawImage(Main.ss16img.get(11),x-tx,y-ty,16,16,null);
		g.translate(-16*scrLoc.tileX-tx,-16*scrLoc.tileY-ty);
		RegLoc a=new RegLoc(scrLoc.regX,scrLoc.regY,scrLoc.chunkX,scrLoc.chunkY);
		while(a.isXLessThanOrEqual(scrEndLoc)){
			while(a.isYLessThanOrEqual(scrEndLoc)){
				if(regions.get(a)!=null&&regions.get(a).chunks[a.chunkX][a.chunkY]!=null)regions.get(a).chunks[a.chunkX][a.chunkY].render(g,a.chunkX-scrLoc.chunkX+(int)((a.regX-scrLoc.regX)<<5),a.chunkY-scrLoc.chunkY+(int)((a.regY-scrLoc.regY)<<5));
				a=a.add(0,16);
			}
			a=new RegLoc(a.regX,scrLoc.regY,a.chunkX,scrLoc.chunkY).add(16,0);
		}
		g.setTransform(DEFAULT_TRANSFORM);
	}
	public static void drawStringWithShadow(Graphics2D g,String str,Rectangle rect,Color font,Color shadow){drawStringWithShadow(g,str,rect.x,rect.y,rect.width,rect.height,font,shadow);}
	public static void drawStringWithShadow(Graphics2D g,String str,int x,int y,int w,int h,Color font,Color shadow){
		g.setColor(shadow);
		Rectangle2D rect=g.getFontMetrics().getStringBounds(str,g);
		g.fillRect(x,y,(int)Math.max(rect.getWidth()+6,w),(int)Math.max(rect.getHeight()+4,h));
		g.drawString(str,x+4+((int)Math.max(0,w-rect.getWidth())>>1),y+1+(int)rect.getHeight());
		g.setColor(font);
		g.drawString(str,x+2+((int)Math.max(0,w-rect.getWidth())>>1),y-1+(int)rect.getHeight());
	}
	public void mouseDragged(MouseEvent e){
		int x=(int)(e.getX()/scale),y=(int)(e.getY()/scale);
		tx+=mouseX-x;
		ty+=mouseY-y;
		scrLoc=scrLoc.add(tx>>4,ty>>4);
		scrEndLoc=scrLoc.add((WIDTH+16)>>4,(HEIGHT+16)>>4);
		tx&=15;
		ty&=15;
		mouseX=x;
		mouseY=y;
		repaint();
	}
	public void mouseMoved(MouseEvent e){
		mouseX=(int)(e.getX()/scale);
		mouseY=(int)(e.getY()/scale);
		RegLoc tmp=scrLoc.add((mouseX+tx)>>4,(mouseY+ty)>>4);
		coords=String.format(coordFormat,tmp.regX,tmp.regY,tmp.chunkX,tmp.chunkY,tmp.tileX,tmp.tileY);
		repaint();
	}
	public void mouseWheelMoved(MouseWheelEvent e){
		scale-=e.getPreciseWheelRotation()*scale;
		scale=Math.clamp(scale,.1,5);
		tx+=(int)Math.ceil(mouseX-e.getX()/scale);
		ty+=(int)Math.ceil(mouseY-e.getY()/scale);
		scrLoc=scrLoc.add(tx>>4,ty>>4);
		tx&=15;
		ty&=15;
		mouseX=(int)(e.getX()/scale);
		mouseY=(int)(e.getY()/scale);
		WIDTH=(int)Math.ceil(getWidth()/scale);
		HEIGHT=(int)Math.ceil(getHeight()/scale);
		scrEndLoc=scrLoc.add((WIDTH+16)>>4,(HEIGHT+16)>>4);
		repaint();
	}
}