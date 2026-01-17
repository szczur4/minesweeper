package szczur4;
import java.awt.geom.*;
public class RegLoc extends Point2D{
	public long regX,regY;
	public int chunkX,chunkY,tileX,tileY;
	public RegLoc(){}
	public RegLoc(long regX,long regY){
		this.regX=regX;
		this.regY=regY;
	}
	public RegLoc(long regX,long regY,int chunkX,int chunkY){
		this(regX,regY);
		this.chunkX=chunkX;
		this.chunkY=chunkY;
	}
	public RegLoc(long regX,long regY,int chunkX,int chunkY,int tileX,int tileY){
		this(regX,regY,chunkX,chunkY);
		this.tileX=tileX;
		this.tileY=tileY;
	}
	public RegLoc add(int x,int y){
		int tileX=this.tileX+x,tileY=this.tileY+y,chunkX=this.chunkX+tileX/16+(tileX>>31),chunkY=this.chunkY+tileY/16+(tileY>>31);
		long regX=this.regX+chunkX/32+(chunkX>>31),regY=this.regY+chunkY/32+(chunkY>>31);
		tileX=(16+tileX%16)%16;
		tileY=(16+tileY%16)%16;
		chunkX=(32+chunkX&31)&31;
		chunkY=(32+chunkY&31)&31;
		return new RegLoc(regX,regY,chunkX,chunkY,tileX,tileY);
	}
	public boolean isXLessThan(RegLoc loc){
		if(regX<loc.regX)return true;
		if(regX==loc.regX){
			if(chunkX<loc.chunkX)return true;
			if(chunkX==loc.chunkX)return tileX<loc.tileX;
		}
		return false;
	}
	public boolean isXLessThanOrEqual(RegLoc loc){return isXLessThan(loc)||(regX<=loc.regX&&chunkX<=loc.chunkX&&tileX<=loc.tileX);}
	public boolean isYLessThan(RegLoc loc){
		if(regY<loc.regY)return true;
		if(regY==loc.regY){
			if(chunkY<loc.chunkY)return true;
			if(chunkY==loc.chunkY)return tileY<loc.tileY;
		}
		return false;
	}
	public boolean isYLessThanOrEqual(RegLoc loc){return isYLessThan(loc)||(regY<=loc.regY&&chunkY<=loc.chunkY&&tileY<=loc.tileY);}
	public double getX(){return regX;}
	public double getY(){return regY;}
	public String toString(){return"X: "+regX+" "+chunkX+" "+tileX+" Y: "+regY+" "+chunkY+" "+tileY;}
	public void setLocation(double x,double y){
		this.regX=(long)x;
		this.regY=(long)y;
	}
}