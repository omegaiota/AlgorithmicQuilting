package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Pebble
 */
public class PebbleRenderer extends PatternRenderer {
    private List<SvgPathCommand> renderedCommands = new ArrayList<>();
    private TreeNode<Point> treeRoot;

    public PebbleRenderer(TreeNode<Point> treeRoot) {
        super(treeRoot);
        this.treeRoot = treeRoot;
    }

    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }

    @Override
    public void pebbleFilling() {
        double dist = Point.getDistance(treeRoot.getData(), treeRoot.getChildren().get(0).getData());
        for (TreeNode<Point> firstChildren : treeRoot.getChildren()) {
            if (Double.compare((Point.getDistance(treeRoot.getData(), firstChildren.getData())), dist) < 0)
                dist = Point.getDistance(treeRoot.getData(), firstChildren.getData());
        }
        dist = dist * 0.66;
        System.out.println("Command distance is" + dist);

        /* Order children tobe counterclockwise*/
        TreeTraversal.treeOrdering(treeRoot, null);

        //landFillTraverse(treeRoot, null, dist);
        renderedCommands.add(new SvgPathCommand(new Point(treeRoot.getData().x + dist, treeRoot.getData().y), SvgPathCommand.CommandType.MOVE_TO));
        HashMap<Point, Double> radiusList = new HashMap<>();
        List<CircleBound> determinedBounds = new ArrayList<>();
        treeRoot.setBoundingCircle(new CircleBound(dist, treeRoot.getData()));

        // first determination loop, make sure each pebble touches one pebble
        pebbleRenderDetermineRadii(treeRoot, dist, determinedBounds);
        pebbleRenderDraw(treeRoot, 0);
        SvgFileProcessor.outputSvgCommands(renderedCommands, "beforeAdjustion");
        renderedCommands.clear();
        renderedCommands.add(new SvgPathCommand(new Point(treeRoot.getData().x + dist, treeRoot.getData().y), SvgPathCommand.CommandType.MOVE_TO));

        // second determination loop, make sure each pebble touches two pebbles
        pebbleAdjustTreenode(treeRoot, dist, determinedBounds);
        pebbleRenderDraw(treeRoot, 0);
        SvgFileProcessor.outputSvgCommands(renderedCommands, "afterfirstadjust");
        renderedCommands.clear();
        renderedCommands.add(new SvgPathCommand(new Point(treeRoot.getData().x + dist, treeRoot.getData().y), SvgPathCommand.CommandType.MOVE_TO));

        //
        System.out.println("First iteration of 3 touch");
        pebbleSecondAdjustTreenode(treeRoot, dist, determinedBounds);
        System.out.println("Second iteration of 3 touch");

        pebbleSecondAdjustTreenode(treeRoot, dist, determinedBounds);

        pebbleRenderDraw(treeRoot, 0);
    }


    /* We push inflate treenodes that touch two pebbles in the direciton of bisectors
*/
    public void pebbleSecondAdjustTreenode(TreeNode<Point> thisNode, double dist, final List<CircleBound> determinedBounds) {
        Point thisCenter = thisNode.getData();
        double r = thisNode.getBoundingCircle().getRadii();
        Point pebble1 = null, pebble2 = null, bestPoint = null;
        double r1 = 0, r2 = 0, best = r;
        int touchCount = 0;

        for (CircleBound bound : determinedBounds)
            if (bound.getCenter() != thisCenter) {
                if (bound.touches(thisNode.getBoundingCircle())) {
                    touchCount++;
                    if (touchCount == 1) {
                        r1 = bound.getRadii();
                        pebble1 = bound.getCenter();
                    } else if (touchCount == 2) {
                        r2 = bound.getRadii();
                        pebble2 = bound.getCenter();
                    } else
                        break;
                }
            }

        if (touchCount == 2) {
            double angle1 = Point.getAngle(thisCenter, pebble1);
            double angle2 = Point.getAngle(thisCenter, pebble2);
            double angleBTW = angle1 - angle2;
            double rotationAngle = -1 * angleBTW / 2;
            if (angleBTW < 0)
                rotationAngle += Math.PI;
            int ITERATION = 100;
            double shiftLen;
            for (int dir = 0; dir < 2; dir++) {
                if (dir == 1)
                    rotationAngle = 2 * Math.PI - rotationAngle;//second iteration, opposite direction of bisector
                //first iteration, one direction
                for (int i = 0; i < ITERATION; i++) {
                    shiftLen = dist / ITERATION * i;
                    Point newPointAngle = new Point(pebble1.x, pebble1.y).rotateAroundCenter(thisCenter, rotationAngle);
                    Point newPoint = Point.intermediatePointWithLen(thisCenter, newPointAngle, shiftLen);
                    double newRadii1 = Point.getDistance(newPoint, pebble1) - r1;
                    double newRadii2 = Point.getDistance(newPoint, pebble2) - r2;
                    double newRadii = Math.min(newRadii1, newRadii2);

                    boolean valid = (newRadii > 0) && (Math.abs(newRadii1 - newRadii2) < 0.05);
                    if (valid) {
                        for (CircleBound b : determinedBounds) {
                            Point pCenter = b.getCenter();
                            if ((pCenter != thisCenter) && (pCenter != pebble1) && (pCenter != pebble2)) {
                                //TODO: change call to circle bound
                                if (b.touches(new CircleBound(newRadii, newPoint))) {
                                    valid = false;
                                    break;
                                }
                            }
                        }
                    }

                    if (valid) {
                        if (newRadii > best) {
                            best = newRadii;
                            bestPoint = newPoint;
                        }
                    } else {
                        break;
                    }

                }
            }

            if (bestPoint != null) {
                System.out.println("adjusted");
                determinedBounds.remove(thisNode.getBoundingCircle());
                thisNode.setData(bestPoint);
                thisNode.setBoundingCircle(new CircleBound(best, thisNode.getData()));
                determinedBounds.add(thisNode.getBoundingCircle());
            }

        }

        for (TreeNode<Point> child : thisNode.getChildren()) {
            pebbleSecondAdjustTreenode(child, dist, determinedBounds);
        }
    }

    /* Since all internal nodes touch at least its parent and one of its children, we only adjust positions of treenodes. We
    push each treenode towards the direction of parent-self line until it touches a treeenode.
    */
    public void pebbleAdjustTreenode(TreeNode<Point> thisNode, double dist, final List<CircleBound> determinedBounds) {
        if (thisNode.getChildren().size() == 0) {
            // leafnode, adjust position
            double shiftLen;
            int ITERATION = 100;
            double thisParentDist = Point.getDistance(thisNode.getParent().getData(), thisNode.getData());
            double thisRadii = thisNode.getBoundingCircle().getRadii();
            double tempRadii;
            Point bestCenter = thisNode.getData();
            double bestRadii = thisRadii;
            boolean isValid;
            for (int i = 0; i < ITERATION; i++) {
                isValid = true;
                shiftLen = dist / 100 * i;
                Point newCenter = Point.intermediatePointWithLen(thisNode.getParent().getData(), thisNode.getData(), thisParentDist + shiftLen);
                tempRadii = thisRadii + shiftLen;
                for (CircleBound b : determinedBounds) {
                    boolean isSelfTest = thisNode.getData() == b.getCenter();
                    boolean isParentTest = thisNode.getParent().getData() == b.getCenter();
                    if ((!isSelfTest) || (!isParentTest) || Double.compare(Point.getDistance(b.getCenter(), newCenter) - (b.getRadii() + tempRadii), -0.05) < 0) {
                        isValid = false;
                        break;
                    }
                }

                if (isValid) {
                    if (tempRadii > bestRadii) {
                        bestRadii = tempRadii;
                        bestCenter = newCenter;
                    }

                }
            }
            determinedBounds.remove(thisNode.getBoundingCircle());
            thisNode.setData(bestCenter);
            thisNode.setBoundingCircle(new CircleBound(bestRadii, thisNode.getData()));
            determinedBounds.add(thisNode.getBoundingCircle());
        }
        for (TreeNode<Point> child : thisNode.getChildren()) {
            pebbleAdjustTreenode(child, dist, determinedBounds);
        }
    }

    public void pebbleRenderDraw(TreeNode<Point> thisNode, int angle) {
        HashMap<Integer, TreeNode<Point>> degreeTreeNodeMap = new HashMap<>();
        boolean[] degreeTable = new boolean[360];
        Arrays.fill(degreeTable, false);
        /* Record the direction of the children*/
        for (TreeNode<Point> child : thisNode.getChildren()) {
            int thisAngle = (int) Math.toDegrees(Point.getAngle(thisNode.getData(), child.getData()));
            degreeTable[thisAngle] = true;
            degreeTreeNodeMap.put(thisAngle, child);
        }

        int gap = 15;
        Point zeroAnglePoint = new Point(thisNode.getData().x + thisNode.getBoundingCircle().getRadii(), thisNode.getData().y);
        for (Integer offset = 0; offset < 360; offset += gap) {
            int currentAngle = (angle + offset) % 360;
            TreeNode<Point> child;
            Point thisPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(currentAngle));
            renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));

            for (Integer j = currentAngle; j < currentAngle + gap; j++) {
                int searchAngle = j % 360;
                if ((child = degreeTreeNodeMap.get(searchAngle)) != null) {
                    int newAngle = (searchAngle + 180) % 360;
                    // Find the minimum radii that won't cause conflict issue
                    thisPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(j));
                    renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
                    pebbleRenderDraw(child, newAngle);
                    renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
                }
            }
            renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));


        }

        Point thisPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(angle));
        renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));

    }

    public void pebbleRenderDetermineRadii(TreeNode<Point> thisNode, double dist, final List<CircleBound> determinedBounds) {
        determinedBounds.add(thisNode.getBoundingCircle());
        for (TreeNode<Point> child : thisNode.getChildren()) {
            double adjustedRadii = Point.getDistance(thisNode.getData(), child.getData()) - dist;
            // Find the minimum radii that won't cause conflict issue
            List<Point> childrenPoint = new ArrayList<>();
            for (TreeNode<Point> firstChildren : child.getChildren()) {
                double distanceBetween = Point.getDistance(child.getData(), firstChildren.getData());
                if (Double.compare(distanceBetween, 0.002) > 0 && Double.compare(distanceBetween, adjustedRadii) < 0)
                    adjustedRadii = distanceBetween;
                childrenPoint.add(firstChildren.getData());
            }

            boolean shortLineSegment = false;

            //loop through pebbles that have drawn already to adjust radii
            childrenPoint.add(child.getData());
            final TreeNode<Point> thisChild = child;
            ArrayList<CircleBound> sortedBoundByDist = new ArrayList<>(determinedBounds);
            sortedBoundByDist.sort((b1, b2) -> (
                    new Double(Point.getDistance(thisChild.getData(), b1.getCenter()) - b1.getRadii()).compareTo(
                            Point.getDistance(thisChild.getData(), b2.getCenter()) - b2.getRadii())
            ));

            int adjustmentSize = (sortedBoundByDist.size() > 5) ? 5 : sortedBoundByDist.size();
            for (int i = 0; i < adjustmentSize; i++) {
                double distanceBetween = Point.getDistance(child.getData(), sortedBoundByDist.get(i).getCenter()) - sortedBoundByDist.get(i).getRadii();

                if ((distanceBetween > 0) && Double.compare(distanceBetween, adjustedRadii) < 0 && (Double.compare(adjustedRadii - distanceBetween, 0.01) > 0)) {
                    adjustedRadii = distanceBetween;
                    assert adjustedRadii > 0;
                    shortLineSegment = true;

                }
            }

            if (shortLineSegment) {
                // Strategy 1: insert a new pebble at all short line segments
                double distBtwChildParent = Point.getDistance(thisNode.getData(), child.getData());
                assert (distBtwChildParent - dist - adjustedRadii) > 0;
                double newRadii = (distBtwChildParent - dist - adjustedRadii) / 2.0;
                Point middlePoint = Point.intermediatePointWithLen(thisNode.getData(), child.getData(), newRadii + dist);
                if (!Point.onLine(thisNode.getData(), child.getData(), middlePoint)) {
                    assert newRadii + dist < distBtwChildParent;
                }
                TreeNode<Point> midTreeNode = new TreeNode<>(middlePoint, new ArrayList<>());
                midTreeNode.setParent(thisNode);
                midTreeNode.addChild(child);
                thisNode.removeChild(child);
                thisNode.addChild(midTreeNode);
                midTreeNode.setBoundingCircle(new CircleBound(newRadii, midTreeNode.getData()));
                pebbleRenderDetermineRadii(midTreeNode, newRadii, determinedBounds);
            } else {
                child.setBoundingCircle(new CircleBound(adjustedRadii, child.getData()));
                pebbleRenderDetermineRadii(child, adjustedRadii, determinedBounds);
            }
        }

    }


}
