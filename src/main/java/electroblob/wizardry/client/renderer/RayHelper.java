package electroblob.wizardry.client.renderer;

/** Instances of this class can calculate their distance from the camera viewpoint and sort themselves accordingly. */
class RayHelper implements Comparable<RayHelper> {

	int ordinal;
	double x1, y1, z1;
	double x2, y2, z2;
	double offsetX, offsetY, offsetZ;
	
	RayHelper(int ordinal, double x1, double y1, double z1, double x2, double y2, double z2, double offsetX, double offsetY, double offsetZ){
		this.ordinal = ordinal;
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
	}
	
	double getDistanceFromViewpoint(){
		
		double midX = (x1+x2)/2;
		double midY = (y1+y2)/2;
		double midZ = (z1+z2)/2;
		
		double absoluteX = offsetX+midX;
		double absoluteY = offsetY+midY;
		double absoluteZ = offsetZ+midZ;
		
		return Math.sqrt(absoluteX*absoluteX + absoluteY*absoluteY + absoluteZ*absoluteZ);
	}
	
	@Override
	public int compareTo(RayHelper ray){
		if(this.getDistanceFromViewpoint() > ray.getDistanceFromViewpoint()){
			return -1;
		}else if(this.getDistanceFromViewpoint() < ray.getDistanceFromViewpoint()){
			return 1;
		}else{
			return 0;
		}
	}

}
