package com.model.Noise;

import com.model.Graph3D.Point3D;

public class LayeredPerlinNoise3D {
    private PerlinNoise3D[] perlinNoiseList;

    public LayeredPerlinNoise3D(double range, double startFrequency, int octaves) {
        perlinNoiseList = new PerlinNoise3D[octaves];

        double frequency = startFrequency;
        for(int i = 0; i < octaves; i++) {
            perlinNoiseList[i] = new PerlinNoise3D(range, frequency);
            frequency *= 2;
        }
    }

    public double getNoiseValue(double x, double y, double z) {
        double noiseValue = 0;
        double amplitude = 1;
        double totalAmplitude = 0;

        for(PerlinNoise3D perlinNoise : perlinNoiseList) {
            noiseValue += perlinNoise.getNoiseValue(x, y, z) * amplitude;
            totalAmplitude += amplitude;
            amplitude /= 2;
        }

        return (noiseValue / totalAmplitude) * 2.6;
    }

    public double getNoiseValue(Point3D point) {
        return getNoiseValue(point.getX(),point.getY(),point.getZ());
    }
}
