package com.controle.despesas.view;

import com.controle.despesas.controller.DespesaController;
import com.controle.despesas.model.Despesa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DespesaView extends JFrame {
    private DespesaController controller;

    // Componentes do Formulário
    private JTextField txtData = new JTextField(8);
    private JComboBox<String> cbOrigem = new JComboBox<>(new String[]{"Cartão de Crédito", "Carnê", "Boleto", "Prestação", "Pix/Dinheiro", "Despesa Fixa (Recorrente)"});
    private JTextField txtProduto = new JTextField(10);
    private JTextField txtLoja = new JTextField(10);
    private JTextField txtParcelasPagas = new JTextField(3);
    private JTextField txtTotalParcelas = new JTextField(3);
    private JTextField txtValorTotal = new JTextField(6);
    private JTextField txtDiaVencimento = new JTextField(3);
    private JTextField txtComprador = new JTextField(8);

    // Componentes da Tabela e Filtros
    private JTable tabela;
    private DefaultTableModel tableModel;
    private JTextField txtFiltroComprador = new JTextField(10);
    private JLabel lblTotalGeral = new JLabel("Total das Compras: R$ 0,00");
    private JLabel lblTotalMensal = new JLabel("Parcelas Deste Mês (Ativas): R$ 0,00");

    public DespesaView(DespesaController controller) {
        this.controller = controller;
        setTitle("Controle de Despesas & Contas Fixas - MVC");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        initUI();
        ConfigurarEventos();
        atualizarTabela("");
    }

    private void initUI() {
        // Painel Superior: Cadastro
        JPanel painelCampos = new JPanel(new GridLayout(5, 4, 6, 6));
        painelCampos.setBorder(BorderFactory.createTitledBorder("Cadastrar Despesa / conta Fixa"));

        painelCampos.add(new JLabel("Data da Compra (dd/MM/yyyy):"));
        painelCampos.add(txtData);
        painelCampos.add(new JLabel("Origem:"));
        painelCampos.add(cbOrigem);

        painelCampos.add(new JLabel("Produto:"));
        painelCampos.add(txtProduto);
        painelCampos.add(new JLabel("Loja:"));
        painelCampos.add(txtLoja);

        painelCampos.add(new JLabel("Parcelas Já Pagas:"));
        painelCampos.add(txtParcelasPagas);
        painelCampos.add(new JLabel("Total de Parcelas:"));
        painelCampos.add(txtTotalParcelas);

        painelCampos.add(new JLabel("Valor Total da Compra (R$):"));
        painelCampos.add(txtValorTotal);
        painelCampos.add(new JLabel("Dia do Vencimento (1 a 31):"));
        painelCampos.add(txtDiaVencimento);

        painelCampos.add(new JLabel("Comprador:"));
        painelCampos.add(txtComprador);

        JButton btnSalvar = new JButton("Salvar Despesa (Grava no Excel)");
        btnSalvar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSalvar.addActionListener(e -> salvarDespesa());

        JPanel topo = new JPanel(new BorderLayout(5, 5));
        topo.add(painelCampos, BorderLayout.CENTER);
        topo.add(btnSalvar, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        // Painel Central: Tabela
        String[] colunas = {
                "Data", "Origem", "Produto/Serviço", "Loja/Empresa", "Parcelas",
                "Valor Total", "Valor Parcela", "Próx. Vencimento", "Comprador", "Status"
        };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela = new JTable(tableModel);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Painel Inferior: Filtros e Totais
        JPanel rodape = new JPanel(new BorderLayout(8, 8));
        rodape.setBorder(BorderFactory.createTitledBorder("Filtragem & Fechamento Mensal"));

        JPanel painelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        painelFiltro.add(new JLabel("Filtrar por Comprador:"));
        painelFiltro.add(txtFiltroComprador);

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.addActionListener(e -> atualizarTabela(txtFiltroComprador.getText()));
        painelFiltro.add(btnFiltrar);

        JButton btnLimpar = new JButton("Mostrar Todos");
        btnLimpar.addActionListener(e -> {
            txtFiltroComprador.setText("");
            atualizarTabela("");
        });
        painelFiltro.add(btnLimpar);

        JButton btnExportarTxt = new JButton("Imprimir Relatório TXT");
        btnExportarTxt.addActionListener(e -> exportarParaTxt());
        painelFiltro.add(btnExportarTxt);

        // Totais
        JPanel painelTotais = new JPanel(new GridLayout(2, 1, 4, 4));
        lblTotalGeral.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTotalMensal.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTotalMensal.setForeground(new Color(180, 0, 0)); // Destaque para cobrança ativa do mês
        painelTotais.add(lblTotalGeral);
        painelTotais.add(lblTotalMensal);

        rodape.add(painelFiltro, BorderLayout.WEST);
        rodape.add(painelTotais, BorderLayout.EAST);

        add(rodape, BorderLayout.SOUTH);
    }

    private void ConfigurarEventos() {
        // Ao selecionar "Despesa Fixa", desabilita campos de parcelas
        cbOrigem.addActionListener(e -> {
            boolean ehFixa = "Despesa Fixa (Recorrente)".equals(cbOrigem.getSelectedItem());
            if (ehFixa) {
                txtParcelasPagas.setText("0");
                txtTotalParcelas.setText("0");
                txtParcelasPagas.setEnabled(false);
                txtTotalParcelas.setEnabled(false);
            } else {
                txtParcelasPagas.setEnabled(true);
                txtTotalParcelas.setEnabled(true);
            }
        });
    }

    private void salvarDespesa() {
        try {
            controller.adicionarDespesa(
                    txtData.getText(),
                    (String) cbOrigem.getSelectedItem(),
                    txtProduto.getText(),
                    txtLoja.getText(),
                    txtParcelasPagas.getText(),
                    txtTotalParcelas.getText(),
                    txtValorTotal.getText(),
                    txtDiaVencimento.getText(),
                    txtComprador.getText()
            );
            JOptionPane.showMessageDialog(this, "Despesa registrada com sucesso!");
            limparFormulario();
            atualizarTabela(txtFiltroComprador.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao gravar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarTabela(String filtro) {
        tableModel.setRowCount(0);
        List<Despesa> lista = controller.listarDespesas(filtro);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Despesa d : lista) {
            tableModel.addRow(new Object[]{
                    d.getDataCompra().format(fmt),
                    d.getOrigemCompra(),
                    d.getNomeProduto(),
                    d.getNomeLoja(),
                    d.getParcelasPagas() + "/" + d.getTotalParcelas(),
                    String.format("R$ %.2f", d.getValorTotal()),
                    String.format("R$ %.2f", d.getValorParcelaMensal()),
                    d.getProximoVencimentoFormatado(),
                    d.getNomeComprador(),
                    d.isQuitada() ? "ENCERRADA" : "ATIVA"
            });
        }

        double totalGeral = controller.calcularTotalGeral(lista);
        double totalMensal = controller.calcularTotalParcelasMensaisAtivas(lista);

        lblTotalGeral.setText(String.format("Total Acumulado das Compras: R$ %.2f", totalGeral));
        lblTotalMensal.setText(String.format("Parcelas do Mês a Pagar (Ativas): R$ %.2f", totalMensal));
    }

    private void exportarParaTxt() {
        String filtro = txtFiltroComprador.getText();
        List<Despesa> lista = controller.listarDespesas(filtro);
        double totalGeral = controller.calcularTotalGeral(lista);
        double totalMensal = controller.calcularTotalParcelasMensaisAtivas(lista);

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("relatorio_mensal.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                controller.exportarRelatorio(lista, filtro, totalGeral, totalMensal, chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Relatório TXT emitido com sucesso!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao exportar TXT: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparFormulario() {
        txtData.setText("");
        txtProduto.setText("");
        txtLoja.setText("");
        txtParcelasPagas.setText("");
        txtTotalParcelas.setText("");
        txtValorTotal.setText("");
        txtDiaVencimento.setText("");
        txtComprador.setText("");
    }
}