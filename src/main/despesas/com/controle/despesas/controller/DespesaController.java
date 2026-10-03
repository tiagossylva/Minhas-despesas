package com.controle.despesas.controller;

import com.controle.despesas.dao.DespesaDAO;
import com.controle.despesas.model.Despesa;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DespesaController {
    private DespesaDAO dao;

    public DespesaController() {
        this.dao = new DespesaDAO();
    }

    public void adicionarDespesa(String dataStr, String origem, String produto, String loja,
                                 String pagasStr, String totalStr, String valorStr,
                                 String diaVencimentoStr, String comprador) throws Exception {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data = LocalDate.parse(dataStr, fmt);
        int pagas = Integer.parseInt(pagasStr);
        int total = Integer.parseInt(totalStr);
        double valor = Double.parseDouble(valorStr.replace(",", "."));
        int diaVencimento = Integer.parseInt(diaVencimentoStr);

        if (diaVencimento < 1 || diaVencimento > 31) {
            throw new IllegalArgumentException("O dia de vencimento deve estar entre 1 e 31.");
        }

        Despesa despesa = new Despesa(data, origem, produto, loja, pagas, total, valor, diaVencimento, comprador);
        dao.salvar(despesa);
    }

    public List<Despesa> listarDespesas(String filtroComprador) {
        List<Despesa> todas = dao.carregarTodas();
        if (filtroComprador == null || filtroComprador.trim().isEmpty()) {
            return todas;
        }
        return todas.stream()
                .filter(d -> d.getNomeComprador().equalsIgnoreCase(filtroComprador.trim()))
                .collect(Collectors.toList());
    }

    // Soma o valor total de todas as compras da lista
    public double calcularTotalGeral(List<Despesa> despesas) {
        return despesas.stream().mapToDouble(Despesa::getValorTotal).sum();
    }

    // Soma APENAS as parcelas do mês de compras ainda NÃO quitadas
    public double calcularTotalParcelasMensaisAtivas(List<Despesa> despesas) {
        return despesas.stream()
                .filter(d -> !d.isQuitada())
                .mapToDouble(Despesa::getValorParcelaMensal)
                .sum();
    }

    public void exportarRelatorio(List<Despesa> despesas, String filtro, double totalGeral, double totalMensal, String destino) throws IOException {
        dao.exportarTxt(despesas, filtro, totalGeral, totalMensal, destino);
    }
}