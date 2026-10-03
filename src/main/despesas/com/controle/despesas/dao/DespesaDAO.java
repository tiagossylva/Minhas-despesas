package com.controle.despesas.dao;

import com.controle.despesas.model.Despesa;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DespesaDAO {
    private static final String ARQUIVO_EXCEL = "despesas.csv";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void salvar(Despesa despesa) throws IOException {
        File file = new File(ARQUIVO_EXCEL);
        boolean novoArquivo = !file.exists();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            if (novoArquivo) {
                writer.write("Data;Origem;Produto;Loja;Parcelas Pagas;Total Parcelas;Valor Total;Dia Vencimento;Comprador;Status\n");
            }
            writer.write(despesa.toCsvLine());
            writer.newLine();
        }
    }

    public List<Despesa> carregarTodas() {
        List<Despesa> lista = new ArrayList<>();
        File file = new File(ARQUIVO_EXCEL);
        if (!file.exists()) return lista;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String linha = reader.readLine(); // Pular cabeçalho
            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] colunas = linha.split(";");
                if (colunas.length >= 9) {
                    LocalDate data = LocalDate.parse(colunas[0], FMT);
                    String origem = colunas[1];
                    String produto = colunas[2];
                    String loja = colunas[3];
                    int pagas = Integer.parseInt(colunas[4]);
                    int total = Integer.parseInt(colunas[5]);
                    double valor = Double.parseDouble(colunas[6].replace(",", "."));
                    int vencimento = Integer.parseInt(colunas[7]);
                    String comprador = colunas[8];

                    lista.add(new Despesa(data, origem, produto, loja, pagas, total, valor, vencimento, comprador));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void exportarTxt(List<Despesa> lista, String filtroComprador, double totalGeral, double totalMensalAtivo, String caminhoTxt) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(caminhoTxt), StandardCharsets.UTF_8))) {
            writer.write("=========================================================================\n");
            writer.write("                RELATÓRIO DE DESPESAS E PARCELAS MENSAIS                 \n");
            writer.write("=========================================================================\n");
            writer.write("Filtro Comprador: " + (filtroComprador.isEmpty() ? "TODOS" : filtroComprador) + "\n\n");

            for (Despesa d : lista) {
                writer.write(String.format("Data Compra: %s | Loja: %s | Produto: %s\n",
                        d.getDataCompra().format(FMT), d.getNomeLoja(), d.getNomeProduto()));
                writer.write(String.format("Origem: %s | Parcelas: %d/%d (Restam: %d) | Comprador: %s\n",
                        d.getOrigemCompra(), d.getParcelasPagas(), d.getTotalParcelas(), d.getParcelasRestantes(), d.getNomeComprador()));
                writer.write(String.format("Valor Total: R$ %.2f | Parcela Mensal: R$ %.2f\n",
                        d.getValorTotal(), d.getValorParcelaMensal()));
                writer.write(String.format("Status: %s | Próximo Vencimento: %s (Dia %d)\n",
                        d.isQuitada() ? "ENCERRADA" : "EM ABERTO", d.getProximoVencimentoFormatado(), d.getDiaVencimento()));
                writer.write("-------------------------------------------------------------------------\n");
            }

            writer.write(String.format("\nTOTAL DAS COMPRAS FILTRADAS: R$ %.2f\n", totalGeral));
            writer.write(String.format("TOTAL DAS PARCELAS DO MÊS (COBRANÇAS ATIVAS): R$ %.2f\n", totalMensalAtivo));
            writer.write("=========================================================================\n");
        }
    }
}