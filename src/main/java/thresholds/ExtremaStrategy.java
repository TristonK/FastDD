package thresholds;

import java.util.*;

/**
 * @author tristonK 2023/3/9
 */

//具体策略类
public class ExtremaStrategy implements ThresholdsStrategy {

    /**
     * @param diff2Freq         存储了diff-freq的hashmap
     * @param orderedKeyList    存储了升序排列的diff值
     * @param thresholdsNum     返回的阈值数量，可能由于去重而不足
     * @param minThresholdsSize 最小的阈值返回数量
     * @param freqBoundary      频度阈值，用于划分左右区间
     * @param indexBoundary     左阈值边界下标，避免右侧阈值频度很大却被划分到resLeft的情况
     * @return 返回List<List < Double>>类型，包括左区间阈值和右区间阈值
     */
    @Override
    public List<List<Double>> calculateThresholds(HashMap<Double, Integer> diff2Freq, List<Double> orderedKeyList, int thresholdsNum, int minThresholdsSize, double freqBoundary, double indexBoundary) {
        //TODO:修改res为两部分，一部分为左阈值(<)，另一部分为右阈值(>)，若阈值数量小于等于minThresholdsSize，则返回全部阈值，若阈值为单个，则返回两个相同值
        List<Double> resLeft = new ArrayList<>();//记录左侧阈值
        List<Double> resRight = new ArrayList<>();//记录右侧阈值
        double ERR = 0.000000001;
        int count = 0;//记录当前选取的阈值数量
        int tmpIndex = 0;//记录前一轮选中的阈值下标
        double maxScore = -1;
        int index = 0;//记录达到maxScore的阈值在orderedKeyList中的下标
        int partitionLeftIndex = 0;//用于划分左阈值选取空间
        int partitionRightIndex = 0;//用于划分右阈值选取空间
        long totalFreqSum = 0;//freq累计和

        //左侧阈值一定包含0

        //阈值数量为1时，返回两个同样的阈值
        if (diff2Freq.size() <= 1) {
            resLeft.add(0.0);
            if (ERR < orderedKeyList.get(0)) {//左端点阈值为不0，加入res中
                resLeft.add(orderedKeyList.get(0));
            }
            List<List<Double>> sameRes = new ArrayList<>();
            sameRes.add(resLeft);
            sameRes.add(resLeft);
            return sameRes;
        }

        //阈值数量小于thresholdsNum,返回全部阈值，左右各一半
        if (diff2Freq.size() < thresholdsNum) {
            if (ERR < orderedKeyList.get(0)) {//左端点阈值为不0，将0阈值加入resleft中
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


        //TODO:统计累加频度,选定左侧和右侧区间点

        for (int i = 0; i < diff2Freq.size(); i++) {
            totalFreqSum += diff2Freq.get(orderedKeyList.get(i));//累加总阈值频度，和freqBoundary共同确定左右阈值边界
        }

        long tmpCount = 0;
        for (int i = 0; ; i++) {//限定左侧阈值的边界下标
            tmpCount += diff2Freq.get(orderedKeyList.get(i));//累加freq
            if (totalFreqSum * freqBoundary <= tmpCount || i >= diff2Freq.size() * indexBoundary) {
                partitionLeftIndex = i;//记录左侧边界终止下标
                tmpCount = 0;
                break;
            }
        }

        for (int i = diff2Freq.size() - 1; i > partitionLeftIndex + 1; i--) {//从右往左确定右阈值的左边界
            tmpCount += diff2Freq.get(orderedKeyList.get(i));
            if (totalFreqSum * freqBoundary <= tmpCount) {
                partitionRightIndex = i;//记录右侧边界起始下标
//                tmpCount = 0;
                break;
            }
        }


        //评估函数计算
        //TODO:使用大顶堆优化评估函数计算的时间复杂度

        //优先在resleft中加入0阈值
        resLeft.add(0.0);
        count++;

//        if (ERR > orderedKeyList.get(0)) {//左端点阈值为0，直接加入res中
//            resLeft.add(orderedKeyList.get(0));
//            count++;
//        }

        //[1,partitionLeftIndex]
        while (count < Math.ceil((thresholdsNum + 1) / 2.0)) {
            for (int i = tmpIndex + 1; i < partitionLeftIndex + 1; i++) {
                double intervalLength = orderedKeyList.get(i) - orderedKeyList.get(tmpIndex);
                long freqSum = 0;
                for (int j = tmpIndex; j <= i; j++) {
                    freqSum += diff2Freq.get(orderedKeyList.get(j));//(orderedKeyList.get(tmpIndex),orderedKeyList.get[i]]范围内的所有freq
                }
                double score = freqSum / intervalLength;
                if (maxScore < score) {
                    maxScore = score;
                    index = i;
                }
            }
            resLeft.add(orderedKeyList.get(index));
            tmpIndex = index;//tmpIndex到index下标内的最优阈值被选取，不再考虑范围内的其他阈值
            maxScore = -1;
            count++;
        }


//        //划分右区间，起始点为max(index,orderedKeyList.size()/2)
//        tmpIndex = Math.max(index, orderedKeyList.size() / 2);//左侧寻找的最右阈值点


        //右区间，查询方向依然为从左至右 TODO:确认查询方向是否需要改变
        //[partitionRightIndex,diff2Freq.size()-1]
        tmpIndex = partitionRightIndex;
        while (count < thresholdsNum) {
            for (int i = tmpIndex; i < diff2Freq.size() - 1; i++) {
                double intervalLength = orderedKeyList.get(diff2Freq.size() - 1) - orderedKeyList.get(i);//当前阈值与右端点阈值的区间长度
                int freqSum = 0;
                for (int j = i + 1; j < diff2Freq.size(); j++) {
                    freqSum += diff2Freq.get(orderedKeyList.get(j));//(orderedKeyList.get(i),右端点阈值]范围内的所有freq
                }
                double score = freqSum / intervalLength;
                if (maxScore < score) {
                    maxScore = score;
                    index = i;
                }
            }
            resRight.add(orderedKeyList.get(index));
            tmpIndex = index + 1;//右区间起始点变为上一轮选中的阈值的后一个阈值
            maxScore = -1;
            count++;
        }


        //去重操作
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
