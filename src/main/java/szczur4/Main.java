package szczur4;
import java.awt.image.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
public class Main extends JFrame{
	static final ArrayList<BufferedImage>ss16img=new ArrayList<>();
	static final Random rand=new Random();
	void main()throws Exception{
		BufferedImage ss16=(ImageIO.read(Main.class.getResource("ss16.png")));
		for(int i=0;i<13;i++)ss16img.add(ss16.getSubimage(i<<4,0,16,16));
		setIconImage(ss16img.get(9).getSubimage(2,2,12,12));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(new Game());
		setSize(500,300);
		setTitle("szczur4 Minesweeper");
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
	}
}