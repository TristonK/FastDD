package fastdd.enumeration;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import fastdd.Config;
import fastdd.differentialdependency.DifferentialDependencySet;
import fastdd.dfset.DFSet;
import fastdd.differentialfunction.DifferentialFunction;
import fastdd.differentialfunction.DifferentialFunctionBuilder;
import fastdd.differentialfunction.DifferentialFunctionSet;
import fastdd.search.TranslatingMinimizeTree;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import de.metanome.algorithms.dcfinder.predicates.Operator;

import java.util.*;

/**
 * @author tristonK 2023/2/27
 */
public class HybridEvidenceInversion implements Enumeration{
    DifferentialFunctionSet predicates;
    HashMap<Integer, Set<Integer>> pred2PredGroupMap;
    Set<IBitSet> covers;
    DFSet DFSet;
    Map<Integer, LongBitSet> predSatisfiedDifferentialSet;
    Map<Integer, LongBitSet> dfNotSatisfiedDFSet;

    LongBitSet evidenceBitSet;

    IndexProvider<DifferentialFunction> predicateIndexProvider;

    List<Integer> differentialFunctions;

    List<BitSet> colToPredicatesGroup;
    Map<Integer, TranslatingMinimizeTree> minimizeTreeMap = new HashMap<>();

    Map<Integer, Integer> index2Diff;
    List<DifferentialFunction> highestDFofAttr;
    public HybridEvidenceInversion(DFSet DFSet, DifferentialFunctionBuilder differentialFunctionBuilder){
        this.predicates = new DifferentialFunctionSet(differentialFunctionBuilder.getPredicates().size());
        this.pred2PredGroupMap = new HashMap<>();
        this.covers = new HashSet<>();
        this.DFSet = DFSet;
        this.colToPredicatesGroup = new ArrayList<>();
        this.index2Diff = differentialFunctionBuilder.getBitsetIndex2ThresholdsIndex();
        for(BitSet bs: differentialFunctionBuilder.getColDFGroup()){
            colToPredicatesGroup.add((BitSet) bs.clone());
        }
        predicateIndexProvider = differentialFunctionBuilder.getPredicateIdProvider();
        highestDFofAttr = differentialFunctionBuilder.HighestDfOfAttr;

        this.differentialFunctions = new ArrayList<>();
        for(DifferentialFunction df: differentialFunctionBuilder.getPredicates()){
            int index = predicateIndexProvider.getIndex(df);
            this.differentialFunctions.add(index);
            predicates.add(df);
        }

        for (BitSet bitSet : colToPredicatesGroup) {
            HashSet<Integer> pids = new HashSet<>();
            for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
                pids.add(i);
            }
            for (int pid : pids) {
                pred2PredGroupMap.put(pid, pids);
            }
        }
        constructMinimizeTree(differentialFunctionBuilder);
    }

    /**
     * @return dds
     */
    @Override
    public DifferentialDependencySet buildDifferentialDenpendency() {
        buildClueIndexes();

        List<Integer> preds = new ArrayList<>(differentialFunctions);
        preds.sort((o1, o2) -> {
            if (Objects.equals(o1, o2)){return 0;}
            DifferentialFunction p1 = predicateIndexProvider.getObject(o1), p2 = predicateIndexProvider.getObject(o2);
            if (p1.operandWithOpHash() == p2.operandWithOpHash()){
                // 表示范围越来越大
                if (p1.getOperator().equals(Operator.LESS_EQUAL)){
                    return Double.compare(p1.getDistance(), p2.getDistance());
                    //return p2.getDistance() - p1.getDistance() < 0 ? 1 : -1;
                }
                return Double.compare(p2.getDistance(), p1.getDistance());
                //return p1.getDistance() - p2.getDistance() < 0 ? 1 : -1;
            }
            return o1 - o2;
            //return Integer.compare(dfNotSatisfiedDFSet.get(o1).cardinality(), dfNotSatisfiedDFSet.get(o2).cardinality());
        });

        DifferentialDependencySet ret = new DifferentialDependencySet();
        /*System.out.println("Minimize Tree: " + intervalSize + " === " + Arrays.toString(predicateId2NodeId)+ "==\n" +
                Arrays.toString(col2Interval) + " == "+ Arrays.toString(col2PredicateId) + " == " + colSize + " == " +
                Arrays.toString(intervalLength) + " ==\n " + index2Diff.toString());*/

        for(int i = 0; i < preds.size(); i++){
            int rightPid = preds.get(i);
            //System.out.println("Cal " +predicateIndexProvider.getObject(rightPid).toString());
            if (dfNotSatisfiedDFSet.get(rightPid).cardinality() == 0){
                if(Config.NeedRightAlwaysTrueDD){
                    DifferentialFunction rightDF = predicateIndexProvider.getObject(rightPid);
                    Set<IBitSet> pCovers = new HashSet<>();
                    for(DifferentialFunction df: highestDFofAttr){
                        if (!df.getOperand().equals(rightDF.getOperand())){
                            LongBitSet c = new LongBitSet(); c.set(predicateIndexProvider.getIndex(df));
                            pCovers.add(c);
                        }
                    }
                    ret.addAll(partialMinimize(rightPid, pCovers));
                }
                // dd must have at least two predicates
                continue;
            }

            List<Integer> currPredicateSpace = new ArrayList<>(differentialFunctions);
            currPredicateSpace.removeAll(pred2PredGroupMap.get(rightPid));
            Set<Integer> predsNotSatisfied = new HashSet<>();
            LongBitSet currDifferentialSet = dfNotSatisfiedDFSet.get(rightPid);
            List<LongBitSet> currEvidences = new ArrayList<>();
            for(int eviId = currDifferentialSet.nextSetBit(0); eviId >= 0; eviId = currDifferentialSet.nextSetBit(eviId + 1)){
                LongBitSet bs = DFSet.getEvidenceById(eviId).getBitset().clone();
                pred2PredGroupMap.get(rightPid).forEach(bs::clear);
                predsNotSatisfied.forEach(bs::clear);
                currEvidences.add(bs);
            }

            int satisfiedCol = predicateIndexProvider.getObject(rightPid).getOperand().getColumn().getIndex();
            Set<IBitSet> partialCovers =
                new EvidenceInversion(satisfiedCol, colToPredicatesGroup, predsNotSatisfied, currEvidences, predicates.size()).getCovers();

            ret.addAll(partialMinimize(rightPid, partialCovers));
        }
        System.out.println("[Minimize TIME]: " + MinimizeTime +" ms");
        System.out.println("[Minimize] # before " + BeforeMinimizeSize);
        return ret;
    }

    private void buildClueIndexes() {

        predSatisfiedDifferentialSet = new HashMap<>(predicates.size());
        dfNotSatisfiedDFSet = new HashMap<>(predicates.size());
        for(int i = 0; i < predicates.size(); i++){
            predSatisfiedDifferentialSet.put(i, new LongBitSet());
            dfNotSatisfiedDFSet.put(i, new LongBitSet());
        }
        evidenceBitSet = new LongBitSet();
        for(int evidenceId = 0; evidenceId < DFSet.size(); evidenceId++){
            evidenceBitSet.set(evidenceId);
            LongBitSet bs = DFSet.getEvidenceById(evidenceId).getBitset();
            for(int i = 0; i < predicates.size(); i++){
                if(bs.get(i)){
                    predSatisfiedDifferentialSet.get(i).set(evidenceId);
                }else{
                    dfNotSatisfiedDFSet.get(i).set(evidenceId);
                }
            }
        }
    }

    private void handleEvidence(LongBitSet evidenceBitSet, Set<LongBitSet> covers, List<Integer> predicateSpace){
        Set<LongBitSet> coversMinus = new HashSet<>();
        for(LongBitSet cover: covers){
            if(cover.isSubSetOf(evidenceBitSet)) {
                coversMinus.add(cover);
            }
        }
        covers.removeAll(coversMinus);
        List<Integer> predsInEvidence = new ArrayList<>();
        for(int i = evidenceBitSet.nextSetBit(0); i >= 0; i = evidenceBitSet.nextSetBit(i+1)){
            predsInEvidence.add(i);
        }
        List<Integer> pspace = new ArrayList<>(predicateSpace);
        pspace.removeAll(predsInEvidence);
        for(LongBitSet coverMinus: coversMinus){
            for(int pred : pspace){
                boolean flag = true;
                for(int related: pred2PredGroupMap.get(pred)){
                    if(coverMinus.get(related)){flag = false; break;}
                }
                if(!flag){continue;}
                coverMinus.set(pred);
                boolean exist = false;
                for(LongBitSet bs : covers){
                    if(bs.isSubSetOf(coverMinus)){exist = true; coverMinus.clear(pred); break;}
                }
                if(!exist){
                    LongBitSet nbs = coverMinus.clone();
                    covers.add(nbs);
                }
                coverMinus.clear(pred);
            }
        }
    }

    int intervalSize;
    int[] predicateId2NodeId;
    int[] col2Interval;
    int[] col2PredicateId;
    int colSize;
    int[] intervalLength;
    private void constructMinimizeTree(DifferentialFunctionBuilder differentialFunctionBuilder){
        colSize = differentialFunctionBuilder.getColSize();
        intervalSize = 0;
        predicateId2NodeId = new int[differentialFunctionBuilder.size()];
        col2Interval = new int[colSize];
        col2PredicateId = new int[colSize];
        intervalLength = new int[colSize];
        List<BitSet>  colPredicateGroup = differentialFunctionBuilder.getColDFGroup();
        for(int i = 0; i < colSize; i++){
            BitSet bs = colPredicateGroup.get(i);
            col2Interval[i] = intervalSize;
            intervalLength[i] = differentialFunctionBuilder.getColThresholdsSize(i);
            intervalSize += intervalLength[i];
            int cnt = 0;
            for(int j = bs.nextSetBit(0); j >= 0; j = bs.nextSetBit(j + 1)){
                DifferentialFunction df = predicateIndexProvider.getObject(j);
                if(df.getOperator() == Operator.LESS_EQUAL){
                    predicateId2NodeId[j] = i;
                }else{
                    predicateId2NodeId[j] = i + colSize;
                }
                if(cnt == 0){
                    col2PredicateId[i] = j;
                }
                cnt++;
            }
        }
    }

    private int getHash(DifferentialFunction p){
        if(p.getOperator().equals(Operator.LESS_EQUAL)){
            return p.getOperand().hashCode();
        }else {
            return -1 * p.getOperand().hashCode();
        }
    }

    private long MinimizeTime = 0;
    private long BeforeMinimizeSize = 0;
    private DifferentialDependencySet partialMinimize(int rightPid, Set<IBitSet> partialCovers){
        long t1 = System.currentTimeMillis();
        TranslatingMinimizeTree minimizeTree;
        if (!minimizeTreeMap.containsKey(predicateIndexProvider.getObject(rightPid).operandWithOpHash())){
            minimizeTree = new TranslatingMinimizeTree(intervalSize,predicateId2NodeId, col2Interval,
                    col2PredicateId, colSize, intervalLength, index2Diff);
        } else{
            minimizeTree = minimizeTreeMap.get(predicateIndexProvider.getObject(rightPid).operandWithOpHash());
        }
        BeforeMinimizeSize += partialCovers.size();
        Set<IBitSet> ret1 = minimizeTree.minimize(new ArrayList<>(partialCovers));
        minimizeTreeMap.put(predicateIndexProvider.getObject(rightPid).operandWithOpHash(), minimizeTree);
        MinimizeTime += System.currentTimeMillis() - t1;
        return new DifferentialDependencySet(ret1, rightPid, predicateIndexProvider);
    }
}