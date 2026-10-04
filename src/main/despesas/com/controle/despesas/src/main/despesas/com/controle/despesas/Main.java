package com.controle.despesas;

import com.controle.despesas.controller.DespesaController;
import com.controle.despesas.view.DespesaView;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DespesaController controller = new DespesaController();
            DespesaView view = new DespesaView(controller);
            view.setVisible(true);
        });
    }
}