package jackiesvgprocessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 6/19/17.
 */
public class Region {
    private ArrayList<Point> boundary = new ArrayList<>();

    public Region(ArrayList<Point> boundary) {
        this.boundary = boundary;
    }

    public ArrayList<Point> getBoundary() {
        return boundary;
    }

    public boolean insideRegion(Point testPoint) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = boundary.size() - 1; i <  boundary.size(); j = i++) {
            if (( boundary.get(i).getY() > testPoint.getY()) != ( boundary.get(j).getY() > testPoint.getY())
                    && (testPoint.getX() < ( boundary.get(j).getX() - boundary.get(i).getX()) * (testPoint.getY() - boundary.get(i).getY())
                    / ( boundary.get(j).getY() - boundary.get(i).getY()) + boundary.get(i).getX())) {
                result = !result;
            }
        }
        return result;
    }

    public List<SvgPathCommand> fitCommandsToRegion(List<SvgPathCommand> commandsOriginal) {
        List<SvgPathCommand> commandsTrimed = new ArrayList<>();
        int start = 0;
        while ((start < commandsOriginal.size()) && (!insideRegion(commandsOriginal.get(start).getDestinationPoint())) )
            start++;
        if (start >= commandsOriginal.size())
            return commandsTrimed;
        int end = commandsOriginal.size() - 1;
        while ((end >= 0) && (!insideRegion(commandsOriginal.get(end).getDestinationPoint())))
            end--;

        int index = start;
        int outsideStartIndex = -1, outsideEndIndex;
        while (index <= end) {
            if (outsideStartIndex != -1) {
                Point lastIn = commandsOriginal.get(outsideStartIndex).getDestinationPoint();
                Point nextIn = commandsOriginal.get(index).getDestinationPoint();
                int indexToLast = nearestBoundaryPointIndex(lastIn), indexToNext = nearestBoundaryPointIndex(nextIn);
                if (indexToLast <= indexToNext) {
                    for (int i = indexToLast; i <= indexToNext; i++)
                        commandsTrimed.add(new SvgPathCommand(boundary.get(i), SvgPathCommand.CommandType.LINE_TO));
                } else {
                    for (int i = indexToLast; i < boundary.size(); i++)
                        commandsTrimed.add(new SvgPathCommand(boundary.get(i), SvgPathCommand.CommandType.LINE_TO));
                    for (int i = 0; i <= indexToNext; i++)
                        commandsTrimed.add(new SvgPathCommand(boundary.get(i), SvgPathCommand.CommandType.LINE_TO));
                }
            }

            while ((index <= end) && insideRegion(commandsOriginal.get(index).getDestinationPoint())) {
                commandsTrimed.add(commandsOriginal.get(index));
                index++;
            }
            outsideStartIndex = index;
            while ((index <= end) && (!insideRegion(commandsOriginal.get(index).getDestinationPoint())))
                index++;
        }
        return commandsTrimed;
    }

    public int nearestBoundaryPointIndex(Point inputPoint) {
        double distMin = Double.MAX_VALUE;
        int ans = -1;
        for (int i = 0; i < boundary.size(); i++ ) {
            if (Double.compare(Point.getDistance(inputPoint, boundary.get(i)), distMin) <= 0) {
                distMin = Point.getDistance(inputPoint, boundary.get(i));
                ans = i;
            }
        }
        return ans;
    }
}
