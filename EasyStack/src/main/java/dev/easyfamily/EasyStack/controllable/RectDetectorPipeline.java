package dev.easyfamily.EasyStack.controllable;

import org.firstinspires.ftc.vision.VisionProcessor;

import java.util.List;

/**
 * Contrato para um pipeline que detecta retângulos.
 * A implementação concreta deve fornecer as detecções com coordenadas normalizadas
 * entre 0.0 e 1.0 e implementar o processamento de imagem do VisionProcessor.
 */
public interface RectDetectorPipeline extends VisionProcessor {
    /** Retorna as detecções do frame mais recente. */
    List<RectBlob> getDetections();

    /** Opcional: retoma a detecção quando a câmera inicia. */
    default void resumeDetection() { }

    /** Opcional: suspende a detecção quando a câmera para. */
    default void suspendDetection() { }

    /** Dados de um retângulo detectado, em coordenadas normalizadas. */
    final class RectBlob {
        public final int id;
        public final String label;
        public final double cxNorm;
        public final double cyNorm;
        public final double wNorm;
        public final double hNorm;
        public final double confidence;

        public RectBlob(
                int id,
                String label,
                double cxNorm,
                double cyNorm,
                double wNorm,
                double hNorm,
                double confidence
        ) {
            this.id = id;
            this.label = label == null ? "Rectangle" : label;
            this.cxNorm = cxNorm;
            this.cyNorm = cyNorm;
            this.wNorm = wNorm;
            this.hNorm = hNorm;
            this.confidence = confidence;
        }

        public double area() {
            return Math.max(0.0, wNorm) * Math.max(0.0, hNorm);
        }
    }
}
