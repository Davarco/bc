package artemis;

import battlecode.common.*;
import static artemis.Channels.*;
import static artemis.RobotPlayer.*;
import static artemis.Util.*;

public class Nav {

    static void moveToPriorityLoc() {

        try {

            // Move to location broadcasted
            float x = rc.readBroadcastFloat(PRIORITY_X);
            float y = rc.readBroadcastFloat(PRIORITY_Y);
            moveTowardsLocation(new MapLocation(x, y));
            //System.out.println("Moving towards priority location.");
            rc.setIndicatorLine(rc.getLocation(), new MapLocation(x, y), 80, 220, 120);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void moveTowardsEnemy(RobotInfo[] robotInfo) {

        try {

            // Move towards first enemy
            // TODO: This is pretty bad, needs to be fixed before robots get clusterfucked
            moveTowardsLocation(robotInfo[0].getLocation());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void moveTowardsLocation(MapLocation loc) {

        try {

            // Add 90 deg if can't move in that dir
            Direction dir = rc.getLocation().directionTo(loc);
            if (rc.canMove(dir)) {
                rc.move(dir);
            } else {
                if (rc.canMove(dir.rotateLeftDegrees(90))) {
                    rc.move(dir.rotateLeftDegrees(90));
                } else if (rc.canMove(dir.rotateRightDegrees(90))) {
                    rc.move(dir.rotateRightDegrees(90));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean avoidMapBoundaries() {

        try {

            // Move in opposite of first wall direction
            final float GARDENER_SPACE_RADIUS = 4.0f;
            float radians = 0;
            while (radians <= Math.PI*2) {
                MapLocation loc = rc.getLocation().add(new Direction(radians), GARDENER_SPACE_RADIUS);
                if (!rc.onTheMap(loc)) {
                    tryMove(rc.getLocation().directionTo(loc).opposite(), 5, 36);
                    return true;
                }
                radians += (float)(Math.PI/3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    static void evadeRobotGroup(RobotInfo[] robotInfo) {

        // Move in opposite direction
        try {
            tryMove(rc.getLocation().directionTo(robotInfo[0].getLocation()).opposite(), 5, 36);
        } catch (Exception e) {
            System.out.println("Evade robot exception.");
            e.printStackTrace();
        }
    }

    static void dodgeIncomingBullets(BulletInfo[] incomingBullets) {

        try {

            for (BulletInfo info: incomingBullets) {
                if (willCollideWithMe(info)) {

                    // Find movement that won't collide
                    float strideRadius = rc.getType().strideRadius;
                    float x = rc.getLocation().x;
                    float y = rc.getLocation().y;

                    if (!willCollideWithMe(info, x, y+strideRadius) && rc.canMove(Direction.getNorth())) {
                        rc.move(Direction.getNorth());
                        return;
                    } else if (!willCollideWithMe(info, x+strideRadius, y) && rc.canMove(Direction.getEast())) {
                        rc.move(Direction.getEast());
                        return;
                    } else if (!willCollideWithMe(info, x, y-strideRadius) && rc.canMove(Direction.getSouth())) {
                        rc.move(Direction.getSouth());
                        return;
                    } else if (!willCollideWithMe(info, x-strideRadius, y) && rc.canMove(Direction.getWest())) {
                        rc.move(Direction.getWest());
                        return;
                    } else {
                        //System.out.println("Failed to dodge bullet!");
                        Direction dir = randomDirection();
                        while (!rc.canMove(dir)) {
                            dir = randomDirection();
                        }

                        rc.move(dir);
                        return;
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Dodge failed!");
            e.printStackTrace();
        }
    }

    /*
    Preset stuff...
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }
}
