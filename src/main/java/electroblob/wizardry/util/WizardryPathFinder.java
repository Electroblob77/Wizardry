package electroblob.wizardry.util;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathHeap;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Minecraft's pathfinder refused to play nicely, so I 'borrowed' its code and fiddled with it. */
public class WizardryPathFinder {
	
    /** The path being generated */
    private final PathHeap path = new PathHeap();
    private final Set<PathPoint> closedSet = Sets.<PathPoint>newHashSet();
    /** Selection of path points to add to the path */
    private final PathPoint[] pathOptions = new PathPoint[32];
    private final NodeProcessor nodeProcessor;

    public WizardryPathFinder(NodeProcessor processor){
        this.nodeProcessor = processor;
    }
    
    @Nullable
    public Path findPath(IBlockAccess world, EntityLiving entity, BlockPos destination, float range){
        return this.findPath(world, entity, (double)((float)destination.getX() + 0.5F), (double)((float)destination.getY() + 0.5F), (double)((float)destination.getZ() + 0.5F), range);
    }

    @Nullable
    private Path findPath(IBlockAccess world, EntityLiving entity, double x, double y, double z, float range){
        this.path.clearPath();
        this.nodeProcessor.initProcessor(world, entity);
        PathPoint pathpoint = this.nodeProcessor.getStart();
        PathPoint pathpoint1 = this.nodeProcessor.getPathPointToCoords(x, y, z);
        Path path = this.findPath(pathpoint, pathpoint1, range);
        this.nodeProcessor.postProcess();
        return path;
    }

    @Nullable
    private Path findPath(PathPoint start, PathPoint end, float range){
        
    	start.totalPathDistance = 0.0F;
        start.distanceToNext = start.distanceManhattan(end);
        start.distanceToTarget = start.distanceToNext;
        this.path.clearPath();
        this.closedSet.clear();
        this.path.addPoint(start);
        PathPoint pathpoint = start;
        int i = 0;

        while(!this.path.isPathEmpty()){
        	
            ++i;

            // This is the offending line - timeout limit changed from 200 to 5000.
            if(i >= 5000){
                break;
            }

            PathPoint pathpoint1 = this.path.dequeue();

            if(pathpoint1.equals(end)){
                pathpoint = end;
                break;
            }

            if(pathpoint1.distanceManhattan(end) < pathpoint.distanceManhattan(end)){
                pathpoint = pathpoint1;
            }

            pathpoint1.visited = true;
            int j = this.nodeProcessor.findPathOptions(this.pathOptions, pathpoint1, end, range);

            for(int k = 0; k < j; ++k){
            	
                PathPoint pathpoint2 = this.pathOptions[k];
                float f = pathpoint1.distanceManhattan(pathpoint2);
                pathpoint2.distanceFromOrigin = pathpoint1.distanceFromOrigin + f;
                pathpoint2.cost = f + pathpoint2.costMalus;
                float f1 = pathpoint1.totalPathDistance + pathpoint2.cost;

                if(pathpoint2.distanceFromOrigin < range && (!pathpoint2.isAssigned() || f1 < pathpoint2.totalPathDistance)){
                	
                    pathpoint2.previous = pathpoint1;
                    pathpoint2.totalPathDistance = f1;
                    pathpoint2.distanceToNext = pathpoint2.distanceManhattan(end) + pathpoint2.costMalus;

                    if(pathpoint2.isAssigned()){
                        this.path.changeDistance(pathpoint2, pathpoint2.totalPathDistance + pathpoint2.distanceToNext);
                    }else{
                        pathpoint2.distanceToTarget = pathpoint2.totalPathDistance + pathpoint2.distanceToNext;
                        this.path.addPoint(pathpoint2);
                    }
                }
            }
        }

        if(pathpoint == start){
            return null;
        }else{
            Path path = this.createEntityPath(start, pathpoint);
            return path;
        }
    }

    /**
     * Returns a new PathEntity for a given start and end point
     */
    private Path createEntityPath(PathPoint start, PathPoint end){
    	
        int i = 1;

        for(PathPoint pathpoint = end; pathpoint.previous != null; pathpoint = pathpoint.previous){
            ++i;
        }

        PathPoint[] points = new PathPoint[i];
        PathPoint pathpoint1 = end;
        --i;

        for(points[i] = end; pathpoint1.previous != null; points[i] = pathpoint1){
            pathpoint1 = pathpoint1.previous;
            --i;
        }

        return new Path(points);
    }
}