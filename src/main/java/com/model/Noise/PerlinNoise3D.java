package com.model.Noise;

public class PerlinNoise3D {
    private double frequency;
    private double range;
    private int size;
    private double[][][][] vectors;
    public PerlinNoise3D(double range, double frequency) {
        this.range = range;
        this.frequency = frequency;
        this.size = (int) Math.round(1 + 2*this.range*this.frequency);
        this.vectors = new double[this.size][this.size][this.size][3];
        this.generateNoise();
    }
    public void generateNoise() {
        // generate vectors
        for (int x = 0; x < this.size; x++) {
            for (int y = 0; y < this.size; y++) {
                for (int z = 0; z < this.size; z++) {
                    this.vectors[x][y][z] = generateRandomVector();
                }
            }
        }
    }

    public double getNoiseValue(double x, double y, double z) {
        // define grid cell coordinates
        int x0 = (int) (this.range * this.frequency + Math.floor(x * this.frequency));
        int x1 = x0 + 1;
        int y0 = (int) (this.range * this.frequency + Math.floor(y * this.frequency));
        int y1 = y0 + 1;
        int z0 = (int) (this.range * this.frequency + Math.floor(z * this.frequency));
        int z1 = z0 + 1;

        // get distance vectors
        double dx = (this.range * this.frequency + x * this.frequency) - x0;
        double dy = (this.range * this.frequency + y * this.frequency) - y0;
        double dz = (this.range * this.frequency + z * this.frequency) - z0;
        
        // get gradient vectors
        double[] g000 = this.vectors[x0][y0][z0];
        double[] g100 = this.vectors[x1][y0][z0];
        double[] g010 = this.vectors[x0][y1][z0];
        double[] g110 = this.vectors[x1][y1][z0];
        double[] g001 = this.vectors[x0][y0][z1];
        double[] g101 = this.vectors[x1][y0][z1];
        double[] g011 = this.vectors[x0][y1][z1];
        double[] g111 = this.vectors[x1][y1][z1];

        // compute dot products
        double n000 = dotProduct(g000, dx, dy, dz);
        double n100 = dotProduct(g100, dx - 1, dy, dz);
        double n010 = dotProduct(g010, dx, dy - 1, dz);
        double n110 = dotProduct(g110, dx - 1, dy - 1, dz);
        double n001 = dotProduct(g001, dx, dy, dz - 1);
        double n101 = dotProduct(g101, dx - 1, dy, dz - 1);
        double n011 = dotProduct(g011, dx, dy - 1, dz - 1);
        double n111 = dotProduct(g111, dx - 1, dy - 1, dz - 1);

        // compute fade curves
        double u = fade(dx);
        double v = fade(dy);
        double w = fade(dz);

        // interpolate
        double nx00 = lerp(n000, n100, u);
        double nx01 = lerp(n001, n101, u);
        double nx10 = lerp(n010, n110, u);
        double nx11 = lerp(n011, n111, u);

        double nxy0 = lerp(nx00, nx10, v);
        double nxy1 = lerp(nx01, nx11, v);
        
        double nxyz = lerp(nxy0, nxy1, w);
        
        return nxyz;
    }

    double[] generateRandomVector() {
        double theta = Math.random() * 2 * Math.PI;
        double phi = Math.acos(2 * Math.random() - 1);
        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);
        return new double[]{x, y, z};
    }

    double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    double dotProduct(double[] g, double x, double y, double z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }
}
