package thresholds;

import java.util.*;

/**
 * @author tristonK 2023/3/9
 */

//具体策略类
public class ExtremaStrategy implements ThresholdsStrategy {

    @Override
    public List<List<Double>> calculateThresholds(HashMap<Double, Integer> diff2Freq, List<Double> orderedKeyList, int thresholdsNum, int minThresholdsSize, double freqBoundary, double indexBoundary) {
        List<Double> resLeft = new ArrayList<>();
        List<Double> resRight = new ArrayList<>();
        double ERR = 0.000000001;
        int count = 0;
        int tmpIndex = 0;
        double maxScore = -1;
        int index = 0;
        int partitionLeftIndex = 0;
        int partitionRightIndex = 0;
        long totalFreqSum = 0;


        if (diff2Freq.size() <= 1) {
            resLeft.add(0.0);
            if (ERR < orderedKeyList.get(0)) {
                resLeft.add(orderedKeyList.get(0));
            }
            List<List<Double>> sameRes = new ArrayList<>();
            sameRes.add(resLeft);
            sameRes.add(resLeft);
            return sameRes;
        }

        if (diff2Freq.size() < thresholdsNum) {
            if (ERR < orderedKeyList.get(0)) {
                resLeft.add(0.0);
            }
            for (int i = 0; i < diff2Freq.size() / 2; i++) {
                resLeft.add(orderedKeyList.get(i));
            }
            for (int i = diff2Freq.size() / 2; i < diff2Freq.size(); i++) {
                resRight.add(orderedKeyList.get(i));
            }
            List<List<Double>> allRes = new ArrayList<>();
            allRes.add(resLeft);
            allRes.add(resRight);
            return allRes;
        }



        for (int i = 0; i < diff2Freq.size(); i++) {
            totalFreqSum += diff2Freq.get(orderedKeyList.get(i));
        }

        long tmpCount = 0;
        for (int i = 0; ; i++) {
            tmpCount += diff2Freq.get(orderedKeyList.get(i));
            if (totalFreqSum * freqBoundary <= tmpCount || i >= diff2Freq.size() * indexBoundary) {
                partitionLeftIndex = i;
                tmpCount = 0;
                break;
            }
        }

        for (int i = diff2Freq.size() - 1; i > partitionLeftIndex + 1; i--) {
            tmpCount += diff2Freq.get(orderedKeyList.get(i));
            if (totalFreqSum * freqBoundary <= tmpCount) {
                partitionRightIndex = i;
                break;
            }
        }


        resLeft.add(0.0);
        count++;


        while (count < Math.ceil((thresholdsNum + 1) / 2.0)) {
            for (int i = tmpIndex + 1; i < partitionLeftIndex + 1; i++) {
                double intervalLength = orderedKeyList.get(i) - orderedKeyList.get(tmpIndex);
                long freqSum = 0;
                for (int j = tmpIndex; j <= i; j++) {
                    freqSum += diff2Freq.get(orderedKeyList.get(j));
                }
                double score = freqSum / intervalLength;
                if (maxScore < score) {
                    maxScore = score;
                    index = i;
                }
            }
            resLeft.add(orderedKeyList.get(index));
            tmpIndex = index;
            maxScore = -1;
            count++;
        }





        tmpIndex = partitionRightIndex;
        while (count < thresholdsNum) {
            for (int i = tmpIndex; i < diff2Freq.size() - 1; i++) {
                double intervalLength = orderedKeyList.get(diff2Freq.size() - 1) - orderedKeyList.get(i);//当前阈值与右端点阈值的区间长度
                int freqSum = 0;
                for (int j = i + 1; j < diff2Freq.size(); j++) {
                    freqSum += diff2Freq.get(orderedKeyList.get(j));
                }
                double score = freqSum / intervalLength;
                if (maxScore < score) {
                    maxScore = score;
                    index = i;
                }
            }
            resRight.add(orderedKeyList.get(index));
            tmpIndex = index + 1;
            maxScore = -1;
            count++;
        }


        Set<Double> resSet = new HashSet<>();
        List<Double> resLeftWithoutDuplicates = new ArrayList<>();
        List<Double> resRightWithoutDuplicates = new ArrayList<>();
        for (Double i : resLeft) {
            if (resSet.add(i)) {
                resLeftWithoutDuplicates.add(i);
            }
        }
        for (Double i : resRight) {
            if (resSet.add(i)) {
                resRightWithoutDuplicates.add(i);
            }
        }
        List<List<Double>> resWithoutDupulicates = new ArrayList<>();
        resWithoutDupulicates.add(resLeftWithoutDuplicates);
        resWithoutDupulicates.add(resRightWithoutDuplicates);

        return resWithoutDupulicates;
    }

}
