import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import static java.lang.Math.*;
import static javax.swing.KeyStroke.getKeyStroke;

public class game extends JPanel implements Runnable,MouseListener{
	Robot robot;
	Thread thread=new Thread(this);
	static final Vector<Vector<tile>>tiles=new Vector<>();
	final Vector<Point>locations=new Vector<>();
	static final Queue<Point>queue=new ArrayDeque<>(16);
	final InputMap input=getInputMap(WHEN_IN_FOCUSED_WINDOW);
	static boolean running=true,ended;
	final int gameWidth=60,gameHeight=40;
	int random,X,Y,mouseX,mouseY;
	static int width=60,height=40,checks,numberOfMines=400,minesLeft=numberOfMines;
	static long time,startingTime,endingTime,playTime;
	final String[]actionKeys={"W","A","S","D","ctrl W","ctrl A","ctrl S","ctrl D","shift W","shift A","shift S","shift D","F","C"};
	static String endStr;
	game()throws Exception{
		robot=new Robot();
		Main.reset.setAction(new AbstractAction(){@Override public void actionPerformed(ActionEvent e){
			ended=false;
			try{width=max(Integer.parseInt(Main.width.getText()),20);}catch(Exception ignored){}
			try{height=max(Integer.parseInt(Main.height.getText()),12);}catch(Exception ignored){}
			try{numberOfMines=min(Integer.parseInt(Main.mines.getText()),width*height-4);}catch(Exception ignored){}
			Main.mines.setFocusable(false);
			Main.height.setFocusable(false);
			Main.width.setFocusable(false);
			Main.width.setFocusable(true);
			Main.height.setFocusable(true);
			Main.mines.setFocusable(true);
			minesLeft=max(numberOfMines,0);
			X=0;
			Y=0;
			checks=0;
			startingTime=0;
			if(Main.flag)Main.flagButton.getAction().actionPerformed(e);
			tiles.removeAllElements();
			running=true;
			generate();
			thread=new Thread(game.this);
			thread.start();
		}});
		for(int i=0;i<14;i++)input.put(getKeyStroke(actionKeys[i]),actionKeys[i]);
		for(int i=0;i<12;i++)getActionMap().put(actionKeys[i],Main.actions[i]);
		getActionMap().put("F",Main.flagButton.getAction());
		getActionMap().put("C",Main.check.getAction());
		addMouseListener(this);
		setDoubleBuffered(true);
		setBounds(5,50,min(gameWidth,width)*20+2,min(gameHeight,height)*20+2);
		generate();
		thread.start();
	}
	private void generate(){
		final Random randomizer=new Random();
		for(int x=0;x<width;x++){
			tiles.add(new Vector<>());
			for(int y=0;y<height;y++){
				tiles.get(x).add(new tile());
				locations.add(new Point(x,y));
			}
		}
		locations.remove(width*height-1);
		locations.remove(width*height-height);
		locations.remove(height-1);
		locations.remove(0);
		for(int i=0;i<min(width*height-4,numberOfMines);i++){
			random=randomizer.nextInt(locations.size());
			int X=locations.get(random).x,Y=locations.remove(random).y;
			tiles.get(X).get(Y).n=9;
		}
		for(int x=0;x<width;x++)for(int y=0;y<height;y++){
			tiles.get(x).get(y).t=Main.ss10img.get(11);
			if(tiles.get(x).get(y).n!=9)for(int a=-1;a<2;a++)for(int b=-1;b<2;b++)try{if(game.tiles.get(x+a).get(y+b).n==9)tiles.get(x).get(y).n++;}catch(Exception ignored){}
		}
		locations.removeAllElements();
		setSize(min(gameWidth,width)*20+2,min(gameHeight,height)*20+2);
		Main.frame.setSize(getWidth()+25,getHeight()+95);
	}
	static void bfs(int X,int Y){
		do{
			try{queue.remove();}catch(Exception ignored){}
			if(!ended){
				if(Main.flag){
					if(!tiles.get(X).get(Y).U){
						tiles.get(X).get(Y).F=!tiles.get(X).get(Y).F;
						if(tiles.get(X).get(Y).F)tiles.get(X).get(Y).t=Main.ss40img.get(2);
						else tiles.get(X).get(Y).t=Main.ss10img.get(11);
					}
				}
				else if(tiles.get(X).get(Y).n==9){
					endingTime=System.currentTimeMillis();
					for(int x=0;x<width;x++)for(int y=0;y<height;y++)if(tiles.get(x).get(y).n==9)tiles.get(x).get(y).t=Main.ss10img.get(9);
					endStr="you lost";
					ended=true;
				}
				else if(!tiles.get(X).get(Y).F){
					tiles.get(X).get(Y).t=Main.ss10img.get(tiles.get(X).get(Y).n);
					if(tiles.get(X).get(Y).n==0&&!tiles.get(X).get(Y).U)for(int a=-1;a<2;a++)for(int b=-1;b<2;b++)try{if(tiles.get(a+X).get(b+Y).n!=9){
						tiles.get(a+X).get(b+Y).t=Main.ss10img.get(tiles.get(a+X).get(b+Y).n);
						if(!tiles.get(a+X).get(b+Y).U)queue.add(new Point(a+X,b+Y));
					}}catch(Exception ignored){}
					tiles.get(X).get(Y).U=true;
					if(!queue.isEmpty()){
						X=queue.peek().x;
						Y=queue.peek().y;
					}
				}
			}
			else{
				queue.clear();
				return;
			}
		}while(!queue.isEmpty());
	}
	@Override
	public void run(){
		while(getGraphics()==null){}
		do{
			Graphics2D g=(Graphics2D)getGraphics();
			g.setColor(Main.fore);
			time=System.currentTimeMillis();
			Main.location.setText("X: "+X+"-"+(X+min(gameWidth,width))+", Y: "+Y+"-"+(Y+min(gameHeight,height)));
			Main.minesLeft.setText("mines left: "+minesLeft);
			try{for(int x=X;x<min(gameWidth+X,width);x++)for(int y=Y;y<min(gameHeight+Y,height);y++)g.drawImage(tiles.get(x).get(y).t,20*(x-X)+1,20*(y-Y)+1,20,20,(img,infoFlags,x1,y1,width,height)->false);}catch(Exception ignored){}
			if(ended){
				playTime=endingTime-startingTime;
				playTime/=10;
				g.setColor(Main.back);
				g.fillRect(min(gameWidth,width)*10-119,min(gameHeight,height)*10-39,240,80);
				g.setColor(Main.fore);
				g.setColor(new Color(min(max((int)(255*(1-(float)(numberOfMines-minesLeft)/numberOfMines)*2),0),255),min((int)(255*((float)(numberOfMines-minesLeft))*2),255),0));
				g.drawRect(min(gameWidth,width)*10-119,min(gameHeight,height)*10-39,240,80);
				g.drawString(endStr,min(gameWidth,width)*10-114,min(gameHeight,height)*10-24);
				g.drawString("mines cleared: "+(numberOfMines-minesLeft)+"/"+numberOfMines+" "+((float)(numberOfMines-minesLeft)*100/numberOfMines)+"%",min(gameWidth,width)*10-114,min(gameHeight,height)*10-9);
				g.drawString("time played: "+playTime/360000+"h "+playTime%360000/6000+"min "+playTime%6000/100+"."+playTime%100+"s",min(gameWidth,width)*10-114,min(gameHeight,height)*10+6);
				g.drawString("checked "+checks+" times",min(gameWidth,width)*10-114,min(gameHeight,height)*10+21);
				if((float)(numberOfMines-minesLeft)/numberOfMines==0.69)g.drawString("nice",min(gameWidth,width)*10-114,min(gameHeight,height)*10+36);
				running=false;
			}
			g.dispose();
			getGraphics().dispose();
			if(System.currentTimeMillis()-time<50)robot.delay((int)(time+50-System.currentTimeMillis()));
		}while(thread.isAlive()&&running);
	}
	@Override public void mouseClicked(MouseEvent e){}
	@Override public void mousePressed(MouseEvent e){
		if(startingTime==0)startingTime=System.currentTimeMillis();
		mouseX=e.getX()/20+X;
		mouseY=e.getY()/20+Y;
		if(mouseX>=width||mouseY>=height){
			endingTime=System.currentTimeMillis();
			endStr="you broke the game. you bastard";
			ended=true;
		}
		bfs(mouseX,mouseY);
	}
	@Override public void mouseReleased(MouseEvent e){}
	@Override public void mouseEntered(MouseEvent e){}
	@Override public void mouseExited(MouseEvent e){}
}