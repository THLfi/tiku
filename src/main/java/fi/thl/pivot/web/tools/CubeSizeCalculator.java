package fi.thl.pivot.web.tools;

import java.util.List;

import fi.thl.pivot.model.DimensionNode;

public class CubeSizeCalculator {

    private CubeSizeCalculator() {
    }

    public static int[] calculateNumberOfNodesInEachLevel(List<DimensionNode> headers) {
        int index = 0;
        int[] nodesInLevel = new int[headers.size()];
        for (DimensionNode level : headers) {
            nodesInLevel[index++] = level.getChildren().size() + 1;
        }
        return nodesInLevel;
    }

    public static int[] calculateRepetitionReverse(int[] nodesInLevel) {
        if (nodesInLevel.length == 0) {
            return new int[0];
        }
        int[] repeatCount = new int[nodesInLevel.length];
        int lastCount = 1;
        repeatCount[repeatCount.length - 1] = 1;
        for (int i = nodesInLevel.length - 2; i >= 0; --i) {
            repeatCount[i] = lastCount * nodesInLevel[i + 1];
            lastCount = repeatCount[i];
        }
        return repeatCount;
    }

    public static int[] calculateRepetition(int[] nodesInLevel) {
        if (nodesInLevel.length == 0) {
            return new int[0];
        }
        int[] repeatCount = new int[nodesInLevel.length];
        int lastCount = 1;
        repeatCount[0] = 1;
        for (int i = 1; i < repeatCount.length; ++i) {
            repeatCount[i] = lastCount * nodesInLevel[i - 1];
            lastCount = repeatCount[i];
        }
        return repeatCount;
    }
}
