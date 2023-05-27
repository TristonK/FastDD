package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.pli.PliShard;
import ddfinder.predicate.PredicateBuilder;

import java.util.Set;

public interface EvidenceSetBuild {
    public Set<LongBitSet> buildEvidenceSet(PliShard[] pliShards);

    EvidenceSet getEvidenceSet();
}
