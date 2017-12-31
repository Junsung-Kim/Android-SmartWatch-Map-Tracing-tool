package com.junsung.wpi3;

class MapElement {
    static class Node {
        double x;
        double y;

        Node(double x, double y) {
            this.x = x;
            this.y = y;
        }

        double getDist(double x, double y) {
            return Math.sqrt(sqr(this.x-x) + sqr(this.y-y));
        }

        double getDist(Node node) {
            return Math.sqrt(sqr(this.x-node.x) + sqr(this.y-node.y));
        }

        double sqr(double d) { return d * d; }
    }

    static class Junction extends Node {
        double radius;
        Gate[] gates;
        Junction[] junctions;
        int numOfGates;
        int numOfJunctions;

        Junction(double x, double y, double radius) {
            super(x, y);
            this.radius = radius;
            numOfGates = 0;
            gates = new Gate[numOfGates];
            numOfJunctions = 0;
            junctions = new Junction[numOfJunctions];
        }

        void addGate(Junction junction) {
            Gate[] tmp = gates;
            numOfGates++;
            gates = new Gate[numOfGates];
            System.arraycopy(tmp, 0, gates, 0, numOfGates - 1);

            double tmpX, tmpY;

            if(junction.x == this.x) {
                if(junction.y > this.y)
                    tmpY = this.y + this.radius;
                else
                    tmpY = this.y - this.radius;
                tmpX = this.x;
            } else if(junction.y == this.y) {
                if(junction.x > this.x)
                    tmpX = this.x + this.radius;
                else
                    tmpX = this.x - this.radius;
                tmpY =this.y;
            } else {
                double x1, y1, x2, y2;
                double slope = (junction.y - this.y)/(junction.x - this.x);

                x1 = this.radius / Math.sqrt(1 + sqr(slope)) + this.x;
                x2 = -1 * this.radius / Math.sqrt(1 + sqr(slope)) + this.x;
                y1 = this.radius / Math.sqrt(1 + sqr(1.0/slope)) + this.y;
                y2 = -1 * this.radius / Math.sqrt(1 + sqr(1.0/slope)) + this.y;

                tmpX = Math.abs(junction.x - x1) < Math.abs(junction.x - x2) ? x1 : x2;
                tmpY = Math.abs(junction.y - y1) < Math.abs(junction.y - y2) ? y1 : y2;
            }

            gates[numOfGates-1] = new Gate(tmpX, tmpY, this, junction);
        }

        void addJunction(Junction junction) {
            Junction[] tmp = junctions;
            numOfJunctions++;
            junctions = new Junction[numOfJunctions];
            System.arraycopy(tmp, 0, junctions, 0, numOfJunctions - 1);
            junctions[numOfJunctions-1] = junction;
        }
    }

    static class Gate extends Node {
        Junction srcJunction;
        Junction destJunction;

        Gate(double x, double y, Junction srcJunction, Junction destJunction) {
            super(x, y);
            this.srcJunction = srcJunction;
            this.destJunction = destJunction;
        }
    }

    static class User extends Node {
        Junction lastJunction;
        Gate lastGate;
        boolean isInJunction;

        User(double x, double y) {
            super(x, y);
            lastJunction = null;
            lastGate = null;
            isInJunction = true;
        }

        Gate getNearestGate(Gate[] gates) {
            double minDist = 1000000;
            Gate nearestGate = null;
            for (Gate gate : gates) {
                if (minDist > this.getDist(gate)) {
                    minDist = this.getDist(gate);
                    nearestGate = gate;
                }
            }

            return nearestGate;
        }
    }

    static class Edge {
        //ax + by + c = 0, coefficients
        double a, b, c;
        Junction junctionA, junctionB;
        double weight;

        Edge(Junction junctionA, Junction junctionB) {
            this.junctionA = junctionA;
            this.junctionB = junctionB;
            this.weight = junctionA.getDist(junctionB);
            this.getCoef();
            junctionA.addGate(junctionB);
            junctionA.addJunction(junctionB);
            junctionB.addGate(junctionA);
            junctionB.addJunction(junctionA);
        }

        private void getCoef() {
            if(junctionA.x == junctionB.x) {
                a = 1; b = 0; c = -1 * junctionA.x;
            } else if (junctionA.y == junctionB.y) {
                a = 0; b = 1; c = -1 * junctionA.y;
            } else {
                a = (junctionA.y - junctionB.y) / (junctionA.x - junctionB.x);
                b = -1;
                c = (-1 * a * junctionA.x) + junctionA.y;
            }
        }

        public double getDist(Node node) {
            return Math.abs(a * node.x + b * node.y + c) / Math.sqrt(sqr(a) + sqr(b));
        }

        private double sqr(double d) { return d*d; }
    }
}