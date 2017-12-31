package com.junsung.wpi3;

import android.util.Log;

import com.junsung.wpi3.MapElement.Edge;
import com.junsung.wpi3.MapElement.Junction;
import com.junsung.wpi3.MapElement.User;

public class MapData {
    User user;
    private Junction[] junctions;
    private Edge[] edges;
    private int numOfJunctions;
    private int numOfEdges;

    public MapData() {
        numOfJunctions = numOfEdges = 0;
        junctions = new Junction[numOfJunctions];
        edges = new Edge[numOfEdges];
        user = new User(0, 0);
    }

    MapData(Junction[] junctions, Edge[] edges) {
        this.junctions = junctions;
        this.edges = edges;
        numOfJunctions = junctions.length;
        numOfEdges = edges.length;
        user = new User(0, 0);

        double minDist = 1000000;
        int minJunctionIndex = -1;
        for(int i = 0 ; i < numOfJunctions; i++) {
            if(minDist > user.getDist(junctions[i])) {
                minJunctionIndex = i;
                minDist = user.getDist(junctions[i]);
            }
        }
        user.lastJunction = junctions[minJunctionIndex];
    }

    public void addJunction(Junction junction) {
        Junction[] tmp = junctions;
        numOfJunctions++;
        junctions = new Junction[numOfJunctions];
        System.arraycopy(tmp, 0, junctions, 0, numOfJunctions - 1);
        junctions[numOfJunctions-1] = junction;
    }

    public void addEdge(Edge edge) {
        Edge[] tmp = edges;
        numOfEdges++;
        edges = new Edge[numOfEdges];
        System.arraycopy(tmp, 0, edges, 0, numOfEdges - 1);
        edges[numOfEdges-1] = edge;
    }

    void update(double x, double y) {
        user.x = x;
        user.y = y;

        if(user.isInJunction) {
            if(user.lastJunction.getDist(user) > user.lastJunction.radius) {
                user.lastGate = user.getNearestGate(user.lastJunction.gates);
                user.isInJunction = false;
            }
        } else {
            if(user.lastJunction.getDist(user) < user.lastJunction.radius)
                user.isInJunction = true;
        }
    }

    void correction(String option) {
        double minDist = 100000;
        int nearestJunctionIndex = -1;

        switch (option) {
            case "easy":
                for(int i = 0; i < numOfJunctions; i++) {
                    if(junctions[i].getDist(user) < minDist) {
                        minDist = junctions[i].getDist(user);
                        nearestJunctionIndex = i;
                    }
                }
                user.x = junctions[nearestJunctionIndex].x;
                user.y = junctions[nearestJunctionIndex].y;
                break;
            case "hard":
                for(int i = 0 ; i < user.lastJunction.numOfJunctions; i++) {
                    if(user.lastJunction.junctions[i].getDist(user) < minDist) {
                        minDist = user.lastJunction.junctions[i].getDist(user);
                        nearestJunctionIndex = i;
                    }
                }
                if(user.getDist(user.lastJunction) < minDist) { // user is closer to last junction.
                    //user's point will be last junction
                    user.x = user.lastJunction.x;
                    user.y = user.lastJunction.y;
                } else {
                    user.x = user.lastJunction.junctions[nearestJunctionIndex].x;
                    user.y = user.lastJunction.junctions[nearestJunctionIndex].y;
                    user.lastJunction = user.lastJunction.junctions[nearestJunctionIndex];
                }
                break;
            case "gate":
                user.lastJunction = user.getDist(user.lastGate.srcJunction) < user.getDist(user.lastGate.destJunction) ?
                        user.lastGate.srcJunction : user.lastGate.destJunction;
                user.x = user.lastJunction.x;
                user.y = user.lastJunction.y;


                break;
        }
    }

    double correction(double d) {
        double minDist = 100000;
        int nearestJunctionIndex;
        double headingDirection = d;

        Junction tmp = user.lastJunction;
        user.lastJunction = user.getDist(user.lastGate.srcJunction) < user.getDist(user.lastGate.destJunction) ?
                user.lastGate.srcJunction : user.lastGate.destJunction;
        user.x = user.lastJunction.x;
        user.y = user.lastJunction.y;

        if(tmp.x == user.x && tmp.y == user.y) { //same position

        } else if (tmp.x == user.x && tmp.y != user.y) {
            if(tmp.y > user.y) { // went straight left
                headingDirection = -90;
            } else { // went straight right
                headingDirection = 90;
            }
        } else if (tmp.x != user.x && tmp.y == user.y) {
            if(tmp.x > user.x) { // went straight down
                headingDirection = 180;
            } else { // went straight up
                headingDirection = 0;
            }
        } else {
            double m = (user.y - tmp.y) / (user.x - tmp.x);
            //
        }

        return headingDirection;
    }
}