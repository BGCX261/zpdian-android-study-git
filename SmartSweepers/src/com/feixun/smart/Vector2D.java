package com.feixun.smart;

public class Vector2D {
    double x;
    double y;

    public Vector2D(double x, double y) {
        super();
        this.x = x;
        this.y = y;
    }

    Vector2D zijia(Vector2D rhs) {
        this.x += rhs.x;
        this.y += rhs.y;

        return this;
    }

    Vector2D zijian(Vector2D rhs) {
        this.x -= rhs.x;
        this.y -= rhs.y;

        return this;
    }

    Vector2D zichen(double rhs) {
        this.x *= rhs;
        this.y *= rhs;

        return this;
    }

    Vector2D zichu(double rhs) {
        this.x /= rhs;
        this.y /= rhs;

        return this;
    }

    static Vector2D chen(Vector2D lhs, double rhs) // lhsÎªÒıÓÃ
    {
        Vector2D result = new Vector2D(lhs.x, lhs.y);
        result = result.zichen(rhs);
        return result;
    }

    // Vector2D chen(double lhs, Vector2D rhs) {
    // Vector2D result = new Vector2D(rhs.x, rhs.y);
    // result = result.zichen(lhs);
    // return result;
    // }

    // overload the - operator
    static Vector2D jian(Vector2D lhs, Vector2D rhs) {
        Vector2D result = new Vector2D(lhs.x, lhs.y);
        result.x -= rhs.x;
        result.y -= rhs.y;

        return result;
    }

    // ------------------------- Vec2DLength -----------------------------
    //
    // returns the length of a 2D vector
    // --------------------------------------------------------------------
    static double vec2DLength(Vector2D v) {
        return Math.sqrt(v.x * v.x + v.y * v.y);
    }

    // ------------------------- Vec2DNormalize -----------------------------
    Vector2D vec2DNormalize(Vector2D v) {
        double length = vec2DLength(v);

        v.x = v.x / length;
        v.y = v.y / length;
        return v;
    }

    // ------------------------- Vec2DDot --------------------------
    //
    // calculates the dot product
    // --------------------------------------------------------------------
    double vec2DDot(Vector2D v1, Vector2D v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    // ------------------------ Vec2DSign --------------------------------
    //
    // returns positive if v2 is clockwise of v1, minus if anticlockwise
    // -------------------------------------------------------------------
    int vec2DSign(Vector2D v1, Vector2D v2) {
        if (v1.y * v2.x > v1.x * v2.y) {
            return 1;
        } else {
            return -1;
        }
    }
}
