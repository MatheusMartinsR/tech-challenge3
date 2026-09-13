package com.fiap.challenge.techChallenge3.scheduling.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentEventPublisherPort;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEditadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEventos;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventPublisherAdapter implements AppointmentEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    public AppointmentEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishAppointmentCreated(Appointment appointment) {
        rabbitTemplate.convertAndSend(
                ConsultaEventos.EXCHANGE,
                ConsultaEventos.APPOINTMENT_CREATED_ROUTING_KEY,
                new ConsultaCriadaEvent(
                        appointment.getId(),
                        appointment.getPaciente().getId(),
                        appointment.getPaciente().getNome(),
                        appointment.getPaciente().getEmail(),
                        appointment.getMedico().getId(),
                        appointment.getDataHora()));
    }

    @Override
    public void publishAppointmentUpdated(Appointment appointment) {
        rabbitTemplate.convertAndSend(
                ConsultaEventos.EXCHANGE,
                ConsultaEventos.APPOINTMENT_UPDATED_ROUTING_KEY,
                new ConsultaEditadaEvent(
                        appointment.getId(),
                        appointment.getPaciente().getId(),
                        appointment.getPaciente().getNome(),
                        appointment.getPaciente().getEmail(),
                        appointment.getMedico().getId(),
                        appointment.getDataHora(),
                        appointment.getStatus()));
    }
}
