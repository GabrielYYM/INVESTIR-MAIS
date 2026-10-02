package com.repositorio.investir_mais.infrastructure.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    /**
     * Redireciona as rotas do frontend (React SPA) para o index.html.
     */
    @GetMapping({
            "/dashboard",
            "/reset-password",
            "/aportes",
            "/profile"
    })
    public String forwardToFrontend() {
        return "forward:/index.html";
    }
}
