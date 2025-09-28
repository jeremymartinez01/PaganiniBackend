package espol.integradora.paganinibackend.Dto;

import espol.integradora.paganinibackend.model.constantes.Plataforma;
public record RegisterDeviceRequest(String correo, String token, Plataforma plataforma) {}
