package com.data;

import java.util.Objects;

class EdgeKey {
    int u, v;

    EdgeKey(int a, int b) {
        this.u = Math.min(a, b);
        this.v = Math.max(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EdgeKey))
            return false;
        EdgeKey e = (EdgeKey) o;
        return this.u == e.u && this.v == e.v;
    }

    @Override
    public int hashCode() {
        return Objects.hash(u, v);
    }
}