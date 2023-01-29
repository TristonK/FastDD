package ddfinder.pli;

import java.util.List;

public class PliShard {

    public final List<Pli> plis;

    /*
        tuple id range [beg, end)
    */
    public final int beg, end;

    public PliShard(List<Pli> plis, int beg, int end) {
        this.plis = plis;
        this.beg = beg;
        this.end = end;

        for (Pli pli : plis) {
            pli.pliShard = this;
        }
    }
}
