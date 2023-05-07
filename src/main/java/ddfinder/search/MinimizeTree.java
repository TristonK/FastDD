package ddfinder.search;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.search.NTreeSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author tristonK 2023/4/25
 */
public class MinimizeTree {
    protected HashMap<Integer, MinimizeTree> subtrees = new HashMap<>();
    protected List<IBitSet> bitsets;

    public MinimizeTree(){
        //this.intervalSize = intervalSize;
        this.bitsets = new ArrayList<>();
    }

    public boolean canAdd(IBitSet candidateTransformed, List<Integer> nodes, int index){
        if(bitsets.size() > 0){
            for(IBitSet bs : bitsets){
                if(candidateTransformed.isSubSetOf(bs)){
                    return false;
                }
            }
        }
        if(index >= nodes.size()){return true;}
        int nextNode = nodes.get(index);
        if(subtrees.containsKey(nextNode)){
            boolean flag = subtrees.get(nextNode).canAdd(candidateTransformed, nodes, index + 1);
            if(!flag){return  false;}
        }
        return canAdd(candidateTransformed, nodes, index + 1);
    }

    public boolean addTree(IBitSet candidateTransformed, List<Integer> nodes){
        if(!canAdd(candidateTransformed, nodes, 0)){
            return false;
        }
        MinimizeTree currNode = this;
        for(int i = 0; i < nodes.size(); i++){
            int currNodeId = nodes.get(i);
            if(!currNode.subtrees.containsKey(currNodeId)){
                currNode.subtrees.put(currNodeId, new MinimizeTree());
            }
            currNode = currNode.subtrees.get(currNodeId);
        }
        currNode.bitsets.add(candidateTransformed);
        return true;
    }
}
