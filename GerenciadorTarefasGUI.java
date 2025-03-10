import javax.swing.table.TableRowSorter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class GerenciadorTarefasGUI extends JFrame {
    private DefaultTableModel tableModel;
    private JTable tarefaTable;
    private JTextField tituloField, descricaoField, pesquisaField;
    private JComboBox<String> prioridadeBox;
    private JDateChooser prazoChooser;
    private JButton addButton, removeButton, concluirButton, gerarRelatorioButton;

    private static final String FILE_NAME = "tarefas.csv";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public GerenciadorTarefasGUI() {
        setTitle("Gerenciador de Tarefas");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Configuração do painel de entrada de dados
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Nova Tarefa"));

        JLabel tituloLabel = new JLabel("Título:");
        tituloField = new JTextField();
        tituloField.setBackground(new Color(255, 255, 204)); // Amarelo claro
        inputPanel.add(tituloLabel);
        inputPanel.add(tituloField);

        JLabel descricaoLabel = new JLabel("Descrição:");
        descricaoField = new JTextField();
        descricaoField.setBackground(new Color(204, 255, 204)); // Verde claro
        inputPanel.add(descricaoLabel);
        inputPanel.add(descricaoField);

        JLabel prioridadeLabel = new JLabel("Prioridade:");
        prioridadeBox = new JComboBox<>(new String[]{"Baixa", "Média", "Alta"});
        prioridadeBox.setBackground(new Color(204, 229, 255)); // Azul claro
        inputPanel.add(prioridadeLabel);
        inputPanel.add(prioridadeBox);

        JLabel prazoLabel = new JLabel("Prazo:");
        prazoChooser = new JDateChooser();
        prazoChooser.setBackground(new Color(255, 204, 229)); // Rosa claro
        inputPanel.add(prazoLabel);
        inputPanel.add(prazoChooser);

        add(inputPanel, BorderLayout.NORTH);

        // Barra de pesquisa
        JPanel pesquisaPanel = new JPanel();
        pesquisaPanel.add(new JLabel("🔍 Pesquisar Tarefa:"));
        pesquisaField = new JTextField(15);
        pesquisaPanel.add(pesquisaField);
        add(pesquisaPanel, BorderLayout.WEST);

        pesquisaField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarTarefas(pesquisaField.getText());
            }
        });

        // Configuração da tabela
        tableModel = new DefaultTableModel(new String[]{"Título", "Descrição", "Prioridade", "Prazo", "Status"}, 0);
        tarefaTable = new JTable(tableModel);
        add(new JScrollPane(tarefaTable), BorderLayout.CENTER);

        // Configuração do painel de botões
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Adicionar");
        removeButton = new JButton("Excluir");
        concluirButton = new JButton("Concluir");
        gerarRelatorioButton = new JButton("Gerar Relatório PDF");

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(concluirButton);
        buttonPanel.add(gerarRelatorioButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Estilizando os botões e a janela
        addButton.setBackground(Color.GREEN);
        addButton.setForeground(Color.WHITE);
        removeButton.setBackground(Color.RED);
        removeButton.setForeground(Color.WHITE);
        concluirButton.setBackground(Color.CYAN);
        concluirButton.setForeground(Color.BLACK);
        getContentPane().setBackground(new Color(240, 240, 240)); // cor cinza claro

        // Ações dos botões
        addButton.addActionListener(e -> adicionarTarefa());
        removeButton.addActionListener(e -> excluirTarefa());
        concluirButton.addActionListener(e -> concluirTarefa());
        gerarRelatorioButton.addActionListener(e -> gerarRelatorioPDF());

        carregarTarefas();
    }

    private void excluirTarefa() {
        int selectedRow = tarefaTable.getSelectedRow();
        if (selectedRow != -1) {
            int resposta = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir esta tarefa?", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (resposta == JOptionPane.YES_OPTION) {
                tableModel.removeRow(selectedRow);
                salvarTarefas();
            }
        }
    }

    private void adicionarTarefa() {
        String titulo = tituloField.getText();
        String descricao = descricaoField.getText();
        String prioridade = (String) prioridadeBox.getSelectedItem();
        Date prazoData = prazoChooser.getDate();

        if (titulo.isEmpty() || descricao.isEmpty() || prazoData == null) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos antes de adicionar a tarefa.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String prazo = DATE_FORMAT.format(prazoData);
        tableModel.addRow(new Object[]{titulo, descricao, prioridade, prazo, "Pendente"});
        salvarTarefas();

        tituloField.setText("");
        descricaoField.setText("");
        prioridadeBox.setSelectedIndex(0);
        prazoChooser.setDate(null);
    }

    private void concluirTarefa() {
        int selectedRow = tarefaTable.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.setValueAt("Concluída", selectedRow, 4);
            salvarTarefas();
        }
    }

    private void salvarTarefas() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.write(tableModel.getValueAt(i, 0) + "," +
                             tableModel.getValueAt(i, 1) + "," +
                             tableModel.getValueAt(i, 2) + "," +
                             tableModel.getValueAt(i, 3) + "," +
                             tableModel.getValueAt(i, 4) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filtrarTarefas(String pesquisa) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        tarefaTable.setRowSorter(sorter);
        RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + pesquisa, 0);
        sorter.setRowFilter(rf);
    }

    private void gerarRelatorioPDF() {
        JOptionPane.showMessageDialog(this, "Função de geração de relatório ainda não implementada.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void carregarTarefas() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5) {  // Garantir que há todas as colunas
                    tableModel.addRow(data);
                }
            }
        } catch (IOException e) {
            System.out.println("Nenhum arquivo encontrado. Iniciando com lista vazia.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GerenciadorTarefasGUI app = new GerenciadorTarefasGUI();
            app.setVisible(true);
        });
    }
}