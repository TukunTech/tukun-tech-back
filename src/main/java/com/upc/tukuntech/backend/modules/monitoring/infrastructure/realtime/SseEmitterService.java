package com.upc.tukuntech.backend.modules.monitoring.infrastructure.realtime;

import com.upc.tukuntech.backend.modules.monitoring.domain.entity.Alert;
import com.upc.tukuntech.backend.modules.monitoring.domain.entity.VitalSignRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio responsable de manejar conexiones SSE (Server-Sent Events)
 * para notificaciones en tiempo real de mediciones y alertas.
 */
@Service
public class SseEmitterService {

    /** Clave: userId (paciente o cuidador) -> conexión SSE activa */
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Registra un nuevo cliente para recibir eventos en tiempo real.
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(0L); // conexión indefinida (se mantiene abierta)
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        return emitter;
    }

    /**
     * Envía una medición actualizada (paciente).
     */
    public void emitVitalSign(VitalSignRecord record) {
        Long patientId = record.getPatientId();
        sendToUser(patientId, "vital-sign-update", record);
    }

    /**
     * Envía una alerta generada al paciente y/o a sus cuidadores.
     */
    public void emitAlert(Alert alert) {
        // 🔸 Notificar al paciente directamente
        sendToUser(alert.getPatientId(), "alert", alert);

        // 🔸 Difundir a cuidadores (más adelante, se integrará con CareManagement)
        broadcastToCaregivers(alert);
    }

    /**
     * Envío directo a un usuario específico.
     */
    public void sendToUser(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }

    /**
     * Difunde un evento global (por ejemplo, para cuidadores o panel de monitoreo).
     */
    public void broadcast(Object event) {
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("update").data(event));
            } catch (IOException e) {
                emitters.remove(id);
            }
        });
    }

    /**
     * Difunde una alerta a todos los cuidadores registrados (futuro: basado en asignación).
     */
    private void broadcastToCaregivers(Alert alert) {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("alert")
                        .data(alert));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        });
    }
}