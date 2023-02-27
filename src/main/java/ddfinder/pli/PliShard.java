package ddfinder.pli;

import java.util.List;

public class PliShard {

    public final List<IPli> plis;

    /*
        tuple id range [beg, end)
    */
    public final int beg, end;

    public PliShard(List<IPli> plis, int beg, int end) {
        this.plis = plis;
        this.beg = beg;
        this.end = end;

        for (IPli pli : plis) {
            pli.setPlishard(this);
        }
    }
}
