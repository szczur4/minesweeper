import javax.swing.*;
import java.awt.event.ActionEvent;

public class check extends JButton{
	int flags,mines;
	boolean flag;
	check(){
		setAction(new AbstractAction(){@Override public void actionPerformed(ActionEvent e){
			if(Main.flag){
				flag=true;
				Main.flagButton.getAction().actionPerformed(e);
			}
			game.checks++;
			for(int i=0;i<5;i++)for(int x=0;x<game.width;x++)for(int y=0;y<game.height;y++)if(game.tiles.get(x).get(y).t!=Main.ss10img.get(0)&&game.tiles.get(x).get(y).U){
				for(int a=-1;a<2;a++)for(int b=-1;b<2;b++)try{if(game.tiles.get(x+a).get(y+b).n==9&&game.tiles.get(x+a).get(y+b).F)flags++;}catch(Exception ignored){}
				if(game.tiles.get(x).get(y).n==flags){
					for(int a=-1;a<2;a++)for(int b=-1;b<2;b++)try{if(!game.tiles.get(x+a).get(y+b).F&&game.tiles.get(x+a).get(y+b).t!=Main.ss10img.get(10)){
						game.tiles.get(x+a).get(y+b).t=Main.ss10img.get(game.tiles.get(x+a).get(y+b).n);
						game.bfs(x+a,y+b);
					}}catch(Exception ignored){}
					game.tiles.get(x).get(y).t=Main.ss10img.get(10);
				}
				flags=0;
			}
			for(int x=0;x<game.width;x++)for(int y=0;y<game.height;y++)if(game.tiles.get(x).get(y).n==9&&game.tiles.get(x).get(y).F)mines++;
			game.minesLeft=game.numberOfMines-mines;
			mines=0;
			if(flag){
				flag=false;
				Main.flagButton.getAction().actionPerformed(e);
			}
			if(game.minesLeft<=0){
				game.endingTime=System.currentTimeMillis();
				game.endStr="you won";
				game.ended=true;
			}
		}});
		setBounds(50,5,40,40);
		setBorder(null);
		setIcon(new ImageIcon(Main.ss40img.get(0)));
		setPressedIcon(new ImageIcon(Main.ss40img.get(1)));
	}
}