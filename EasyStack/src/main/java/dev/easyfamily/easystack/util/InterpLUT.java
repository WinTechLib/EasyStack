 package dev.easyfamily.easystack.util;

import java.util.Map;
import java.util.TreeMap;

public class InterpLUT {

    private final TreeMap<Double, Double> table = new TreeMap<>();
    private final TreeMap<Double, Double> tangents = new TreeMap<>();

    public void add(double input, double output) {
        table.put(input, output);
    }

    /**
     * Calcula as tangentes utilizadas pela interpolação cúbica.
     *
     * Deve ser chamado depois de adicionar todos os pontos.
     */
    public void createLUT() {

        if (table.size() < 2) {
            throw new IllegalStateException(
                    "InterpLUT must contain at least two points"
            );
        }

        tangents.clear();

        Double[] x = table.keySet().toArray(new Double[0]);
        Double[] y = table.values().toArray(new Double[0]);

        int n = x.length;

        // Slopes entre pontos consecutivos
        double[] slopes = new double[n - 1];

        for (int i = 0; i < n - 1; i++) {

            double h = x[i + 1] - x[i];

            if (h <= 0) {
                throw new IllegalArgumentException(
                        "Input values must be strictly increasing"
                );
            }

            slopes[i] = (y[i + 1] - y[i]) / h;
        }

        // Tangentes em cada ponto
        double[] m = new double[n];

        m[0] = slopes[0];

        for (int i = 1; i < n - 1; i++) {
            m[i] = (slopes[i - 1] + slopes[i]) * 0.5;
        }

        m[n - 1] = slopes[n - 2];

        // Ajusta as tangentes para preservar monotonicidade
        for (int i = 0; i < n - 1; i++) {

            if (slopes[i] == 0) {

                m[i] = 0;
                m[i + 1] = 0;

            } else {

                double a = m[i] / slopes[i];
                double b = m[i + 1] / slopes[i];

                double h = Math.hypot(a, b);

                if (h > 3) {

                    double t = 3 / h;

                    m[i] = t * a * slopes[i];
                    m[i + 1] = t * b * slopes[i];
                }
            }
        }

        // Guarda as tangentes associadas a cada X
        for (int i = 0; i < n; i++) {
            tangents.put(x[i], m[i]);
        }
    }

    public double get(double input) {

        if (table.isEmpty()) {
            throw new IllegalStateException("InterpLUT is empty");
        }

        if (tangents.size() != table.size()) {
            throw new IllegalStateException(
                    "Call createLUT() after adding the control points"
            );
        }

        // NaN
        if (Double.isNaN(input)) {
            return input;
        }

        // Abaixo do menor valor
        if (input <= table.firstKey()) {
            return table.firstEntry().getValue();
        }

        // Acima do maior valor
        if (input >= table.lastKey()) {
            return table.lastEntry().getValue();
        }

        // Encontra os dois pontos que cercam o input
        Map.Entry<Double, Double> lower =
                table.floorEntry(input);

        Map.Entry<Double, Double> upper =
                table.ceilingEntry(input);

        // Entrada exatamente em um ponto existente
        if (lower.getKey().equals(upper.getKey())) {
            return lower.getValue();
        }

        double x1 = lower.getKey();
        double y1 = lower.getValue();
        double m1 = tangents.get(x1);

        double x2 = upper.getKey();
        double y2 = upper.getValue();
        double m2 = tangents.get(x2);

        // Distância entre os pontos
        double h = x2 - x1;

        // Posição normalizada entre 0 e 1
        double t = (input - x1) / h;

        // Interpolação cúbica de Hermite
        return (y1 * (1 + 2 * t) + h * m1 * t)
                * (1 - t) * (1 - t)

                + (y2 * (3 - 2 * t) + h * m2 * (t - 1))
                * t * t;
    }

    public void clear() {
        table.clear();
        tangents.clear();
    }

    public int size() {
        return table.size();
    }
}

