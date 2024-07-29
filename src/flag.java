import javax.swing.*;
import java.awt.event.ActionEvent;

public class flag extends JButton{
	final ImageIcon p=new ImageIcon(Main.ss40img.get(3)),np=new ImageIcon(Main.ss40img.get(4));
	flag(){
		setAction(new AbstractAction(){@Override public void actionPerformed(ActionEvent e){
			if(Main.flag)setIcon(p);
			else setIcon(np);
			Main.flag=!Main.flag;
		}});
		setBounds(5,5,40,40);
		setBorder(null);
		setIcon(p);
		setPressedIcon(np);
	}
}