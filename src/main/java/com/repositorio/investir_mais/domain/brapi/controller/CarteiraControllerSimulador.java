package com.repositorio.investir_mais.domain.brapi.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/carteira")
public class CarteiraControllerSimulador {

    @GetMapping("/ativos")
    public List<AtivoDTO> getAtivosDoUsuario() {
        return List.of(
                new AtivoDTO("1", "Ações internacionais", "QQQ", 2.51895, 28),
                new AtivoDTO("2", "Ações nacionais", "FLRY3", 176, 28),
                new AtivoDTO("3", "Ações nacionais", "WEGE3", 52, 26),
                new AtivoDTO("4", "Ações internacionais", "MCHI", 2.51895, 28)
        );
    }

    public record AtivoDTO(String id, String tipo, String ticker, double quantidade, int nota) {}
}
