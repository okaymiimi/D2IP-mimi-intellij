package org.example.model;

import java.util.Objects;

public class Pair {
    private final int id1;
    private final int id2;

    public Pair(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public int getId1() {
        return id1;
    }

    public int getId2() {
        return id2;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;
        Pair p = (Pair) o;
        return id1 == p.id1 && id2 == p.id2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }

    @Override
    public String toString() {
        return id1 + "," + id2;
    }
}
