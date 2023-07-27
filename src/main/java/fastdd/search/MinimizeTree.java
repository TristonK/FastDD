package fastdd.search;

import ch.javasoft.bitset.IBitSet;

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

    public IBitSet canAdd(IBitSet candidateTransformed, List<Integer> nodes, int index){
        if(bitsets.size() > 0){
            for(IBitSet bs : bitsets){
                if(candidateTransformed.isSubSetOf(bs)){
                    return bs;
                    //return false;
                }
            }
        }
        if(index >= nodes.size()){
            return null;
            //return true;
        }
        int nextNode = nodes.get(index);
        if(subtrees.containsKey(nextNode)){
            IBitSet flag = subtrees.get(nextNode).canAdd(candidateTransformed, nodes, index + 1);
            if(flag != null){return flag;}
            //if(!flag){return  false;}
        }
        return canAdd(candidateTransformed, nodes, index + 1);
    }

    public IBitSet addTree(IBitSet candidateTransformed, List<Integer> nodes){
        IBitSet canRemovedBy = canAdd(candidateTransformed, nodes, 0);
        if(canRemovedBy != null){
            return canRemovedBy;
            //return false;
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
        return null;
    }
}
