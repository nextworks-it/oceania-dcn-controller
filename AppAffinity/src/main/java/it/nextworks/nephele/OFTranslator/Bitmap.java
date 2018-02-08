package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bitmap {

    static final Bitmap NULL_BITMAP = new Bitmap(new boolean[Const.T]);

    private boolean[] bmp = new boolean[Const.T];

    private List<Integer> nonZero = new ArrayList<>();

    private int cursor = 0;

    //returns the element of index ind as an integer (i.e. 0 or 1)
    private Integer el(Integer ind) {
        if (ind < Const.T) {
            if (bmp[ind]) return 1;
            else return 0;
        } else {
            throw new IllegalArgumentException("Access to nonexistent timeslot");
        }
    }

    String getBitmap() {
        StringBuilder out = new StringBuilder();
        for (Integer i = 0; i < Const.T; i++) {
            out = out.append(el(i).toString());
        }
        return out.toString();
    }

    Bitmap(String in) {
        if (in.length() != Const.T) throw new IllegalArgumentException("Invalid timeslot length");
        for (Integer i = 0; i < Const.T; i++) {
            if (in.charAt(i) == '1') bmp[i] = true;
            else if (in.charAt(i) == '0') bmp[i] = false;
            else throw new IllegalArgumentException("Invalid timeslot character (not 1 nor 0)");
        }
        checkNonZero();
    }

    static Bitmap inverting(String in) {
        if (in.length() != Const.T) throw new IllegalArgumentException("Invalid timeslot length");
        boolean[] tbmp = new boolean[Const.T];
        for (Integer i = 0; i < Const.T; i++) {
            if ((!(in.charAt(i) == '1')) && (!(in.charAt(i) == '0')))
                throw new IllegalArgumentException("Invalid timeslot character (not 1 nor 0)");
            tbmp[i] = (in.charAt(i) == '0');
        }
        return new Bitmap(tbmp);
    }

    Bitmap(boolean[] in) {
        if (in.length != Const.T) throw new IllegalArgumentException("Invalid timeslot length");
        else bmp = in;
        checkNonZero();
    }

    private void checkNonZero() {
        for (int i = 0; i < Const.T; i++) {
            if (bmp[i]) {
                nonZero.add(i);
            }
        }
    }

    /**
     * Extract a sub-bitmap with exactly i 1 entries, if possible.
     *
     * Subsequent calls on the same bitmap will return disjoint sub-bitmaps
     * of the first one, splicing the original into many sub-bitmaps.
     * Once exhausted, this method cannot be called again on the original bitmap
     * i.e.: once a call returns an illegalArgumentException, each subsequent
     * call (with the same or greater parameter) will also fail.
     *
     * @param i: the number of desired non-zero entries
     * @return a Bitmap
     * @throws IllegalArgumentException if too many non-zero entries are required
     */
    Bitmap splice(int i) {
        if (cursor + i > nonZero.size()) {
            throw new IllegalArgumentException(String.format("Not enough non-zero entries: %s remaining.",
                bmp.length - cursor));
        } else {
            boolean[] subBmp = new boolean[Const.T];
            for (int j = cursor; j < cursor + i; j++) {
                subBmp[nonZero.get(j)] = true;
            }
            cursor = cursor + i;
            return new Bitmap(subBmp);
        }
    }

    /**
     * Extract a sub-bitmap with all non-spliced entries set.
     *
     * This method will return a sub-bitmap of this. The sub-bitmap will
     * have an entry set iff this has the entry set and that entry has not been
     * spliced yet (by calls to this.splice.
     * After a call to remainingSlice, all calls to splice with non-zero argument
     * will fail.
     * @return the last slice of this bitmap.
     */
    Bitmap remainingSlice() {
        if (cursor == nonZero.size()) {
            return NULL_BITMAP;
        } else {
            return splice(nonZero.size() - cursor);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bitmap bitmap = (Bitmap) o;

        return this.nonZero.equals(bitmap.nonZero);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bmp);
    }
}
