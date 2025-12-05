package io.github.hyscript7.ascendancy.features.threefold;

import java.util.Arrays;

public class ThreefoldHistory {
    private int ptr;
    private final String[] arr;

    public ThreefoldHistory(int size) {
        if (size < 4) {
            throw new IllegalArgumentException("History size must be greater than or equal to 4.");
        }
        this.ptr = -1;
        this.arr = new String[size];
    }

    public String[] get() {
        int start = (ptr + 1) % arr.length; // Since ptr is tail, this should be head
        String[] result = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[(start + i) % arr.length];
        }
        return result;
    }

    public void update(String content) {
        ptr += 1;
        ptr = ptr % arr.length;
        arr[ptr] = ThreefoldUtils.normalizeContent(content);
    }

    public void clear() {
        Arrays.fill(arr, null);
        ptr = 0;
    }
}
