package com.controle.despesas.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Despesa {
    private LocalDate dataCompra;
    private String origemCompra;
    private String nomeProduto;
    private String nomeLoja;
    private int parcelasPagas;
    private int totalParcelas;
    private double valorTotal;
    private int diaVencimento; // Dia do mês (ex: 5, 10, 20)
    private String nomeComprador;

    public Despesa(LocalDate dataCompra, String origemCompra, String nomeProduto,
                   String nomeLoja, int parcelasPagas, int totalParcelas,
                   double valorTotal, int diaVencimento, String nomeComprador) {
        this.dataCompra = dataCompra;
        this.origemCompra = origemCompra;
        this.nomeProduto = nomeProduto;
        this.nomeLoja = nomeLoja;
        this.parcelasPagas = parcelasPagas;
        this.totalParcelas = totalParcelas;
        this.valorTotal = valorTotal;
        this.diaVencimento = diaVencimento;
        this.nomeComprador = nomeComprador;
    }

    public LocalDate getDataCompra() { return dataCompra; }
    public String getOrigemCompra() { return origemCompra; }
    public String getNomeProduto() { return nomeProduto; }
    public String getNomeLoja() { return nomeLoja; }
    public int getParcelasPagas() { return parcelasPagas; }
    public int getTotalParcelas() { return totalParcelas; }
    public double getValorTotal() { return valorTotal; }
    public int getDiaVencimento() { return diaVencimento; }
    public String getNomeComprador() { return nomeComprador; }

    // Regras de Negócio
    public boolean isQuitada() {
        return parcelasPagas >= totalParcelas;
    }

    public int getParcelasRestantes() {
        int restantes = totalParcelas - parcelasPagas;
        return Math.max(restantes, 0);
    }

    public double getValorParcelaMensal() {
        if (totalParcelas <= 0) return valorTotal;
        return valorTotal / totalParcelas;
    }

    // Calcula a data de vencimento da próxima parcela em aberto
    public String getProximoVencimentoFormatado() {
        if (isQuitada()) {
            return "ENCERRADA (PAGA)";
        }
        LocalDate base = dataCompra.plusMonths(parcelasPagas);
        int diaAjustado = Math.min(diaVencimento, base.lengthOfMonth());
        LocalDate proximoVencimento = LocalDate.of(base.getYear(), base.getMonth(), diaAjustado);
        return proximoVencimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String toCsvLine() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("%s;%s;%s;%s;%d;%d;%.2f;%d;%s;%s",
                dataCompra.format(fmt), origemCompra, nomeProduto, nomeLoja,
                parcelasPagas, totalParcelas, valorTotal, diaVencimento,
                nomeComprador, isQuitada() ? "QUITADA" : "ATIVA");
    }
}